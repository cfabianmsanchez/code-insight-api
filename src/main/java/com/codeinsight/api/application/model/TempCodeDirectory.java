package com.codeinsight.api.application.model;

import com.codeinsight.api.domain.model.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TempCodeDirectory implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TempCodeDirectory.class);

    private final Path tempPath;
    private final SourceType sourceType;

    public TempCodeDirectory(Path tempPath, SourceType sourceType) {
        this.tempPath = tempPath;
        this.sourceType = sourceType;
    }

    public Path getTempPath() {
        return tempPath;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public void close() {
        if (tempPath == null || !Files.exists(tempPath)) {
            return;
        }
        try {
            Files.walkFileTree(tempPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Could not completely delete temporary directory {}: {}", tempPath, e.getMessage());
        }
    }
}
