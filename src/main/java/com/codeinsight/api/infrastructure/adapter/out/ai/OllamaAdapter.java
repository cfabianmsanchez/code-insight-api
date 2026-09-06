package com.codeinsight.api.infrastructure.adapter.out.ai;

import com.codeinsight.api.application.port.out.AiModelManagementPort;
import com.codeinsight.api.application.port.out.ArchitectureSynthesisPort;
import com.codeinsight.api.infrastructure.adapter.out.ai.dto.OllamaChatRequest;
import com.codeinsight.api.infrastructure.adapter.out.ai.dto.OllamaChatResponse;
import com.codeinsight.api.infrastructure.adapter.out.ai.dto.OllamaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adaptador de Salida (Outbound Adapter): Cliente HTTP REST para Ollama.
 * 
 * Implementa {@link ArchitectureSynthesisPort} y {@link AiModelManagementPort} conectándose vía REST
 * con la API local de Ollama (`/api/chat` y `/api/tags`) mediante {@link RestClient}.
 */
@Component
public class OllamaAdapter implements ArchitectureSynthesisPort, AiModelManagementPort {

    private static final Logger log = LoggerFactory.getLogger(OllamaAdapter.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final AtomicReference<String> activeModel;
    private final List<String> fallbackModels;

    public record OllamaModelItem(String name, String model) {}
    public record OllamaTagsResponse(List<OllamaModelItem> models) {}

    public OllamaAdapter(RestClient.Builder builder, String baseUrl, String model) {
        this(builder, baseUrl, model, "qwen2.5-coder,llama3,deepseek-coder,codellama,mistral");
    }

    /**
     * Construye una nueva instancia del adaptador de Ollama utilizando {@link RestClient.Builder}.
     *
     * @param builder         Builder inyectado por Spring Boot para instanciar RestClient.
     * @param baseUrl         URL base de la API HTTP de Ollama (ej. http://localhost:11434).
     * @param model           Nombre del modelo de IA configurado por defecto (ej. qwen2.5-coder).
     * @param availableModels Cadena separada por comas de modelos de respaldo.
     */
    @Autowired
    public OllamaAdapter(RestClient.Builder builder,
                          @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
                          @Value("${ollama.model:qwen2.5-coder}") String model,
                          @Value("${ollama.available-models:qwen2.5-coder,llama3,deepseek-coder,codellama,mistral}") String availableModels) {
        this.baseUrl = baseUrl;
        this.activeModel = new AtomicReference<>(model);
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
        this.fallbackModels = Arrays.stream(availableModels.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getActiveModel() {
        return activeModel.get();
    }

    @Override
    public void setActiveModel(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("El nombre del modelo de IA no puede estar vacío.");
        }
        String cleanModel = model.trim();
        List<String> available = fetchAvailableModels();
        if (!available.contains(cleanModel)) {
            throw new IllegalArgumentException("El modelo de Ollama no está instalado o disponible en el servidor: " + cleanModel);
        }
        this.activeModel.set(cleanModel);
        log.info("Modelo de IA activo actualizado exitosamente a: {}", cleanModel);
    }

    /**
     * Consulta dinámicamente los modelos instalados en el servidor local de Ollama (/api/tags).
     * Si la consulta a Ollama es exitosa, retorna EXCLUSIVAMENTE los modelos realmente instalados.
     * Si la consulta falla o el servidor está inactivo, retorna los modelos de respaldo configurados.
     */
    @Override
    public List<String> fetchAvailableModels() {
        try {
            OllamaTagsResponse tagsResponse = restClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .body(OllamaTagsResponse.class);

            if (tagsResponse != null && tagsResponse.models() != null && !tagsResponse.models().isEmpty()) {
                Set<String> installedModels = new LinkedHashSet<>();
                for (OllamaModelItem item : tagsResponse.models()) {
                    if (item.name() != null && !item.name().isBlank()) {
                        installedModels.add(item.name());
                    }
                }
                if (!installedModels.isEmpty()) {
                    installedModels.add(getActiveModel());
                    return new ArrayList<>(installedModels);
                }
            }
        } catch (Exception e) {
            log.debug("No se pudieron obtener los modelos dinámicos de Ollama (/api/tags). Usando lista de respaldo. Detalle: {}", e.getMessage());
        }

        Set<String> fallbackSet = new LinkedHashSet<>();
        fallbackSet.add(getActiveModel());
        fallbackSet.addAll(fallbackModels);
        return new ArrayList<>(fallbackSet);
    }

    /**
     * Envía los prompts a la API de Ollama y retorna la síntesis generada por la IA.
     * Si el servidor de Ollama no responde o no está disponible, retorna un informe de respaldo.
     */
    @Override
    public String synthesize(String systemPrompt, String userPrompt) {
        String currentModel = getActiveModel();
        try {
            OllamaChatRequest request = new OllamaChatRequest(
                    currentModel,
                    List.of(
                            new OllamaMessage("system", systemPrompt),
                            new OllamaMessage("user", userPrompt)
                    ),
                    false
            );

            log.info("Enviando prompt de análisis a Ollama en {} con el modelo {}", baseUrl, currentModel);

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
     */
    private String buildFallbackReport(String causeDetails) {
        String currentModel = getActiveModel();
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
                """.formatted(baseUrl, causeDetails, currentModel);
    }
}
