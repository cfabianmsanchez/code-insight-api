package com.codeinsight.api.infrastructure.adapter.in.rest;

import com.codeinsight.api.application.port.out.AiModelManagementPort;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.SystemConfigResponseDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.UpdateModelRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para exponer y actualizar la configuración del sistema en tiempo de ejecución.
 * Permite a los clientes (ej. frontend Angular) consultar el modelo de IA activo, la lista de modelos disponibles
 * y cambiar dinámicamente el modelo que utilizará Ollama para los análisis.
 *
 * Utiliza el puerto {@link AiModelManagementPort} respetando estrictamente la Arquitectura Hexagonal.
 */
@RestController
@RequestMapping("/api/v1/config")
@CrossOrigin(origins = "*")
@Tag(
  name = "System Configuration",
  description = "Endpoint for inspecting and updating runtime backend configuration"
)
public class SystemConfigController {

  private final AiModelManagementPort modelManagementPort;
  private final String promptVersion;

  public SystemConfigController(
    AiModelManagementPort modelManagementPort,
    @Value("${ai.prompts.architecture-version:v1}") String promptVersion
  ) {
    this.modelManagementPort = modelManagementPort;
    this.promptVersion = promptVersion;
  }

  /**
   * Devuelve la configuración activa del sistema, incluyendo los modelos disponibles.
   */
  @GetMapping
  @Operation(
    summary = "Get active system configuration",
    description = "Returns active Ollama LLM model name, available models, base URL, and prompt version."
  )
  public ResponseEntity<SystemConfigResponseDto> getSystemConfig() {
    return ResponseEntity.ok(
      new SystemConfigResponseDto(
        modelManagementPort.getBaseUrl(),
        modelManagementPort.getActiveModel(),
        promptVersion,
        modelManagementPort.fetchAvailableModels()
      )
    );
  }

  /**
   * Actualiza en caliente el modelo de IA que utilizará Ollama para los análisis.
   */
  @PutMapping("/model")
  @Operation(
    summary = "Update active LLM model",
    description = "Updates the active Ollama LLM model used for architecture synthesis."
  )
  public ResponseEntity<SystemConfigResponseDto> updateActiveModel(
    @Valid @RequestBody UpdateModelRequestDto request
  ) {
    modelManagementPort.setActiveModel(request.model());
    return ResponseEntity.ok(
      new SystemConfigResponseDto(
        modelManagementPort.getBaseUrl(),
        modelManagementPort.getActiveModel(),
        promptVersion,
        modelManagementPort.fetchAvailableModels()
      )
    );
  }
}
