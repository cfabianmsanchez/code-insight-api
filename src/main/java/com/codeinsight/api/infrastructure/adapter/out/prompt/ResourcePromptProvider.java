package com.codeinsight.api.infrastructure.adapter.out.prompt;

import com.codeinsight.api.application.ai.PromptProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Adaptador de Infraestructura para {@link PromptProvider}.
 * Carga plantillas Markdown versionadas desde classpath:prompts/ utilizando las capacidades de I/O de Spring.
 * Carga los archivos al iniciar la aplicación para evitar operaciones de I/O en tiempo de ejecución.
 */
@Component
public class ResourcePromptProvider implements PromptProvider {

    private final ResourceLoader resourceLoader;
    private final String promptVersion;

    private String cachedSystemPrompt;
    private String cachedAnalysisDirectives;

    public ResourcePromptProvider(ResourceLoader resourceLoader,
                                  @Value("${ai.prompts.architecture-version:v1}") String promptVersion) {
        this.resourceLoader = resourceLoader;
        this.promptVersion = promptVersion;
    }

    @PostConstruct
    public void init() {
        this.cachedSystemPrompt = loadPrompt("system");
        this.cachedAnalysisDirectives = loadPrompt("analysis");
    }

    private String loadPrompt(String promptType) {
        String resourcePath = String.format("classpath:prompts/%s-%s.md", promptType, promptVersion);
        Resource resource = resourceLoader.getResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalStateException("No se encontró el artefacto de prompt en: " + resourcePath);
        }
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el archivo de prompt: " + resourcePath, e);
        }
    }

    @Override
    public String systemPrompt() {
        return cachedSystemPrompt;
    }

    @Override
    public String analysisDirectives() {
        return cachedAnalysisDirectives;
    }
}
