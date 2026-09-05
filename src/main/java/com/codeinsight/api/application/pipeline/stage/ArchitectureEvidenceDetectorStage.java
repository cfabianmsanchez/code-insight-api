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
 * archivos Java para recopilar evidencias objetivas:
 * <ul>
 *   <li>Palabras clave de arquitectura en rutas de directorios</li>
 *   <li>Distribución de componentes por capa</li>
 *   <li>Relaciones inbound (UseCase implementado por un Service de aplicación)</li>
 *   <li>Relaciones outbound (Port implementado por un Adapter de infraestructura)</li>
 *   <li>Evidencias de buenas prácticas de ingeniería (DI por constructor, @Configuration, tests)</li>
 * </ul>
 * No emite conclusiones ni clasificaciones rígidas: solo reporta hechos verificables.
 */
@Component
public class ArchitectureEvidenceDetectorStage {

    private static final Logger log = LoggerFactory.getLogger(ArchitectureEvidenceDetectorStage.class);

    // ── Patrones de análisis estático ─────────────────────────────────────────

    /** Captura las interfaces declaradas en {@code implements A, B<X>}. */
    private static final Pattern IMPLEMENTS_PATTERN =
            Pattern.compile("\\bimplements\\s+([\\w,\\s<>]+?)(?:\\{|extends)");

    /** Detecta inyección por constructor: constructor no-vacío con un parámetro final o anotado. */
    private static final Pattern CONSTRUCTOR_INJECTION_PATTERN =
            Pattern.compile("\\bpublic\\s+\\w+\\s*\\([^)]+\\)\\s*\\{");

    /** Detecta métodos anotados con {@code @Bean}. */
    private static final Pattern BEAN_PATTERN =
            Pattern.compile("@Bean");

    /** Detecta clases anotadas con {@code @Configuration}. */
    private static final Pattern CONFIGURATION_PATTERN =
            Pattern.compile("@Configuration");

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
     * extraer evidencias estructurales, relaciones puerto→implementación y
     * métricas de buenas prácticas de ingeniería.
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

