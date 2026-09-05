package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.port.out.ArchitectureSynthesisPort;
import com.codeinsight.api.domain.model.AnalysisContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OllamaAnalysisStageTest {

    @Test
    void analyze_shouldInvokeSynthesisPortWithPrompts() {
        ArchitectureSynthesisPort mockPort = (systemPrompt, userPrompt) ->
                "Report: System=" + systemPrompt + ", User=" + userPrompt;

        OllamaAnalysisStage stage = new OllamaAnalysisStage(mockPort);

        AnalysisContext context = AnalysisContext.builder()
                .systemPrompt("System prompt text")
                .userPrompt("User prompt text")
                .formattedContextPrompt("Full formatted context")
                .build();

        String result = stage.analyze(context);

        assertNotNull(result);
        assertEquals("Report: System=System prompt text, User=User prompt text", result);
    }

    @Test
    void analyze_shouldReturnDefaultMessageWhenContextIsNull() {
        ArchitectureSynthesisPort mockPort = (systemPrompt, userPrompt) -> "Should not be called";
        OllamaAnalysisStage stage = new OllamaAnalysisStage(mockPort);

        String result = stage.analyze(null);

        assertEquals("No se proporcionó contexto para la síntesis de arquitectura.", result);
    }
}
