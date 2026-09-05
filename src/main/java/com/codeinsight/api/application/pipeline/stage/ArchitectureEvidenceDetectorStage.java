package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
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
 * Detector de Evidencias Arquitectónicas.
 *
 * Analiza la estructura de carpetas, nombres de paquetes y el contenido de los
 * archivos Java para recopilar evidencias objetivas: palabras clave de arquitectura,
 * distribución de componentes por capa y relaciones detectadas puerto→adaptador
 * (clases que implementan interfaces de tipo puerto).
 */
@Component
public class ArchitectureEvidenceDetectorStage {

    private static final Logger log = LoggerFactory.getLogger(ArchitectureEvidenceDetectorStage.class);

    /** Patrón para capturar la lista de interfaces en una declaración Java: {@code implements A, B}. */
    private static final Pattern IMPLEMENTS_PATTERN =
            Pattern.compile("\\bimplements\\s+([\\w,\\s<>]+?)(?:\\{|extends)");

    /**
     * Conjunto de palabras clave de arquitectura buscadas en los nombres de
     * directorios.
     */
    private static final Set<String> KNOWN_ARCHITECTURAL_KEYWORDS = Set.of(
            "domain", "application", "infrastructure",
            "port", "ports",
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

    /**
     * Palabras clave prioritarias para identificar la capa o paquete principal de
     * un componente.
     */
    private static final Set<String> LAYER_KEYWORDS = Set.of(
            "domain", "application", "infrastructure",
            "feature", "features",
            "core", "web",
            "service", "services",
            "repository", "repositories",
            "controller", "controllers");

    /**
     * Inspecciona las rutas de archivos escaneados y componentes identificados para
     * extraer
     * evidencias estructurales y métricas factuales sobre la arquitectura del
     * repositorio.
     *
     * @param scannedFiles      Mapa de archivos escaneados
     * @param componentAnalysis Resultado del análisis de componentes
     * @return {@link ArchitectureEvidenceResult} con la distribución de paquetes,
     *         palabras clave y métricas.
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

        if (relativeFiles != null) {
            for (String relativeFile : relativeFiles) {
                Path filePath = Path.of(relativeFile);
                Path parent = filePath.getParent();
                if (parent != null) {
                    String parentPathStr = parent.toString();
                    structuralPaths.add(parentPathStr);

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

        if (componentAnalysis != null && componentAnalysis.getComponents() != null) {
            for (DetectedComponent component : componentAnalysis.getComponents()) {
                String componentPath = component.getRelativePath();
                if (componentPath != null) {
                    String layerOrPackage = extractTopLayerPackage(componentPath);
                    packageComponentDistribution.put(layerOrPackage,
                            packageComponentDistribution.getOrDefault(layerOrPackage, 0) + 1);
                }
            }
        }

        /* Detectar relaciones puerto→adaptador leyendo el contenido de archivos .java */
        Map<String, String> portAdapterRelations = detectPortAdapterRelations(scannedFiles);

        /* Generar notas de evidencia puramente factuales */
        evidenceNotes.add("Identificadas " + structuralPaths.size()
                + " rutas estructurales distintas con profundidad máxima de " + maxDepth + " segmentos.");
        if (!detectedKeywords.isEmpty()) {
            evidenceNotes
                    .add("Palabras clave de arquitectura encontradas en rutas: " + String.join(", ", detectedKeywords));
        }
        if (!packageComponentDistribution.isEmpty()) {
            evidenceNotes.add("Distribución de componentes por paquete/capa: " + packageComponentDistribution);
        }
        if (!portAdapterRelations.isEmpty()) {
            evidenceNotes.add("Relaciones puerto→adaptador detectadas: " + portAdapterRelations.size());
        }

        return ArchitectureEvidenceResult.builder()
                .structuralPaths(new ArrayList<>(structuralPaths))
                .detectedKeywords(new ArrayList<>(detectedKeywords))
                .packageComponentDistribution(packageComponentDistribution)
                .maxPathDepth(maxDepth)
                .totalStructuralPaths(structuralPaths.size())
                .evidenceNotes(evidenceNotes)
                .portAdapterRelations(portAdapterRelations)
                .build();
    }

    /**
     * Escanea los archivos .java del repositorio buscando declaraciones {@code implements NombreInterfaz}.
     * Si el nombre de la interfaz termina con una de las terminaciones típicas de puertos (Port, UseCase,
     * Repository, Gateway, etc.), se registra como relación detectada.
     *
     * @param scannedFiles Mapa de archivos escaneados con sus rutas absolutas.
     * @return Mapa (adaptador → puerto) con las relaciones encontradas.
     */
    private Map<String, String> detectPortAdapterRelations(ScannedFileMap scannedFiles) {
        Map<String, String> relations = new LinkedHashMap<>();
        if (scannedFiles == null || scannedFiles.getAllFilePaths() == null) {
            return relations;
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
                log.warn("No se pudo leer el archivo {} para detectar relaciones puerto→adaptador: {}", filePath, e.getMessage());
                continue;
            }

            Matcher matcher = IMPLEMENTS_PATTERN.matcher(content);
            if (!matcher.find()) {
                continue;
            }

            String[] implemented = matcher.group(1).split(",");
            String adapterName = fileName.replace(".java", "");

            for (String iface : implemented) {
                String ifaceName = iface.trim().replaceAll("<.*>", ""); // quitar generics
                if (looksLikeAPort(ifaceName)) {
                    relations.put(adapterName, ifaceName);
                }
            }
        }
        return relations;
    }

    /**
     * Determina si el nombre de una interfaz corresponde a un puerto en el sentido de arquitectura hexagonal.
     * Se consideran puertos los nombres que terminan con sufijos convencionales como Port, UseCase,
     * Repository, Gateway, Service, Facade, Handler o Client.
     *
     * @param interfaceName Nombre simple de la interfaz.
     * @return {@code true} si el nombre sugiere un puerto; {@code false} en caso contrario.
     */
    private boolean looksLikeAPort(String interfaceName) {
        return interfaceName.endsWith("Port")
                || interfaceName.endsWith("UseCase")
                || interfaceName.endsWith("Repository")
                || interfaceName.endsWith("Gateway")
                || interfaceName.endsWith("Service")
                || interfaceName.endsWith("Facade")
                || interfaceName.endsWith("Handler")
                || interfaceName.endsWith("Client");
    }

    /**
     * Extrae el nombre de la capa o paquete representativo a partir de la ruta
     * relativa de un archivo.
     * Busca primero coincidencias con palabras clave de capa (ej. domain,
     * application, infrastructure).
     *
     * @param relativePath Ruta relativa del archivo.
     * @return Nombre de la capa o directorio padre relevante.
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
