package com.codeinsight.api.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para actualizar el modelo de IA activo en el backend.
 */
public record UpdateModelRequestDto(
  @NotBlank(message = "El nombre del modelo no puede estar vacío") String model
) {}
