package com.codeinsight.api.infrastructure.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeinsight.api.infrastructure.adapter.in.rest.dto.SystemConfigResponseDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.UpdateModelRequestDto;
import com.codeinsight.api.infrastructure.adapter.out.ai.OllamaAdapter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class SystemConfigControllerTest {

  private OllamaAdapter ollamaAdapter;
  private SystemConfigController controller;

  @BeforeEach
  void setUp() {
    ollamaAdapter = new OllamaAdapter(
      RestClient.builder(),
      "http://localhost:11434",
      "qwen2.5-coder",
      "qwen2.5-coder,llama3"
    );
    controller = new SystemConfigController(ollamaAdapter, "v1");
  }

  @Test
  void getSystemConfig_shouldReturnActiveBackendConfigAndAvailableModels() {
    ResponseEntity<SystemConfigResponseDto> response =
      controller.getSystemConfig();

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals("http://localhost:11434", response.getBody().ollamaBaseUrl());
    assertEquals("qwen2.5-coder", response.getBody().ollamaModel());
    assertEquals("v1", response.getBody().promptVersion());
  }

  @Test
  void updateActiveModel_shouldUpdateModelAndReturnConfig() {
    List<String> available = ollamaAdapter.fetchAvailableModels();
    String validModel = available.isEmpty()
      ? "qwen2.5-coder"
      : available.get(0);
    ResponseEntity<SystemConfigResponseDto> response =
      controller.updateActiveModel(new UpdateModelRequestDto(validModel));

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(validModel, response.getBody().ollamaModel());
    assertEquals(validModel, ollamaAdapter.getActiveModel());
  }

  @Test
  void updateActiveModel_shouldThrowExceptionForBlankModel() {
    assertThrows(IllegalArgumentException.class, () -> {
      controller.updateActiveModel(new UpdateModelRequestDto("  "));
    });
  }
}
