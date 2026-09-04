package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.model.ScannedFileMap;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Etapa 2 del Pipeline: File Scanner.
 * Recorre el árbol de carpetas efímeras, filtra directorios de ruido (.git, node_modules, target, etc.)
 * y recopila métricas de archivos, extensiones y manifiestos del proyecto.
 */
@Component
public class FileScannerStage {

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git", "node_modules", "target", "build", ".gradle",
            ".idea", ".vscode", "dist", "bin", ".mvn",
            "venv", "__pycache__", "coverage", "__MACOSX"
    );

    // Corregido: Todas las cadenas están en minúsculas para coincidir exactamente con fileName.toLowerCase()
    private static final Set<String> MANIFEST_FILENAMES = Set.of(
            "pom.xml", "build.gradle", "build.gradle.kts", "package.json",
            "requirements.txt", "pipfile", "pyproject.toml", "dockerfile",
            "docker-compose.yml", "docker-compose.yaml", "application.yml", "application.properties"
    );

    public ScannedFileMap scan(Path rootPath) {
        if (rootPath == null || !Files.exists(rootPath)) {
            throw new IllegalArgumentException("Root path for file scanning must exist");
        }

        List<String> relativePaths = new ArrayList<>();
        List<Path> manifestFiles = new ArrayList<>();
        Map<String, Integer> extensionCounts = new HashMap<>();
        int[] counts = new int[2]; // counts[0] = files, counts[1] = dirs

        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                    if (IGNORED_DIRECTORIES.contains(dirName)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    if (!dir.equals(rootPath)) {
                        counts[1]++;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    if (fileName.startsWith("._") 
                            || fileName.equalsIgnoreCase("Thumbs.db") 
                            || fileName.equalsIgnoreCase("desktop.ini") 
                            || fileName.equalsIgnoreCase(".DS_Store")) {
                        return FileVisitResult.CONTINUE;
                    }

                    counts[0]++;
                    Path relative = rootPath.relativize(file);
                    String relString = relative.toString();
                    relativePaths.add(relString);

                    if (MANIFEST_FILENAMES.contains(fileName.toLowerCase())) {
                        manifestFiles.add(file);
                    }

                    String ext = extractExtension(fileName);
                    if (!ext.isEmpty()) {
                        extensionCounts.put(ext, extensionCounts.getOrDefault(ext, 0) + 1);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error scanning repository files: " + e.getMessage(), e);
        }

        return ScannedFileMap.builder()
                .rootPath(rootPath)
                .totalFiles(counts[0])
                .totalDirectories(counts[1])
                .extensionCounts(extensionCounts)
                .relativeFilePaths(relativePaths)
                .manifestFiles(manifestFiles)
                .build();
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }
}
