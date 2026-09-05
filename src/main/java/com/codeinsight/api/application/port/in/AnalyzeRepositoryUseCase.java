package com.codeinsight.api.application.port.in;

import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;

/**
 * Puerto de Entrada (Inbound Port): Caso de Uso de Análisis de Repositorios.
 * 
 * Define el contrato de aplicación para orquestar la ingeniería inversa y análisis
 * completo de un repositorio de código fuente.
 */
public interface AnalyzeRepositoryUseCase {

    /**
     * Ejecuta el análisis integral de ingeniería inversa sobre el repositorio solicitado.
     *
     * @param request Datos de la solicitud (URL de GitHub o archivo ZIP).
     * @return {@link RepositoryAnalysisResult} con los resultados consolidados de todas las etapas.
     */
    RepositoryAnalysisResult analyzeRepository(FetchCodeRequest request);
}
