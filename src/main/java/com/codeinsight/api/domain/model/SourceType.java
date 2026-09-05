package com.codeinsight.api.domain.model;

/**
 * Enumeración que representa la fuente del código a analizar.
 */
public enum SourceType {
    /** Repositorio clonado desde una URL de GitHub. */
    GITHUB_REPO,
    /** Proyecto descomprimido desde un archivo .ZIP subido. */
    ZIP_FILE
}
