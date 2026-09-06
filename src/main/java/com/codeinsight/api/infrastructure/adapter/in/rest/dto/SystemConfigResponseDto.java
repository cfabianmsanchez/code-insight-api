package com.codeinsight.api.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * DTO de respuesta con la información de configuración activa del sistema backend.
 * Expone la URL base de Ollama, el modelo de IA configurado, la lista de modelos disponibles y la versión de prompts.
 */
public record SystemConfigResponseDto(
  String ollamaBaseUrl,
  String ollamaModel,
  String promptVersion,
  List<String> availableModels
) {}
