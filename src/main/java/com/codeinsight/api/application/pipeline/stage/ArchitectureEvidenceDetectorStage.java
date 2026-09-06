package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.pipeline.rules.ArchitectureDetectionRules;
import com.codeinsight.api.application.pipeline.rules.ArchitectureDetectionRules.FrontendFrameworkRule;
import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult.EngineeringEvidence;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.FrontendFramework;
import com.codeinsight.api.domain.model.ProjectKind;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Etapa 5 del Pipeline: Architecture Evidence Detector.
 *
 * Analiza la estructura de carpetas, nombres de paquetes y el contenido de los
 * archivos fuente para recopilar evidencias objetivas:
 * <ul>
 *   <li>Palabras clave de arquitectura en rutas de directorios</li>
 *   <li>Distribución de componentes por capa</li>
 *   <li>Relaciones inbound (UseCase implementado por un Service de aplicación)</li>
 *   <li>Relaciones outbound (Port implementado por un Adapter de infraestructura)</li>
 *   <li>Evidencias de testing genérico (multiplataforma: Java, Node, Python)</li>
 *   <li>Metadata de manifiestos (package.json: nombre, descripción, entrada principal, script test)</li>
 *   <li>Evidencias de DI Spring (@Configuration, @Bean, constructor injection)</li>
 * </ul>
 * No emite conclusiones ni clasificaciones rígidas: solo reporta hechos verificables.
 * <p>
 * Esta clase es genérica: no contiene ningún dato de detección hardcodeado.
 * Todas las reglas y constantes viven en {@link ArchitectureDetectionRules}.
 */
@Component
public class ArchitectureEvidenceDetectorStage {

    private static final Logger log = LoggerFactory.getLogger(ArchitectureEvidenceDetectorStage.class);

    // ── Patrones de análisis estático Java (lógica, no datos escalables) ─────

    /** Captura las interfaces declaradas en {@code implements A, B<X>}. */
    private static final Pattern IMPLEMENTS_PATTERN =
            Pattern.compile("\\bimplements\\s+([\\w,\\s<>]+?)(?:\\{|extends)");

    /** Detecta un constructor público con parámetros (indica posible inyección por constructor). */
    private static final Pattern CONSTRUCTOR_INJECTION_PATTERN =
            Pattern.compile("\\bpublic\\s+\\w+\\s*\\([^)]+\\)\\s*\\{");

    /** Detecta métodos anotados con {@code @Bean}. */
    private static final Pattern BEAN_PATTERN = Pattern.compile("@Bean");

    /** Detecta clases anotadas con {@code @Configuration}. */
    private static final Pattern CONFIGURATION_PATTERN = Pattern.compile("@Configuration");

    // ── Método principal ──────────────────────────────────────────────────────

