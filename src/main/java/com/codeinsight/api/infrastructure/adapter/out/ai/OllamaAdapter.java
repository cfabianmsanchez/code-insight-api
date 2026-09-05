package com.codeinsight.api.infrastructure.adapter.out.ai;

import com.codeinsight.api.application.port.out.ArchitectureSynthesisPort;
import com.codeinsight.api.infrastructure.adapter.out.ai.dto.OllamaChatRequest;
import com.codeinsight.api.infrastructure.adapter.out.ai.dto.OllamaChatResponse;
import com.codeinsight.api.infrastructure.adapter.out.ai.dto.OllamaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Adaptador de Salida (Outbound Adapter): Cliente HTTP REST para Ollama.
 * 
 * Implementa {@link ArchitectureSynthesisPort} conectándose vía REST con la API local de Ollama
 * (`/api/chat`) mediante {@link RestClient} para sintetizar el informe de arquitectura.
 */
@Component
public class OllamaAdapter implements ArchitectureSynthesisPort {

    private static final Logger log = LoggerFactory.getLogger(OllamaAdapter.class);

    private final RestClient restClient;
    private final String model;
    private final String baseUrl;

    /**
     * Construye una nueva instancia del adaptador de Ollama utilizando {@link RestClient.Builder}.
     *
     * @param builder Builder inyectado por Spring Boot para instanciar RestClient.
     * @param baseUrl URL base de la API HTTP de Ollama (ej. http://localhost:11434).
     * @param model   Nombre del modelo de IA configurado (ej. qwen2.5-coder).
     */
    public OllamaAdapter(RestClient.Builder builder,
                          @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
                          @Value("${ollama.model:qwen2.5-coder}") String model) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Envía los prompts a la API de Ollama y retorna la síntesis generada por la IA.
     * Si el servidor de Ollama no responde o no está disponible, retorna un informe de respaldo.
     *
     * @param systemPrompt Directivas del sistema y rol del evaluador.
     * @param userPrompt   Radiografía factual y evidencias determinísticas del repositorio.
     * @return Texto en formato Markdown con el análisis de síntesis o el reporte de respaldo.
     */
    @Override
    public String synthesize(String systemPrompt, String userPrompt) {
        try {
            OllamaChatRequest request = new OllamaChatRequest(
                    model,
                    List.of(
                            new OllamaMessage("system", systemPrompt),
                            new OllamaMessage("user", userPrompt)
                    ),
                    false
            );

            log.info("Enviando prompt de análisis a Ollama en {} con el modelo {}", baseUrl, model);

            OllamaChatResponse response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaChatResponse.class);

            if (response != null && response.message() != null && response.message().content() != null) {
                log.info("Síntesis arquitectónica generada exitosamente por Ollama.");
                return response.message().content();
            }

            log.warn("Respuesta nula o sin contenido recibida de Ollama. Se recurre al reporte de respaldo.");
            return buildFallbackReport("Respuesta vacía o nula del servidor Ollama.");
        } catch (Exception e) {
            log.warn("No se pudo conectar con el servidor Ollama en {}. Detalle: {}. Se utilizará el reporte de respaldo.", baseUrl, e.getMessage());
            return buildFallbackReport(e.getMessage());
        }
    }

    /**
     * Genera un informe de respaldo formateado en Markdown cuando el servicio Ollama no se encuentra disponible.
     *
     * @param causeDetails Detalles del error o razón del fallback.
     * @return Reporte informativo de respaldo.
     */
    private String buildFallbackReport(String causeDetails) {
        return """
                ## ⚠️ Síntesis de IA No Disponible (Modo de Respaldo)

                No fue posible obtener la síntesis de arquitectura desde el modelo de IA local Ollama en `%s`.

                **Detalle de la conexión:** %s

                ---

                ### 💡 Instrucciones para activar la IA local:
                1. Asegúrate de tener **Ollama** instalado e iniciado en tu máquina local:
                   ```bash
                   ollama serve
                   ```
                2. Descarga el modelo recomendado ejecutando:
                   ```bash
                   ollama pull %s
                   ```
                3. Vuelve a realizar la solicitud de análisis.

                ---

                > **Nota:** Las evidencias factuales determinísticas (Ficha Técnica, Stack Tecnológico, Catálogo de Componentes y Evidencias Estructurales) han sido calculadas correctamente y se encuentran disponibles en los datos estructurados del reporte.
                """.formatted(baseUrl, causeDetails, model);
    }
}
