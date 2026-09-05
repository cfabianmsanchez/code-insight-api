package com.codeinsight.api.domain.exception;

/**
 * Excepción base abstracta para todas las excepciones del dominio del sistema.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
