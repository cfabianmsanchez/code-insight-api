package com.codeinsight.api.domain.exception;

/**
 * Excepción lanzada cuando el tipo de fuente suministrado no es soportado por ningún cargador.
 */
public class UnsupportedSourceTypeException extends DomainException {

  public UnsupportedSourceTypeException(String message) {
    super(message);
  }
}
