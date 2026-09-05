package com.codeinsight.api.domain.exception;

/**
 * Excepción lanzada cuando ocurre un error al adquirir o extraer el código del repositorio.
 */
public class RepositoryFetchException extends DomainException {

    public RepositoryFetchException(String message) {
        super(message);
    }

    public RepositoryFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
