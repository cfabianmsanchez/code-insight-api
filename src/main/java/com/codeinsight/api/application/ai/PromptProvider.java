package com.codeinsight.api.application.ai;

/**
 * Puerto/Proveedor para la obtención de plantillas de prompts versionadas.
 * Desacopla la lógica del pipeline de la definición de los artefactos de prompt.
 */
public interface PromptProvider {

    /**
     * Obtiene el prompt del sistema con las directivas de rol y reglas anti-alucinación.
     *
     * @return Contenido Markdown del system prompt.
     */
    String systemPrompt();

    /**
     * Obtiene las directivas de análisis para la síntesis arquitectónica.
     *
     * @return Contenido Markdown de las directivas de usuario.
     */
    String analysisDirectives();
}
