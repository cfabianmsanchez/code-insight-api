package com.codeinsight.api.application.pipeline.stage;

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
 * Etapa 5 del Pipeline: Architecture Evidence Detector.
 * Recopila evidencias objetivas y señales estructurales sobre la arquitectura del proyecto
 * (rutas estructurales, distribución de componentes y palabras clave arquitectónicas)
 * sin emitir juicios ni clasificaciones rígidas.
 */
@Component
public class ArchitectureEvidenceDetectorStage {

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
            "shared", "core", "web", "view", "views"
    );

    private static final Set<String> LAYER_KEYWORDS = Set.of(
            "domain", "application", "infrastructure",
            "feature", "features",
            "core", "web",
            "service", "services",
            "repository", "repositories",
            "controller", "controllers"
    );

    public ArchitectureEvidenceResult detect(ScannedFileMap scannedFiles, ComponentAnalysisResult componentAnalysis) {
        if (scannedFiles == null) {
            throw new IllegalArgumentException("ScannedFileMap must not be null");
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
                    packageComponentDistribution.put(layerOrPackage, packageComponentDistribution.getOrDefault(layerOrPackage, 0) + 1);
                }
            }
        }

        // Generar notas de evidencia puramente factuales
        evidenceNotes.add("Identificadas " + structuralPaths.size() + " rutas estructurales distintas con profundidad máxima de " + maxDepth + " segmentos.");
        if (!detectedKeywords.isEmpty()) {
            evidenceNotes.add("Palabras clave de arquitectura encontradas en rutas: " + String.join(", ", detectedKeywords));
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
