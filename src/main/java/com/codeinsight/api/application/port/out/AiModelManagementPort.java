package com.codeinsight.api.application.port.out;

import java.util.List;

/**
 * Puerto de Salida (Outbound Port) para la gestión y consulta de modelos de Inteligencia Artificial.
 * Abstrae la consulta de modelos instalados y la selección del modelo activo en la infraestructura (ej. Ollama).
 */
public interface AiModelManagementPort {

    /**
     * Retorna la URL base del servidor de IA.
     */
    String getBaseUrl();

    /**
     * Retorna el nombre del modelo de IA actualmente activo.
     */
    String getActiveModel();

    /**
     * Establece el modelo de IA activo para las siguientes síntesis.
     *
     * @param model Nombre del modelo a activar.
     */
    void setActiveModel(String model);

    /**
     * Retorna la lista de modelos realmente disponibles e instalados en la infraestructura.
     */
    List<String> fetchAvailableModels();
}
