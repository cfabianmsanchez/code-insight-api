package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.ComponentType;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Detector de Componentes de Software.
 * 
 * Inspecciona el contenido de los archivos de código fuente (.java, .ts) para
 * identificar
 * y clasificar sus componentes según estereotipos de framework (Controladores,
 * Servicios,
 * Repositorios, Entidades, Beans de Configuración y componentes Frontend).
 */
@Component
public class ComponentDetectorStage {

    private static final Logger log = LoggerFactory.getLogger(ComponentDetectorStage.class);

    /**
     * Recorre los archivos escaneados del proyecto e identifica los componentes de
     * código fuente.
     *
     * @param scannedFiles Mapa de archivos escaneados.
     * @return {@link ComponentAnalysisResult} con la lista de componentes
     *         detectados y sus conteos.
     */
    public ComponentAnalysisResult detect(ScannedFileMap scannedFiles) {
        if (scannedFiles == null || scannedFiles.getRootPath() == null) {
            throw new InvalidRepositoryException("ScannedFileMap and root path must not be null");
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
                        } catch (IOException e) {
                            log.warn("Could not read file for component detection {}: {}", relativePath, e.getMessage());
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

    /**
     * Determina si la ruta corresponde a un archivo de código fuente analizable
     * (.java, .ts),
     * filtrando carpetas de pruebas (src/test/) y archivos unitarios (*Test.java).
     */
    private boolean isSourceCodeFile(String relativePath) {
        String lower = relativePath.toLowerCase();
        if (lower.contains("src/test/") || lower.contains("/test/") || lower.endsWith("test.java")
                || lower.endsWith("test.ts")) {
            return false;
        }
        return lower.endsWith(".java") || lower.endsWith(".ts");
    }

    /**
     * Analiza el texto del archivo para inferir el tipo de componente según
     * anotaciones
     * de Spring Boot (ej. @RestController, @Service) o Angular
     * (ej. @Component, @Injectable).
     */
    private ComponentType detectComponentType(String relativePath, String content) {
        String lowerPath = relativePath.toLowerCase();

        if (lowerPath.endsWith(".java")) {
            if (hasAnnotation(content, "RestController") || hasAnnotation(content, "Controller")) {
                return ComponentType.CONTROLLER;
            }
            if (hasAnnotation(content, "Service")) {
                return ComponentType.SERVICE;
            }
            if (hasAnnotation(content, "Repository") || hasInterfaceExtension(content, "JpaRepository")
                    || hasInterfaceExtension(content, "CrudRepository")) {
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

    /**
     * Verifica si una anotación está presente en el código usando límites de
     * palabra (\\b)
     * para evitar falsos positivos (por ejemplo, evitar
     * confundir @RestControllerAdvice con @RestController).
     */
    private boolean hasAnnotation(String content, String annotation) {
        return Pattern.compile("@" + Pattern.quote(annotation) + "\\b").matcher(content).find();
    }

    /**
     * Verifica si el código contiene la herencia explícita de una interfaz (ej.
     * extends JpaRepository).
     */
    private boolean hasInterfaceExtension(String content, String interfaceName) {
        return Pattern.compile("\\bextends\\s+.*" + Pattern.quote(interfaceName) + "\\b").matcher(content).find();
    }

    /**
     * Extrae el nombre simple de la clase o componente removiendo la extensión del
     * archivo.
     */
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
