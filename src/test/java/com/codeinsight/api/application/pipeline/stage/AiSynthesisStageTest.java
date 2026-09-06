package com.codeinsight.api.application.pipeline.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.codeinsight.api.application.port.out.ArchitectureSynthesisPort;
import com.codeinsight.api.domain.model.AnalysisContext;
import org.junit.jupiter.api.Test;

class AiSynthesisStageTest {

  private ArchitectureSynthesisPort mockPort(String report) {
    return new ArchitectureSynthesisPort() {
      @Override
      public String synthesize(String systemPrompt, String userPrompt) {
        return report != null
          ? report
          : "Report: System=" + systemPrompt + ", User=" + userPrompt;
      }

      @Override
      public String getActiveModel() {
        return "mock-model";
      }
    };
  }

  @Test
  void analyze_shouldInvokeSynthesisPortWithPrompts() {
    ArchitectureSynthesisPort port = new ArchitectureSynthesisPort() {
      @Override
      public String synthesize(String systemPrompt, String userPrompt) {
        return "Report: System=" + systemPrompt + ", User=" + userPrompt;
      }

      @Override
      public String getActiveModel() {
        return "mock-model";
      }
    };

    AiSynthesisStage stage = new AiSynthesisStage(port);

    AnalysisContext context = AnalysisContext.builder()
      .systemPrompt("System prompt text")
      .userPrompt("User prompt text")
      .formattedContextPrompt("Full formatted context")
      .build();

    String result = stage.analyze(context);

    assertNotNull(result);
    assertEquals(
      "Report: System=System prompt text, User=User prompt text",
      result
    );
  }

  @Test
  void analyze_shouldReturnDefaultMessageWhenContextIsNull() {
    AiSynthesisStage stage = new AiSynthesisStage(
      mockPort("Should not be called")
    );

    String result = stage.analyze(null);

    assertEquals(
      "No se proporcionó contexto para la síntesis de arquitectura.",
      result
    );
  }

  @Test
  void getActiveModel_shouldDelegateToPort() {
    AiSynthesisStage stage = new AiSynthesisStage(mockPort(null));

    assertEquals("mock-model", stage.getActiveModel());
  }
}
