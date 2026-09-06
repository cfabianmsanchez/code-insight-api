package com.codeinsight.api.infrastructure.adapter.out.ai.dto;

/**
 * Registro (Record) DTO de respuesta recibido desde el endpoint /api/chat de Ollama.
 *
 * @param model   Nombre del modelo que generó la respuesta.
 * @param message Mensaje de respuesta devuelto por el modelo (rol "assistant").
 * @param done    Indica si la generación ha concluido.
 */
public record OllamaChatResponse(
  String model,
  OllamaMessage message,
  boolean done
) {}
