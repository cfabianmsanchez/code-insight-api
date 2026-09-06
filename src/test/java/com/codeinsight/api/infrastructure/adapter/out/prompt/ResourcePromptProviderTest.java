package com.codeinsight.api.infrastructure.adapter.out.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class ResourcePromptProviderTest {

  private ResourcePromptProvider promptProvider;

  @BeforeEach
  void setUp() {
    promptProvider = new ResourcePromptProvider(
      new DefaultResourceLoader(),
      "v1"
    );
    promptProvider.init();
  }

  @Test
  void systemPrompt_shouldLoadVersionedSystemPromptTemplate() {
    String systemPrompt = promptProvider.systemPrompt();

    assertThat(systemPrompt).isNotNull();
    assertThat(systemPrompt).contains(
      "Ingeniero de Software Senior y Arquitecto de Soluciones"
    );
    assertThat(systemPrompt).contains(
      "PRINCIPIO CRÍTICO: La ausencia de una evidencia NO implica"
    );
  }

  @Test
  void analysisDirectives_shouldLoadVersionedDirectivesTemplate() {
    String directives = promptProvider.analysisDirectives();

    assertThat(directives).isNotNull();
    assertThat(directives).contains(
      "Directivas para la Síntesis Arquitectónica"
    );
    assertThat(directives).contains("0. Resumen Funcional (OBLIGATORIO)");
    assertThat(directives).contains("3. Recomendaciones Técnicas");
  }

  @Test
  void init_shouldThrowExceptionWhenPromptVersionNotFound() {
    ResourcePromptProvider invalidProvider = new ResourcePromptProvider(
      new DefaultResourceLoader(),
      "v999"
    );

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      invalidProvider::init
    );
    assertThat(exception.getMessage()).contains(
      "No se encontró el artefacto de prompt"
    );
  }
}
