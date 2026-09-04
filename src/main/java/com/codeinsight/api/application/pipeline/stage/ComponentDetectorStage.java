package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.ComponentType;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.ScannedFileMap;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Etapa 4 del Pipeline: Component Detector.
 * Inspecciona el contenido de los archivos de código fuente (.java, .ts) para identificar
 * y clasificar los componentes arquitectónicos clave (Controladores, Servicios, Repositorios,
 * Entidades, Beans de Configuración y Componentes Frontend).
 */
@Component
public class ComponentDetectorStage {

    public ComponentAnalysisResult detect(ScannedFileMap scannedFiles) {
        if (scannedFiles == null || scannedFiles.getRootPath() == null) {
            throw new IllegalArgumentException("ScannedFileMap and root path must not be null");
        }

        List<DetectedComponent> detectedComponents = new ArrayList<>();
        Map<String, Integer> componentCounts = new HashMap<>();

        Path rootPath = scannedFiles.getRootPath();
        List<String> relativePaths = scannedFiles.getRelativeFilePaths();

        if (relativePaths != null) {
            for (String relativePath : relativePaths) {
                if (isSourceCodeFile(relativePath)) {
                    Path absolutePath = rootPath.resolve(relativePath);
                    if (Files.exists(absolutePath) && Files.isRegularFile(absolutePath)) {
                        try {
                            String content = Files.readString(absolutePath);
                            ComponentType type = detectComponentType(relativePath, content);
                            if (type != null) {
                                String className = extractClassName(relativePath);
                                DetectedComponent component = new DetectedComponent(className, type, relativePath);
                                detectedComponents.add(component);

                                String category = type.name();
                                componentCounts.put(category, componentCounts.getOrDefault(category, 0) + 1);
                            }
                        } catch (IOException ignored) {
                            // Ignorar lectura de archivos no legibles como texto binario
                        }
                    }
                }
            }
        }

        return ComponentAnalysisResult.builder()
                .totalComponents(detectedComponents.size())
                .componentCounts(componentCounts)
                .components(detectedComponents)
                .build();
    }

    private boolean isSourceCodeFile(String relativePath) {
        String lower = relativePath.toLowerCase();
        if (lower.contains("src/test/") || lower.contains("/test/") || lower.endsWith("test.java") || lower.endsWith("test.ts")) {
            return false;
        }
        return lower.endsWith(".java") || lower.endsWith(".ts");
    }

    private ComponentType detectComponentType(String relativePath, String content) {
        String lowerPath = relativePath.toLowerCase();

        if (lowerPath.endsWith(".java")) {
            if (hasAnnotation(content, "RestController") || hasAnnotation(content, "Controller")) {
                return ComponentType.CONTROLLER;
            }
            if (hasAnnotation(content, "Service")) {
                return ComponentType.SERVICE;
            }
            if (hasAnnotation(content, "Repository") || hasInterfaceExtension(content, "JpaRepository") || hasInterfaceExtension(content, "CrudRepository")) {
                return ComponentType.REPOSITORY;
            }
            if (hasAnnotation(content, "Entity") || hasAnnotation(content, "Table")) {
                return ComponentType.ENTITY;
            }
            if (hasAnnotation(content, "Configuration")) {
                return ComponentType.CONFIGURATION;
            }
            if (hasAnnotation(content, "Component")) {
                return ComponentType.COMPONENT;
            }
        } else if (lowerPath.endsWith(".ts")) {
            if (hasAnnotation(content, "Component")) {
                return ComponentType.FRONTEND_COMPONENT;
            }
            if (hasAnnotation(content, "Injectable")) {
                return ComponentType.FRONTEND_SERVICE;
            }
        }

        return null;
    }

    private boolean hasAnnotation(String content, String annotation) {
        return Pattern.compile("@" + Pattern.quote(annotation) + "\\b").matcher(content).find();
    }

    private boolean hasInterfaceExtension(String content, String interfaceName) {
        return Pattern.compile("\\bextends\\s+.*" + Pattern.quote(interfaceName) + "\\b").matcher(content).find();
    }

    private String extractClassName(String relativePath) {
        Path path = Path.of(relativePath);
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
}
