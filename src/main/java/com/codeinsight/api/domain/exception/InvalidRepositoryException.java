package com.codeinsight.api.domain.exception;

/**
 * Excepción lanzada cuando los datos del repositorio o parámetros de análisis son inválidos.
 */
public class InvalidRepositoryException extends DomainException {

    public InvalidRepositoryException(String message) {
        super(message);
    }

    public InvalidRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
