package com.codeinsight.api.infrastructure.config;

import com.codeinsight.api.application.pipeline.stage.ArchitectureEvidenceDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ComponentDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ContextBuilderStage;
import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
import com.codeinsight.api.application.pipeline.stage.RepositoryLoaderStage;
import com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage;
import com.codeinsight.api.application.port.in.AnalyzeRepositoryUseCase;
import com.codeinsight.api.application.service.AnalyzeRepositoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public AnalyzeRepositoryUseCase analyzeRepositoryUseCase(RepositoryLoaderStage repositoryLoader,
                                                               FileScannerStage fileScanner,
                                                               TechnologyDetectorStage technologyDetector,
                                                               ComponentDetectorStage componentDetector,
                                                               ArchitectureEvidenceDetectorStage architectureEvidenceDetector,
                                                               ContextBuilderStage contextBuilder) {
        return new AnalyzeRepositoryService(repositoryLoader, fileScanner, technologyDetector, componentDetector, architectureEvidenceDetector, contextBuilder);
    }
}