    /**
     * Inspecciona las rutas de archivos escaneados y componentes identificados para
     * extraer evidencias estructurales, relaciones puerto→implementación,
     * métricas de testing genéricas y metadata del proyecto.
     *
     * @param scannedFiles      Mapa de archivos escaneados (Etapa 2).
     * @param componentAnalysis Resultado del análisis de componentes (Etapa 4).
     * @param technologyStack   Stack tecnológico ya detectado (Etapa 3). Se usa para
     *                          resolver {@link ProjectKind} con prioridad sobre las
     *                          heurísticas de extensión de archivo.
     * @return {@link ArchitectureEvidenceResult} con todas las evidencias recopiladas.
     */
    public ArchitectureEvidenceResult detect(ScannedFileMap scannedFiles,
                                             ComponentAnalysisResult componentAnalysis,
                                             TechnologyStack technologyStack) {
        if (scannedFiles == null) {
            throw new InvalidRepositoryException("ScannedFileMap must not be null");
        }

        Set<String> structuralPaths = new TreeSet<>();
        Set<String> detectedKeywords = new TreeSet<>();
        Map<String, Integer> packageComponentDistribution = new HashMap<>();
        List<String> evidenceNotes = new ArrayList<>();

        int maxDepth = 0;
        List<String> relativeFiles = scannedFiles.getRelativeFilePaths();

        // 1. Analizar rutas de directorios: palabras clave y profundidad
        if (relativeFiles != null) {
            for (String relativeFile : relativeFiles) {
                Path filePath = Path.of(relativeFile);
                Path parent = filePath.getParent();
                if (parent != null) {
                    structuralPaths.add(parent.toString());
                    int depth = parent.getNameCount();
                    if (depth > maxDepth) maxDepth = depth;
                    for (Path segment : parent) {
                        String nameLower = segment.toString().toLowerCase();
                        if (ArchitectureDetectionRules.KNOWN_ARCHITECTURAL_KEYWORDS.contains(nameLower)) {
                            detectedKeywords.add(nameLower);
                        }
                    }
                }
            }
        }

        // 2. Distribución de componentes por capa
        if (componentAnalysis != null && componentAnalysis.getComponents() != null) {
            for (DetectedComponent component : componentAnalysis.getComponents()) {
                String componentPath = component.getRelativePath();
                if (componentPath != null) {
                    String layer = extractTopLayerPackage(componentPath);
                    packageComponentDistribution.merge(layer, 1, Integer::sum);
                }
            }
        }

        // 3. Relaciones puerto→implementación (inbound / outbound)
        PortRelations portRelations = detectPortRelations(scannedFiles);

        // 4. Evidencias de ingeniería: testing genérico + DI + metadata de manifiesto
        EngineeringEvidence engineeringEvidence = detectEngineeringEvidence(scannedFiles);

        // 5. Detectar ProjectKind y FrontendFramework
        ProjectKind projectKind = determineProjectKind(scannedFiles, engineeringEvidence, technologyStack);
        FrontendFramework frontendFramework = determineFrontendFramework(scannedFiles);

        // 6. Detectar Evidencias de Arquitectura Frontend
        List<String> frontendEvidenceNotes = detectFrontendArchitectureEvidence(scannedFiles, projectKind);

        // 7. Generar notas de evidencia puramente factuales
        evidenceNotes.add("Clasificación de tipo de proyecto: " + projectKind + " (Framework Frontend: " + frontendFramework + ")");
        evidenceNotes.add("Identificadas " + structuralPaths.size()
                + " rutas estructurales distintas con profundidad máxima de " + maxDepth + " segmentos.");
        if (!detectedKeywords.isEmpty()) {
            evidenceNotes.add("Palabras clave de arquitectura encontradas en rutas: " + String.join(", ", detectedKeywords));
        }
        if (!packageComponentDistribution.isEmpty()) {
            evidenceNotes.add("Distribución de componentes por paquete/capa: " + packageComponentDistribution);
        }
        if (!portRelations.inbound().isEmpty()) {
            evidenceNotes.add("Puertos de entrada (inbound) implementados: " + portRelations.inbound().size());
        }
        if (!portRelations.outbound().isEmpty()) {
            evidenceNotes.add("Adaptadores de salida (outbound) detectados: " + portRelations.outbound().size());
        }
        if (engineeringEvidence.testFilesDetected() > 0) {
            evidenceNotes.add("Archivos de test detectados (multiplataforma): " + engineeringEvidence.testFilesDetected()
                    + " en directorios: " + engineeringEvidence.testDirectories());
        }
        if (engineeringEvidence.testScriptDetected()) {
            evidenceNotes.add("Script de test detectado en manifiesto: " + engineeringEvidence.testScript());
        }
        if (engineeringEvidence.springConfigurationDetected()) {
            evidenceNotes.add("Configuración Spring (@Configuration) detectada con " + engineeringEvidence.beanDefinitions() + " definiciones @Bean.");
        }
        if (engineeringEvidence.constructorInjectionDetected()) {
            evidenceNotes.add("Inyección de dependencias por constructor detectada.");
        }

        return ArchitectureEvidenceResult.builder()
                .structuralPaths(new ArrayList<>(structuralPaths))
                .detectedKeywords(new ArrayList<>(detectedKeywords))
                .packageComponentDistribution(packageComponentDistribution)
                .maxPathDepth(maxDepth)
                .totalStructuralPaths(structuralPaths.size())
                .evidenceNotes(evidenceNotes)
                .inboundPortImplementations(portRelations.inbound())
                .outboundAdapterImplementations(portRelations.outbound())
                .engineeringEvidence(engineeringEvidence)
                .projectKind(projectKind)
                .frontendFramework(frontendFramework)
                .frontendEvidenceNotes(frontendEvidenceNotes)
                .build();
    }

    // ── Detección de relaciones puerto→implementación ─────────────────────────

