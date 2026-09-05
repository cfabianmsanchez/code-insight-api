package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.ScannedFileMap;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Detector de Evidencias Arquitectónicas.
 * 
 * Analiza la estructura de carpetas y nombres de paquetes del proyecto para
 * recopilar
 * evidencias objetivas y señales estructurales (palabras clave como 'domain',
 * 'adapter',
 * 'port', profundidad de rutas y distribución de componentes por capa), sin
 * emitir
 * conclusiones ni clasificaciones rígidas.
 */
@Component
public class ArchitectureEvidenceDetectorStage {

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

        return ArchitectureEvidenceResult.builder()
                .structuralPaths(new ArrayList<>(structuralPaths))
                .detectedKeywords(new ArrayList<>(detectedKeywords))
                .packageComponentDistribution(packageComponentDistribution)
                .maxPathDepth(maxDepth)
                .totalStructuralPaths(structuralPaths.size())
                .evidenceNotes(evidenceNotes)
                .build();
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
