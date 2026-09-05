package com.codeinsight.api.infrastructure.adapter.out.ai.dto;

/**
 * Registro (Record) que representa un mensaje individual para la API de Ollama (/api/chat).
 *
 * @param role    Rol del emisor ("system", "user", "assistant").
 * @param content Contenido de texto del mensaje.
 */
public record OllamaMessage(
        String role,
        String content
) {}
