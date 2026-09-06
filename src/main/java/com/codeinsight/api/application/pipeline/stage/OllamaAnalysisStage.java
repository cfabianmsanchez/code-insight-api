package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.port.out.ArchitectureSynthesisPort;
import com.codeinsight.api.domain.model.AnalysisContext;
import org.springframework.stereotype.Component;

/**
 * Etapa 7 del Pipeline: Ollama Analysis Stage.
 * 
 * Invoca el puerto de salida {@link ArchitectureSynthesisPort} para obtener la síntesis
 * técnica generada por el LLM a partir del contexto determinístico compilado en la Etapa 6.
 */
@Component
public class OllamaAnalysisStage {

    private final ArchitectureSynthesisPort synthesisPort;

    /**
     * Crea una nueva instancia inyectando el puerto de síntesis de arquitectura.
     *
     * @param synthesisPort Puerto de salida hacia el motor de IA.
     */
    public OllamaAnalysisStage(ArchitectureSynthesisPort synthesisPort) {
        this.synthesisPort = synthesisPort;
    }

    /**
     * Ejecuta la síntesis de arquitectura enviando el contexto formateado al LLM.
     *
     * @param context Objeto {@link AnalysisContext} generado en la Etapa 6.
     * @return El texto del reporte de síntesis en formato Markdown.
     */
    public String analyze(AnalysisContext context) {
        if (context == null) {
            return "No se proporcionó contexto para la síntesis de arquitectura.";
        }
        return synthesisPort.synthesize(context.getSystemPrompt(), context.getUserPrompt());
    }

    public String getActiveModel() {
        return synthesisPort.getActiveModel();
    }
}
