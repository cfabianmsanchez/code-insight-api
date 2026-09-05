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

/**
 * Representa una carpeta temporal en disco donde descargamos o descomprimimos
 * el código fuente a analizar.
 *
 * Al implementar {@link AutoCloseable}, esta clase permite ser usada en bloques
 * try-with-resources para garantizar que todos los archivos temporales se eliminen
 * automáticamente del disco al finalizar el análisis.
 */
public class TempCodeDirectory implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TempCodeDirectory.class);

    /** Ruta absoluta de la carpeta temporal en el sistema de archivos. */
    private final Path tempPath;

    /** Origen del código analizado (por ejemplo: ZIP_FILE o GITHUB_REPO). */
    private final SourceType sourceType;

    /**
     * Crea un nuevo gestor de directorio temporal.
     *
     * @param tempPath   Ruta física donde se almacenó el código.
     * @param sourceType Tipo de fuente de donde se obtuvo el código.
     */
    public TempCodeDirectory(Path tempPath, SourceType sourceType) {
        this.tempPath = tempPath;
        this.sourceType = sourceType;
    }

    /**
     * Obtiene la ruta absoluta de la carpeta temporal.
     */
    public Path getTempPath() {
        return tempPath;
    }

    /**
     * Obtiene el tipo de origen del código cargado.
     */
    public SourceType getSourceType() {
        return sourceType;
    }

    /**
     * Limpia y elimina recursivamente la carpeta temporal y todo su contenido
     * al cerrar el recurso (ej. al terminar el try-with-resources).
     */
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