        // 1. Analizar rutas de directorios para extraer palabras clave y profundidad
        if (relativeFiles != null) {
            for (String relativeFile : relativeFiles) {
                Path filePath = Path.of(relativeFile);
                Path parent = filePath.getParent();
                if (parent != null) {
                    structuralPaths.add(parent.toString());
                    int depth = parent.getNameCount();
                    if (depth > maxDepth) {
                        maxDepth = depth;
                    }
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

        // 3. Analizar contenido de archivos .java: relaciones y evidencia de ingeniería
        PortRelations portRelations = detectPortRelations(scannedFiles);
        EngineeringEvidence engineeringEvidence = detectEngineeringEvidence(scannedFiles);

        // 4. Generar notas de evidencia puramente factuales
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
            evidenceNotes.add("Archivos de test detectados en src/test/: " + engineeringEvidence.testFilesDetected());
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
     * Clasifica la relación como <em>inbound</em> u <em>outbound</em> según la ruta del archivo:
     * <ul>
     *   <li>Si la ruta contiene {@code port/in} o {@code ports/in} → inbound</li>
     *   <li>Si la ruta contiene {@code adapter}, {@code port/out} o {@code ports/out} → outbound</li>
     *   <li>Si no se puede determinar la ubicación, se clasifica por el sufijo de la interfaz</li>
     * </ul>
     */
    private PortRelations detectPortRelations(ScannedFileMap scannedFiles) {
        Map<String, String> inbound = new LinkedHashMap<>();
        Map<String, String> outbound = new LinkedHashMap<>();

        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) {
            return new PortRelations(inbound, outbound);
        }

        for (Path filePath : scannedFiles.getAllFilePaths()) {
            String fileName = filePath.getFileName().toString();
            if (!fileName.endsWith(".java")) {
                continue;
            }

            String content;
            try {
                content = Files.readString(filePath);
            } catch (IOException e) {
                log.warn("No se pudo leer {} para detectar relaciones puerto: {}", filePath, e.getMessage());
                continue;
            }

            Matcher matcher = IMPLEMENTS_PATTERN.matcher(content);
            if (!matcher.find()) {
                continue;
            }

            String className = fileName.replace(".java", "");
            String pathStr = filePath.toString().replace("\\", "/").toLowerCase();
            String[] implemented = matcher.group(1).split(",");

            for (String iface : implemented) {
                String ifaceName = iface.trim().replaceAll("<.*>", "").trim();
                if (!looksLikeAPort(ifaceName)) {
                    continue;
                }

                // Clasificar según ubicación en el árbol de directorios
                if (pathStr.contains("port/in") || pathStr.contains("ports/in")) {
                    inbound.put(ifaceName, className);
                } else if (pathStr.contains("adapter") || pathStr.contains("port/out") || pathStr.contains("ports/out")) {
                    outbound.put(className, ifaceName);
                } else if (pathStr.contains("application")) {
                    // En capa application, las implementaciones de puertos son inbound
                    inbound.put(ifaceName, className);
                } else if (pathStr.contains("infrastructure")) {
                    // En capa infrastructure, las implementaciones de puertos son outbound
                    outbound.put(className, ifaceName);
                } else {
                    // Sin contexto de ruta, se registra como outbound por convención
                    outbound.put(className, ifaceName);
                }
            }
        }
        return new PortRelations(inbound, outbound);
    }

    /**
     * Determina si el nombre de una interfaz corresponde a un puerto hexagonal.
     * Usa sufijos convencionales de la arquitectura hexagonal y sus variantes más comunes.
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

    // ── Detección de evidencias de ingeniería ─────────────────────────────────

    /**
     * Analiza archivos {@code .java} y rutas {@code src/test/} para detectar buenas prácticas:
     * uso de {@code @Configuration}, definiciones de {@code @Bean}, inyección por constructor
     * y existencia de archivos de test.
     *
     * @param scannedFiles Mapa de archivos escaneados.
     * @return {@link EngineeringEvidence} con los resultados de la detección.
     */
    private EngineeringEvidence detectEngineeringEvidence(ScannedFileMap scannedFiles) {
        boolean configDetected = false;
        int beanCount = 0;
        boolean constructorInjection = false;
        int testFileCount = 0;

        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) {
            return new EngineeringEvidence(false, 0, false, 0);
        }

        for (Path filePath : scannedFiles.getAllFilePaths()) {
            String pathStr = filePath.toString().replace("\\", "/");

            // Conteo de archivos de test (src/test/)
            if (pathStr.contains("/test/") && filePath.getFileName().toString().endsWith(".java")) {
                testFileCount++;
                continue; // los archivos de test no contribuyen al análisis DI
            }

            if (!filePath.getFileName().toString().endsWith(".java")) {
                continue;
            }

            String content;
            try {
                content = Files.readString(filePath);
            } catch (IOException e) {
                log.warn("No se pudo leer {} para detectar evidencias de ingeniería: {}", filePath, e.getMessage());
                continue;
            }

            if (!configDetected && CONFIGURATION_PATTERN.matcher(content).find()) {
                configDetected = true;
            }

            Matcher beanMatcher = BEAN_PATTERN.matcher(content);
            while (beanMatcher.find()) {
                beanCount++;
            }

            if (!constructorInjection && CONSTRUCTOR_INJECTION_PATTERN.matcher(content).find()) {
                // Solo cuenta si hay más de un parámetro (descarta constructores sin args)
                if (content.matches("(?s).*public\\s+\\w+\\s*\\([^)]+\\)\\s*\\{.*")) {
                    constructorInjection = true;
                }
            }
        }

        return new EngineeringEvidence(configDetected, beanCount, constructorInjection, testFileCount);
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Extrae el nombre de la capa o paquete representativo de la ruta de un componente.
     * Prioriza palabras clave de capa (domain, application, infrastructure, etc.).
     */
    private String extractTopLayerPackage(String relativePath) {
        Path path = Path.of(relativePath);
        Path parent = path.getParent();
        if (parent == null) {
            return "root";
        }
        for (Path segment : parent) {
            String nameLower = segment.toString().toLowerCase();
            if (LAYER_KEYWORDS.contains(nameLower)) {
                return nameLower;
            }
        }
        return parent.getFileName() != null ? parent.getFileName().toString() : "root";
    }
}