    /**
     * Contenedor interno de relaciones inbound y outbound detectadas.
     *
     * @param inbound  Clave=interfaz (UseCase/Port inbound), Valor=clase que la implementa.
     * @param outbound Clave=clase adaptador, Valor=interfaz (Port outbound) que implementa.
     */
    private record PortRelations(Map<String, String> inbound, Map<String, String> outbound) {}

    /**
     * Lee el contenido de cada archivo {@code .java} y busca declaraciones {@code implements}.
     * Clasifica la relación como <em>inbound</em> u <em>outbound</em> según la ruta del archivo,
     * usando {@link ArchitectureDetectionRules#INBOUND_PATH_SEGMENTS} y
     * {@link ArchitectureDetectionRules#OUTBOUND_PATH_SEGMENTS}.
     */
    private PortRelations detectPortRelations(ScannedFileMap scannedFiles) {
        Map<String, String> inbound = new LinkedHashMap<>();
        Map<String, String> outbound = new LinkedHashMap<>();

        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) {
            return new PortRelations(inbound, outbound);
        }

        for (Path filePath : scannedFiles.getAllFilePaths()) {
            String fileName = filePath.getFileName().toString();
            if (!fileName.endsWith(".java")) continue;

            String content;
            try {
                content = Files.readString(filePath);
            } catch (IOException e) {
                log.warn("No se pudo leer {} para detectar relaciones puerto: {}", filePath, e.getMessage());
                continue;
            }

            Matcher matcher = IMPLEMENTS_PATTERN.matcher(content);
            if (!matcher.find()) continue;

            String className = fileName.replace(".java", "");
            String pathStr = filePath.toString().replace("\\", "/").toLowerCase();
            String[] implemented = matcher.group(1).split(",");

            for (String iface : implemented) {
                String ifaceName = iface.trim().replaceAll("<.*>", "").trim();
                if (!looksLikeAPort(ifaceName)) continue;

                boolean isInbound = ArchitectureDetectionRules.INBOUND_PATH_SEGMENTS.stream()
                        .anyMatch(pathStr::contains);
                boolean isOutbound = ArchitectureDetectionRules.OUTBOUND_PATH_SEGMENTS.stream()
                        .anyMatch(pathStr::contains);

                if (isInbound && !isOutbound) {
                    inbound.put(ifaceName, className);
                } else {
                    outbound.put(className, ifaceName);
                }
            }
        }
        return new PortRelations(inbound, outbound);
    }

    /**
     * Determina si el nombre de una interfaz corresponde a un puerto hexagonal,
     * usando {@link ArchitectureDetectionRules#PORT_INTERFACE_SUFFIXES}.
     */
    private boolean looksLikeAPort(String interfaceName) {
        return ArchitectureDetectionRules.PORT_INTERFACE_SUFFIXES.stream()
                .anyMatch(interfaceName::endsWith);
    }

    // ── Detección de evidencias de ingeniería (stack-agnostic) ────────────────

    /**
     * Detecta evidencias de ingeniería multiplataforma:
     * <ul>
     *   <li>Archivos de test con patrones Java, Node.js y Python</li>
     *   <li>Directorios de test convencionales (test/, tests/, __tests__/, etc.)</li>
     *   <li>Metadata de {@code package.json}: nombre, descripción, entrada principal, script de test</li>
     *   <li>Evidencias Spring-specific: @Configuration, @Bean, inyección por constructor</li>
     * </ul>
     */
    private EngineeringEvidence detectEngineeringEvidence(ScannedFileMap scannedFiles) {
        boolean configDetected = false;
        int beanCount = 0;
        boolean constructorInjection = false;
        int testFileCount = 0;
        Set<String> foundTestDirs = new TreeSet<>();

        // Metadata de package.json
        String projectName = null;
        String projectDescription = null;
        String mainEntry = null;
        String testScript = null;
        boolean testScriptDetected = false;
        int minPackageJsonDepth = Integer.MAX_VALUE;

        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) {
            return emptyEvidence();
        }

        for (Path filePath : scannedFiles.getAllFilePaths()) {
            String fileName = filePath.getFileName().toString();
            String pathStr = filePath.toString().replace("\\", "/");
            String pathLower = pathStr.toLowerCase();

            // ── Detección de tests multiplataforma ────────────────────────────
            boolean isTestFile = isTestFile(fileName, pathLower);
            if (isTestFile) {
                testFileCount++;
                Path parent = filePath.getParent();
                if (parent != null) {
                    String parentName = parent.getFileName().toString().toLowerCase();
                    if (ArchitectureDetectionRules.TEST_DIRECTORY_NAMES.contains(parentName)) {
                        foundTestDirs.add(parentName);
                    } else {
                        for (String testDir : ArchitectureDetectionRules.TEST_DIRECTORY_NAMES) {
                            if (pathLower.contains("/" + testDir + "/") || pathLower.endsWith("/" + testDir)) {
                                foundTestDirs.add(testDir);
                                break;
                            }
                        }
                    }
                }
                continue; // Los archivos de test no contribuyen a métricas de producción
            }

            // ── Lectura de package.json (se prefiere el más cercano a la raíz en monorepos) ──
            if (fileName.equals("package.json")) {
                int depth = filePath.getNameCount();
                if (depth < minPackageJsonDepth) {
                    try {
                        String content = Files.readString(filePath);
                        projectName = extractJsonField(content, "name");
                        projectDescription = extractJsonField(content, "description");
                        mainEntry = extractJsonField(content, "main");
                        testScript = extractJsonScriptField(content, "test");
                        testScriptDetected = testScript != null && !testScript.isBlank();
                        minPackageJsonDepth = depth;
                    } catch (IOException e) {
                        log.warn("No se pudo leer package.json en {}: {}", filePath, e.getMessage());
                    }
                }
                continue;
            }

            // ── Spring/Java DI detection ───────────────────────────────────────
            if (!fileName.endsWith(".java")) continue;

            String content;
            try {
                content = Files.readString(filePath);
            } catch (IOException e) {
                log.warn("No se pudo leer {} para detectar evidencias DI: {}", filePath, e.getMessage());
                continue;
            }

            if (!configDetected && CONFIGURATION_PATTERN.matcher(content).find()) {
                configDetected = true;
            }
            Matcher beanMatcher = BEAN_PATTERN.matcher(content);
            while (beanMatcher.find()) beanCount++;

            // Solo evaluar inyección por constructor en clases gestionadas por Spring
            boolean isSpringManagedClass = ArchitectureDetectionRules.SPRING_MANAGED_ANNOTATIONS.stream()
                    .anyMatch(content::contains);

            if (isSpringManagedClass && !constructorInjection && CONSTRUCTOR_INJECTION_PATTERN.matcher(content).find()) {
                if (content.matches("(?s).*public\\s+\\w+\\s*\\([^)]+\\)\\s*\\{.*")) {
                    constructorInjection = true;
                }
            }
        }

        return new EngineeringEvidence(
                testFileCount,
                new ArrayList<>(foundTestDirs),
                testScriptDetected,
                configDetected,
                beanCount,
                constructorInjection,
                projectName,
                projectDescription,
                mainEntry,
                testScript
        );
    }

    /**
     * Determina si un archivo es un archivo de test según su nombre y ruta.
     * Cubre patrones de Java, Node.js (JS/TS/MJS) y Python,
     * usando {@link ArchitectureDetectionRules}.
     */
    private boolean isTestFile(String fileName, String pathLower) {
        // Sufijos genéricos (Java y Node)
        for (String suffix : ArchitectureDetectionRules.TEST_FILE_SUFFIXES) {
            if (fileName.endsWith(suffix)) return true;
        }
        // Python: prefijo test_ o sufijo _test.py
        if (fileName.endsWith(ArchitectureDetectionRules.PYTHON_EXTENSION)) {
            for (String prefix : ArchitectureDetectionRules.TEST_FILE_PREFIXES_PYTHON) {
                if (fileName.startsWith(prefix)) return true;
            }
            for (String suffix : ArchitectureDetectionRules.TEST_FILE_SUFFIXES_PYTHON) {
                if (fileName.endsWith(suffix)) return true;
            }
        }
        // Cualquier archivo dentro de una carpeta de test conocida
        for (String testDir : ArchitectureDetectionRules.TEST_DIRECTORY_NAMES) {
            if (pathLower.contains("/" + testDir + "/")) return true;
        }
        return false;
    }

    /**
     * Extrae el valor de un campo simple de un JSON minimamente parseado (sin dependencias externas).
     * Solo válido para valores de tipo string en la raíz del objeto.
     *
     * @param json      Contenido JSON como cadena.
     * @param fieldName Nombre del campo a extraer.
     * @return Valor del campo, o {@code null} si no se encuentra.
     */
    private String extractJsonField(String json, String fieldName) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Extrae el valor del campo {@code test} dentro de {@code scripts} en un package.json.
     */
    private String extractJsonScriptField(String json, String scriptName) {
        Pattern scriptsBlock = Pattern.compile("\"scripts\"\\s*:\\s*\\{([^}]+)\\}");
        Matcher blockMatcher = scriptsBlock.matcher(json);
        if (!blockMatcher.find()) return null;
        String scriptsContent = blockMatcher.group(1);
        Pattern field = Pattern.compile("\"" + Pattern.quote(scriptName) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher fieldMatcher = field.matcher(scriptsContent);
        return fieldMatcher.find() ? fieldMatcher.group(1) : null;
    }

    /** Devuelve una instancia de {@link EngineeringEvidence} con todos los campos vacíos/cero. */
    private EngineeringEvidence emptyEvidence() {
        return new EngineeringEvidence(0, List.of(), false, false, 0, false, null, null, null, null);
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Extrae el nombre de la capa o paquete representativo de la ruta de un componente,
     * usando {@link ArchitectureDetectionRules#LAYER_KEYWORDS}.
     */
    private String extractTopLayerPackage(String relativePath) {
        Path path = Path.of(relativePath);
        Path parent = path.getParent();
        if (parent == null) return "root";
        for (Path segment : parent) {
            String nameLower = segment.toString().toLowerCase();
            if (ArchitectureDetectionRules.LAYER_KEYWORDS.contains(nameLower)) return nameLower;
        }
        return parent.getFileName() != null ? parent.getFileName().toString() : "root";
    }

    // ── Clasificación de Proyecto y Framework Frontend ─────────────────────────

    /**
     * Clasifica el proyecto como BACKEND, FRONTEND, FULLSTACK, MOBILE o UNKNOWN.
     * <p>
     * Estrategia de resolución (por prioridad):
     * <ol>
     *   <li>Si el {@code technologyStack} ya identificó un framework conocido, se usa ese
     *       dato directamente — evita que proyectos NestJS/Express se clasifiquen como
     *       FRONTEND por tener archivos {@code .ts} / {@code .js}.</li>
     *   <li>Si no hay framework, se recurre a las heurísticas de extensión de archivo
     *       definidas en {@link ArchitectureDetectionRules}.</li>
     * </ol>
     */
    private ProjectKind determineProjectKind(ScannedFileMap scannedFiles,
                                             EngineeringEvidence engEvidence,
                                             TechnologyStack technologyStack) {
        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) {
            return ProjectKind.UNKNOWN;
        }

        // Prioridad 1: framework ya detectado por TechnologyDetectorStage
        if (technologyStack != null) {
            String framework = technologyStack.getMainFramework();
            if (framework != null) {
                if (ArchitectureDetectionRules.MOBILE_FRAMEWORKS.contains(framework))   return ProjectKind.MOBILE;
                if (ArchitectureDetectionRules.BACKEND_FRAMEWORKS.contains(framework))  return ProjectKind.BACKEND;
                if (ArchitectureDetectionRules.FRONTEND_FRAMEWORKS.contains(framework)) return ProjectKind.FRONTEND;
            }
        }

        // Prioridad 2: heurísticas de extensión y nombre de archivo
        boolean hasBackend = false;
        boolean hasFrontendFiles = false;
        boolean hasPackageJson = engEvidence != null
                && (engEvidence.projectName() != null || engEvidence.testScriptDetected());

        for (Path filePath : scannedFiles.getAllFilePaths()) {
            String fileName = filePath.getFileName().toString().toLowerCase();
            String pathLower = filePath.toString().replace("\\", "/").toLowerCase();

            // Señales de mobile (prioridad máxima — retorno inmediato)
            for (String mobileKw : ArchitectureDetectionRules.MOBILE_FILE_KEYWORDS) {
                if (fileName.contains(mobileKw)) return ProjectKind.MOBILE;
            }

            // Señales de backend
            if (!hasBackend) {
                boolean isBackendExt  = ArchitectureDetectionRules.BACKEND_FILE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
                boolean isBackendFile = ArchitectureDetectionRules.BACKEND_FILE_NAMES.contains(fileName);
                boolean isPythonBackend = (fileName.endsWith(ArchitectureDetectionRules.PYTHON_EXTENSION)
                        && ArchitectureDetectionRules.PYTHON_BACKEND_KEYWORDS.stream().anyMatch(pathLower::contains))
                        || fileName.equals(ArchitectureDetectionRules.PYTHON_REQUIREMENTS_FILE);
                if (isBackendExt || isBackendFile || isPythonBackend) hasBackend = true;
            }

            // Señales de frontend
            if (!hasFrontendFiles && !pathLower.contains("node_modules")) {
                if (ArchitectureDetectionRules.FRONTEND_FILE_EXTENSIONS.stream().anyMatch(fileName::endsWith)) {
                    hasFrontendFiles = true;
                }
            }
        }

        if (hasBackend && hasFrontendFiles) return ProjectKind.FULLSTACK;
        if (hasBackend)                     return ProjectKind.BACKEND;
        if (hasFrontendFiles || hasPackageJson) return ProjectKind.FRONTEND;
        return ProjectKind.UNKNOWN;
    }

    /**
     * Identifica el framework frontend activo iterando en orden de prioridad sobre
     * {@link ArchitectureDetectionRules#FRONTEND_FRAMEWORK_RULES}.
     * La primera regla que coincide en cualquier archivo del proyecto gana.
     */
    private FrontendFramework determineFrontendFramework(ScannedFileMap scannedFiles) {
        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) {
            return FrontendFramework.NONE;
        }

        // Estado de señales detectadas por framework (en el mismo orden que las reglas)
        List<FrontendFrameworkRule> rules = ArchitectureDetectionRules.FRONTEND_FRAMEWORK_RULES;
        boolean[] detected = new boolean[rules.size()];

        for (Path filePath : scannedFiles.getAllFilePaths()) {
            String fileName = filePath.getFileName().toString().toLowerCase();
            String pathLower = filePath.toString().replace("\\", "/").toLowerCase();

            for (int i = 0; i < rules.size(); i++) {
                if (detected[i]) continue;
                FrontendFrameworkRule rule = rules.get(i);

                boolean matches =
                        rule.fileNameEquals().stream().anyMatch(fileName::equals)
                        || rule.fileNameContains().stream().anyMatch(fileName::contains)
                        || rule.pathContains().stream().anyMatch(pathLower::contains)
                        || rule.fileExtensions().stream().anyMatch(fileName::endsWith);

                if (matches) {
                    detected[i] = true;
                    continue;
                }

                // Señales de contenido (solo en extensiones relevantes)
                if (!rule.contentSignals().isEmpty()
                        && ArchitectureDetectionRules.FRONTEND_CONTENT_SCAN_EXTENSIONS.stream().anyMatch(fileName::endsWith)) {
                    try {
                        String content = Files.readString(filePath);
                        if (rule.contentSignals().stream().anyMatch(content::contains)) {
                            detected[i] = true;
                        }
                    } catch (IOException ignored) {}
                }
            }
        }

        // Devolver el primer framework detectado (orden = prioridad)
        for (int i = 0; i < rules.size(); i++) {
            if (detected[i]) return rules.get(i).framework();
        }
        return FrontendFramework.NONE;
    }

    /**
     * Detecta evidencias de arquitectura frontend (Feature-based, Shared/Core layers,
     * Facade pattern, Signals, Standalone components, Lazy loading).
     * Usa los segmentos y patrones de {@link ArchitectureDetectionRules}.
     */
    private List<String> detectFrontendArchitectureEvidence(ScannedFileMap scannedFiles, ProjectKind projectKind) {
        List<String> notes = new ArrayList<>();
        if (projectKind == ProjectKind.BACKEND) return notes;
        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) return notes;

        Set<String> featureDirs = new TreeSet<>();
        boolean sharedLayerDetected = false;
        boolean coreLayerDetected = false;
        boolean dataAccessLayerDetected = false;
        boolean pagesDetected = false;
        boolean facadeDetected = false;
        List<String> facadeClassNames = new ArrayList<>();
        boolean signalsDetected = false;
        boolean standaloneComponentsDetected = false;
        Set<String> lazyRouteFiles = new TreeSet<>();

        for (Path filePath : scannedFiles.getAllFilePaths()) {
            String pathStr = filePath.toString().replace("\\", "/");
            String pathLower = pathStr.toLowerCase();
            String fileName = filePath.getFileName().toString();
            String fileNameLower = fileName.toLowerCase();

            // Feature-based architecture
            for (String segment : ArchitectureDetectionRules.FRONTEND_FEATURE_SEGMENTS) {
                if (pathLower.contains(segment)) {
                    int idx = pathLower.indexOf(segment);
                    String sub = pathStr.substring(idx + 1);
                    String[] parts = sub.split("/");
                    if (parts.length >= 2) featureDirs.add(parts[0] + "/" + parts[1]);
                    break;
                }
            }

            if (!sharedLayerDetected)
                sharedLayerDetected = ArchitectureDetectionRules.FRONTEND_SHARED_SEGMENTS.stream().anyMatch(pathLower::contains);
            if (!coreLayerDetected)
                coreLayerDetected = ArchitectureDetectionRules.FRONTEND_CORE_SEGMENTS.stream().anyMatch(pathLower::contains);
            if (!dataAccessLayerDetected)
                dataAccessLayerDetected = ArchitectureDetectionRules.FRONTEND_DATA_ACCESS_SEGMENTS.stream().anyMatch(pathLower::contains);
            if (!pagesDetected)
                pagesDetected = ArchitectureDetectionRules.FRONTEND_PAGE_SEGMENTS.stream().anyMatch(pathLower::contains);

            if (!facadeDetected) {
                facadeDetected = ArchitectureDetectionRules.FRONTEND_FACADE_SEGMENTS.stream().anyMatch(pathLower::contains)
                        || fileNameLower.endsWith(ArchitectureDetectionRules.FRONTEND_FACADE_FILE_SUFFIX);
            }

            // Análisis de contenido en archivos fuente frontend
            if (ArchitectureDetectionRules.FRONTEND_SOURCE_EXTENSIONS.stream().anyMatch(fileName::endsWith)) {
                try {
                    String content = Files.readString(filePath);

                    if (fileNameLower.endsWith(ArchitectureDetectionRules.FRONTEND_FACADE_FILE_SUFFIX)
                            || content.contains("Facade")) {
                        Matcher m = Pattern.compile("export\\s+class\\s+(\\w+Facade)").matcher(content);
                        if (m.find()) facadeClassNames.add(m.group(1));
                    }

                    if (!signalsDetected && (content.contains("signal(")
                            || content.contains("computed(")
                            || content.contains("asReadonly()"))) {
                        signalsDetected = true;
                    }
                    if (!standaloneComponentsDetected && (content.contains("standalone: true")
                            || content.contains("standalone:true"))) {
                        standaloneComponentsDetected = true;
                    }
                    if (content.contains("loadComponent")
                            || content.contains("loadChildren")
                            || content.contains("React.lazy")) {
                        Path relPath = scannedFiles.getRootPath() != null
                                ? scannedFiles.getRootPath().relativize(filePath)
                                : filePath;
                        lazyRouteFiles.add(relPath.toString().replace("\\", "/"));
                    }
                } catch (IOException ignored) {}
            }
        }

        // Componer notas de evidencia
        if (!featureDirs.isEmpty()) {
            notes.add("Arquitectura basada en características (Feature-based) detectada con carpetas: " + String.join(", ", featureDirs));
        }
        if (sharedLayerDetected)      notes.add("Capa o módulo compartido (shared) detectado.");
        if (coreLayerDetected)        notes.add("Capa o módulo central (core) detectado.");
        if (dataAccessLayerDetected)  notes.add("Capa de acceso a datos / servicios (data-access / services / api) separada.");
        if (pagesDetected)            notes.add("Componentes de página / contenedor (pages / views) identificados.");
        if (facadeDetected || !facadeClassNames.isEmpty()) {
            notes.add("Patrón Facade detectado"
                    + (!facadeClassNames.isEmpty() ? " (" + String.join(", ", facadeClassNames) + ")" : "")
                    + " para encapsular estado y servicios.");
        }
        if (signalsDetected)          notes.add("Uso de reactividad / Signals (signal, computed, asReadonly) detectado para gestión de estado.");
        if (standaloneComponentsDetected) notes.add("Componentes Standalone (standalone: true) detectados.");
        if (!lazyRouteFiles.isEmpty()) {
            notes.add("Carga diferida de rutas (Lazy Loading via loadComponent / loadChildren) detectada explícitamente en: "
                    + String.join(", ", lazyRouteFiles));
        }

        return notes;
    }
}
