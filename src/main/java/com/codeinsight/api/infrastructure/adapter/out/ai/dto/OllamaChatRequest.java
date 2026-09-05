package com.codeinsight.api.infrastructure.adapter.out.ai.dto;

import java.util.List;

/**
 * Registro (Record) DTO de solicitud enviado al endpoint /api/chat de Ollama.
 *
 * @param model    Nombre del modelo configurado (ejemplo: "qwen2.5-coder").
 * @param messages Lista de mensajes estructurados (system, user).
 * @param stream   Booleano para habilitar o deshabilitar streaming (fijado en false).
 */
public record OllamaChatRequest(
        String model,
        List<OllamaMessage> messages,
        boolean stream
) {}
