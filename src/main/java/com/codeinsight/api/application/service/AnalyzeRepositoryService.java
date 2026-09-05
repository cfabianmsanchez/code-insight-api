package com.codeinsight.api.application.service;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.pipeline.stage.ArchitectureEvidenceDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ComponentDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ContextBuilderStage;
import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
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

/**
 * Servicio de Aplicación para el Análisis de Repositorios.
 * 
 * Implementa el caso de uso {@link AnalyzeRepositoryUseCase} y orquesta la ejecución
 * secuencial de todas las etapas del pipeline de ingeniería inversa.
 * Garantiza la limpieza automática del directorio temporal al concluir la ejecución.
 */
public class AnalyzeRepositoryService implements AnalyzeRepositoryUseCase {

    private final RepositoryLoaderStage repositoryLoader;
    private final FileScannerStage fileScanner;
    private final TechnologyDetectorStage technologyDetector;
    private final ComponentDetectorStage componentDetector;
    private final ArchitectureEvidenceDetectorStage architectureEvidenceDetector;
    private final ContextBuilderStage contextBuilder;

    /**
     * Crea una nueva instancia del servicio inyectando todas las etapas del pipeline.
     */
    public AnalyzeRepositoryService(RepositoryLoaderStage repositoryLoader,
                                   FileScannerStage fileScanner,
                                   TechnologyDetectorStage technologyDetector,
                                   ComponentDetectorStage componentDetector,
                                   ArchitectureEvidenceDetectorStage architectureEvidenceDetector,
                                   ContextBuilderStage contextBuilder) {
        this.repositoryLoader = repositoryLoader;
        this.fileScanner = fileScanner;
        this.technologyDetector = technologyDetector;
        this.componentDetector = componentDetector;
        this.architectureEvidenceDetector = architectureEvidenceDetector;
        this.contextBuilder = contextBuilder;
    }

    /**
     * Orquesta secuencialmente las etapas del pipeline (Carga, Escaneo, Stack, Componentes, Evidencias y Contexto).
     *
     * @param request Solicitud con los datos de acceso al código fuente.
     * @return {@link RepositoryAnalysisResult} con la radiografía completa consolidada.
     */
    @Override
    public RepositoryAnalysisResult analyzeRepository(FetchCodeRequest request) {
        // El bloque try-with-resources envuelve el pipeline completo y destruye la carpeta efímera al finalizar la Etapa N
        try (TempCodeDirectory repository = repositoryLoader.loadRepository(request)) {

            // Etapa 2: File Scanner (Escaneo efímero en memoria)
            ScannedFileMap scannedFiles = fileScanner.scan(repository.getTempPath());

            // Etapa 3: Technology Detector (Detección determinista de manifiestos y extensiones)
            TechnologyStack technologyStack = technologyDetector.detect(scannedFiles);

            // Etapa 4: Component Detector (Clasificación de componentes arquitectónicos)
            ComponentAnalysisResult componentAnalysis = componentDetector.detect(scannedFiles);

            // Etapa 5: Architecture Evidence Detector (Recopilación factual de paquetes y evidencias)
            ArchitectureEvidenceResult architectureEvidence = architectureEvidenceDetector.detect(scannedFiles, componentAnalysis);

            // Etapa 6: Context Builder (Ensamblado del prompt estructurado y contexto de análisis)
            AnalysisContext analysisContext = contextBuilder.buildContext(request, scannedFiles, technologyStack, componentAnalysis, architectureEvidence);

            return RepositoryAnalysisResult.builder()
                    .projectKey(request.getProjectKey())
                    .sourceType(request.getSourceType())
                    .totalFiles(scannedFiles.getTotalFiles())
                    .totalDirectories(scannedFiles.getTotalDirectories())
                    .technologyStack(technologyStack)
                    .componentAnalysis(componentAnalysis)
                    .architectureEvidence(architectureEvidence)
                    .analysisContext(analysisContext)
                    .extensionCounts(scannedFiles.getExtensionCounts())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
