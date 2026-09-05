package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult.EngineeringEvidence;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.ScannedFileMap;
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
 */
@Component
public class ArchitectureEvidenceDetectorStage {

    private static final Logger log = LoggerFactory.getLogger(ArchitectureEvidenceDetectorStage.class);

    // ── Patrones de análisis estático Java ───────────────────────────────────

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

    // ── Patrones de archivos de test multiplataforma ──────────────────────────

    /**
     * Extensiones y sufijos que identifican archivos de test en cualquier stack.
     * Java: *Test.java, *Tests.java, *Spec.java
     * Node: *.test.js/ts, *.spec.js/ts
     * Python: test_*.py, *_test.py
     */
    private static final Set<String> TEST_FILE_SUFFIXES = Set.of(
            "Test.java", "Tests.java", "Spec.java",
            ".test.js", ".test.ts", ".spec.js", ".spec.ts",
            ".test.mjs", ".spec.mjs"
    );

    /** Directorios que convencionalmente contienen tests en distintos stacks. */
    private static final Set<String> TEST_DIRECTORY_NAMES = Set.of(
            "test", "tests", "__tests__", "spec", "specs",
            "src/test", "test/unit", "test/integration",
            "e2e", "cypress", "jest"
    );

    // ── Palabras clave de arquitectura ────────────────────────────────────────

    private static final Set<String> KNOWN_ARCHITECTURAL_KEYWORDS = Set.of(
            "domain", "application", "infrastructure",
            "port", "ports", "in", "out",
            "adapter", "adapters",
            "usecase", "usecases",
            "controller", "controllers",
            "service", "services",
            "repository", "repositories",
            "entity", "entities",
            "model", "models",
            "dto", "config", "configuration",
            "feature", "features",
            "component", "components",
            "mapper", "mappers",
            "exception", "exceptions",
            "shared", "core", "web", "view", "views");

    private static final Set<String> LAYER_KEYWORDS = Set.of(
            "domain", "application", "infrastructure",
            "feature", "features",
            "core", "web",
            "service", "services",
            "repository", "repositories",
            "controller", "controllers");

    // ── Método principal ──────────────────────────────────────────────────────

    /**
     * Inspecciona las rutas de archivos escaneados y componentes identificados para
     * extraer evidencias estructurales, relaciones puerto→implementación,
     * métricas de testing genéricas y metadata del proyecto.
     *
     * @param scannedFiles      Mapa de archivos escaneados (Etapa 2).
     * @param componentAnalysis Resultado del análisis de componentes (Etapa 4).
     * @return {@link ArchitectureEvidenceResult} con todas las evidencias recopiladas.
     */
    public ArchitectureEvidenceResult detect(ScannedFileMap scannedFiles, ComponentAnalysisResult componentAnalysis) {
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
                        if (KNOWN_ARCHITECTURAL_KEYWORDS.contains(nameLower)) {
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

        // 5. Generar notas de evidencia puramente factuales
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
     * Clasifica la relación como <em>inbound</em> u <em>outbound</em> según la ruta del archivo.
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

                if (pathStr.contains("port/in") || pathStr.contains("ports/in")) {
                    inbound.put(ifaceName, className);
                } else if (pathStr.contains("adapter") || pathStr.contains("port/out") || pathStr.contains("ports/out")) {
                    outbound.put(className, ifaceName);
                } else if (pathStr.contains("application")) {
                    inbound.put(ifaceName, className);
                } else if (pathStr.contains("infrastructure")) {
                    outbound.put(className, ifaceName);
                } else {
                    outbound.put(className, ifaceName);
                }
            }
        }
        return new PortRelations(inbound, outbound);
    }

    /**
     * Determina si el nombre de una interfaz corresponde a un puerto hexagonal.
     */
    private boolean looksLikeAPort(String interfaceName) {
        return interfaceName.endsWith("Port")
                || interfaceName.endsWith("UseCase")
                || interfaceName.endsWith("Repository")
                || interfaceName.endsWith("Gateway")
                || interfaceName.endsWith("Facade")
                || interfaceName.endsWith("Handler")
                || interfaceName.endsWith("Client");
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
                // Registrar el directorio de test
                Path parent = filePath.getParent();
                if (parent != null) {
                    String parentName = parent.getFileName().toString().toLowerCase();
                    if (TEST_DIRECTORY_NAMES.contains(parentName)) {
                        foundTestDirs.add(parentName);
                    } else {
                        // Buscar en la ruta completa un segmento que sea directorio de test
                        for (String testDir : TEST_DIRECTORY_NAMES) {
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

            // Solo evaluar inyección por constructor si la clase tiene estereotipos de componente Spring
            boolean isSpringManagedClass = content.contains("@Component")
                    || content.contains("@Service")
                    || content.contains("@Repository")
                    || content.contains("@RestController")
                    || content.contains("@Controller")
                    || content.contains("@Configuration");

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
     * Cubre patrones de Java, Node.js (JS/TS/MJS) y Python.
     */
    private boolean isTestFile(String fileName, String pathLower) {
        // Sufijos Node/TS
        for (String suffix : TEST_FILE_SUFFIXES) {
            if (fileName.endsWith(suffix)) return true;
        }
        // Java: *Test.java, *Tests.java
        if (fileName.endsWith("Test.java") || fileName.endsWith("Tests.java") || fileName.endsWith("Spec.java")) {
            return true;
        }
        // Python: test_*.py, *_test.py
        if (fileName.endsWith(".py") && (fileName.startsWith("test_") || fileName.endsWith("_test.py"))) {
            return true;
        }
        // Cualquier archivo dentro de una carpeta de test conocida
        for (String testDir : TEST_DIRECTORY_NAMES) {
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
        // Busca dentro del bloque "scripts": { ... }
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
     * Extrae el nombre de la capa o paquete representativo de la ruta de un componente.
     */
    private String extractTopLayerPackage(String relativePath) {
        Path path = Path.of(relativePath);
        Path parent = path.getParent();
        if (parent == null) return "root";
        for (Path segment : parent) {
            String nameLower = segment.toString().toLowerCase();
            if (LAYER_KEYWORDS.contains(nameLower)) return nameLower;
        }
        return parent.getFileName() != null ? parent.getFileName().toString() : "root";
    }
}
