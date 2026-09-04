package com.codeinsight.api.application.service;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.pipeline.stage.ComponentDetectorStage;
import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
import com.codeinsight.api.application.pipeline.stage.RepositoryLoaderStage;
import com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage;
import com.codeinsight.api.application.port.in.AnalyzeRepositoryUseCase;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;

import java.time.LocalDateTime;

public class AnalyzeRepositoryService implements AnalyzeRepositoryUseCase {

    private final RepositoryLoaderStage repositoryLoader;
    private final FileScannerStage fileScanner;
    private final TechnologyDetectorStage technologyDetector;
    private final ComponentDetectorStage componentDetector;

    public AnalyzeRepositoryService(RepositoryLoaderStage repositoryLoader,
                                   FileScannerStage fileScanner,
                                   TechnologyDetectorStage technologyDetector,
                                   ComponentDetectorStage componentDetector) {
        this.repositoryLoader = repositoryLoader;
        this.fileScanner = fileScanner;
        this.technologyDetector = technologyDetector;
        this.componentDetector = componentDetector;
    }

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

            return RepositoryAnalysisResult.builder()
                    .projectKey(request.getProjectKey())
                    .sourceType(request.getSourceType())
                    .totalFiles(scannedFiles.getTotalFiles())
                    .totalDirectories(scannedFiles.getTotalDirectories())
                    .technologyStack(technologyStack)
                    .componentAnalysis(componentAnalysis)
                    .extensionCounts(scannedFiles.getExtensionCounts())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
