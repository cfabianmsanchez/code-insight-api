package com.codeinsight.api.application.service;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.pipeline.stage.ArchitectureEvidenceDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ComponentDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ContextBuilderStage;
import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
import com.codeinsight.api.application.pipeline.stage.AiSynthesisStage;
import com.codeinsight.api.application.pipeline.stage.RepositoryLoaderStage;
import com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage;
import com.codeinsight.api.application.port.in.AnalyzeRepositoryUseCase;
import com.codeinsight.api.domain.model.AnalysisContext;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de aplicación que orquesta el pipeline de análisis de repositorios.
 */
public class AnalyzeRepositoryService implements AnalyzeRepositoryUseCase {

    private final RepositoryLoaderStage repositoryLoader;
    private final FileScannerStage fileScanner;
    private final TechnologyDetectorStage technologyDetector;
    private final ComponentDetectorStage componentDetector;
    private final ArchitectureEvidenceDetectorStage architectureEvidenceDetector;
    private final ContextBuilderStage contextBuilder;
    private final AiSynthesisStage aiSynthesis;

    /**
     * Crea una nueva instancia del servicio inyectando todas las etapas del pipeline.
     */
    public AnalyzeRepositoryService(RepositoryLoaderStage repositoryLoader,
                                   FileScannerStage fileScanner,
                                   TechnologyDetectorStage technologyDetector,
                                   ComponentDetectorStage componentDetector,
                                   ArchitectureEvidenceDetectorStage architectureEvidenceDetector,
                                   ContextBuilderStage contextBuilder,
                                   AiSynthesisStage aiSynthesis) {
        this.repositoryLoader = repositoryLoader;
        this.fileScanner = fileScanner;
        this.technologyDetector = technologyDetector;
        this.componentDetector = componentDetector;
        this.architectureEvidenceDetector = architectureEvidenceDetector;
        this.contextBuilder = contextBuilder;
        this.aiSynthesis = aiSynthesis;
    }

    /**
     * Orquesta secuencialmente las etapas del pipeline (Carga, Escaneo, Stack, Componentes, Evidencias, Contexto y Síntesis IA).
     *
     * @param request Solicitud con los datos de acceso al código fuente.
     * @return {@link RepositoryAnalysisResult} con la radiografía completa consolidada.
     */
    @Override
    public RepositoryAnalysisResult analyzeRepository(FetchCodeRequest request) {
        // Garantiza la eliminación del workspace temporal al finalizar el pipeline, incluso ante excepciones.
        try (TempCodeDirectory repository = repositoryLoader.loadRepository(request)) {

            // Etapa 2: File Scanner (Escaneo efímero en memoria)
            ScannedFileMap scannedFiles = fileScanner.scan(repository.getTempPath());

            // Etapa 3: Technology Detector (Detección determinista de manifiestos y extensiones)
            TechnologyStack technologyStack = technologyDetector.detect(scannedFiles);

            // Etapa 4: Component Detector (Clasificación de componentes arquitectónicos)
            ComponentAnalysisResult componentAnalysis = componentDetector.detect(scannedFiles);

            // Etapa 5: Architecture Evidence Detector (Recopilación factual de paquetes y evidencias)
            ArchitectureEvidenceResult architectureEvidence = architectureEvidenceDetector.detect(scannedFiles, componentAnalysis, technologyStack);

            // Etapa 6: Context Builder (Ensamblado del prompt estructurado y contexto de análisis)
            AnalysisContext analysisContext = contextBuilder.buildContext(request, scannedFiles, technologyStack, componentAnalysis, architectureEvidence);

            // Etapa 7: AI Synthesis (Síntesis de arquitectura asistida por IA)
            String aiSynthesisReport = aiSynthesis.analyze(analysisContext);
            String aiModelUsed = aiSynthesis.getActiveModel();

            // Extraer el resumen funcional de la síntesis generada por la IA
            String functionalSummary = extractFunctionalSummary(aiSynthesisReport);

            return RepositoryAnalysisResult.builder()
                    .projectKey(request.getProjectKey())
                    .sourceType(request.getSourceType())
                    .totalFiles(scannedFiles.getTotalFiles())
                    .totalDirectories(scannedFiles.getTotalDirectories())
                    .technologyStack(technologyStack)
                    .componentAnalysis(componentAnalysis)
                    .architectureEvidence(architectureEvidence)
                    .analysisContext(analysisContext)
                    .aiSynthesis(aiSynthesisReport)
                    .functionalSummary(functionalSummary)
                    .aiModelUsed(aiModelUsed)
                    .extensionCounts(scannedFiles.getExtensionCounts())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Extrae de forma estricta únicamente la sección 0 ("Resumen Funcional") de la respuesta del LLM.
     * Trunca la cadena antes de que comience la sección 1 ("Clasificación Arquitectónica").
     *
     * @param aiSynthesis Respuesta completa generada por el LLM.
     * @return Texto exclusivo del resumen funcional, o {@code null} si está vacío.
     */
    private String extractFunctionalSummary(String aiSynthesis) {
        if (aiSynthesis == null || aiSynthesis.isBlank()) return null;
        int start = -1;
        for (String marker : new String[]{"## 0.", "### 0.", "## Resumen Funcional", "**0. Resumen", "0. Resumen"}) {
            int idx = aiSynthesis.indexOf(marker);
            if (idx >= 0) { start = idx; break; }
        }
        if (start < 0) {
            start = 0;
        }

        String fromStart = aiSynthesis.substring(start);
        String cleanedHeader = fromStart.replaceFirst("(?i)^(##|###|\\*\\*)*\\s*0?\\.?\\s*Resumen\\s*Funcional.*(\\r?\\n)?", "").trim();

        // Delimitar el final de la sección 0 buscando el inicio de la sección 1 o cualquier encabezado subsiguiente
        Pattern nextSectionPattern = Pattern.compile("(?m)^(?=#{1,3}\\s*1\\.|#{1,3}\\s+Clasificaci[oó]n|\\*\\*1\\.|1\\.\\s+Clasificaci[oó]n|#{1,3}\\s+)");
        Matcher matcher = nextSectionPattern.matcher(cleanedHeader);
        if (matcher.find() && matcher.start() > 0) {
            cleanedHeader = cleanedHeader.substring(0, matcher.start()).trim();
        }

        return cleanedHeader.isBlank() ? null : cleanedHeader;
    }
}
