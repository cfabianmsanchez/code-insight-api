package com.codeinsight.api.application.port.out;

/**
 * Puerto de Salida (Outbound Port) para la Síntesis de Arquitectura con Inteligencia Artificial.
 * 
 * Abstrae la generación de informes técnicos de arquitectura basándose en los prompts
 * producidos en el pipeline determinístico.
 */
public interface ArchitectureSynthesisPort {

    /**
     * Realiza la síntesis del informe arquitectónico enviando los prompts del sistema y usuario.
     *
     * @param systemPrompt Directivas del sistema y rol del evaluador.
     * @param userPrompt   Radiografía factual y evidencias determinísticas del repositorio.
     * @return El reporte en formato Markdown generado por el modelo de IA o una síntesis de respaldo.
     */
    String synthesize(String systemPrompt, String userPrompt);
}
