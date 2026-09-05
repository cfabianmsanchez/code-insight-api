package com.codeinsight.api.infrastructure.adapter.out.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaAdapterTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void synthesize_shouldReturnModelResponseOnSuccess() {
        String jsonResponse = """
                {
                  "model": "qwen2.5-coder",
                  "message": {
                    "role": "assistant",
                    "content": "### Análisis de Arquitectura\\n- Patrón: Hexagonal"
                  },
                  "done": true
                }
                """;

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        OllamaAdapter adapter = new OllamaAdapter(restClientBuilder, "http://localhost:11434", "qwen2.5-coder");

        String result = adapter.synthesize("System prompt", "User prompt");

        assertTrue(result.contains("### Análisis de Arquitectura"));
        assertTrue(result.contains("Hexagonal"));
    }

    @Test
    void synthesize_shouldReturnFallbackReportOnServerErrorOrConnectionFailure() {
        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withServerError());

        OllamaAdapter adapter = new OllamaAdapter(restClientBuilder, "http://localhost:11434", "qwen2.5-coder");

        String result = adapter.synthesize("System prompt", "User prompt");

        assertTrue(result.contains("Síntesis de IA No Disponible (Modo de Respaldo)"));
        assertTrue(result.contains("ollama serve"));
    }
}
