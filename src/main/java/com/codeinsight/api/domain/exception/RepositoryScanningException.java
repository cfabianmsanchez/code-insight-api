package com.codeinsight.api.domain.exception;

/**
 * Excepción lanzada cuando ocurre un fallo durante el escaneo del árbol de archivos del repositorio.
 */
public class RepositoryScanningException extends DomainException {

    public RepositoryScanningException(String message) {
        super(message);
    }

    public RepositoryScanningException(String message, Throwable cause) {
        super(message, cause);
    }
}
