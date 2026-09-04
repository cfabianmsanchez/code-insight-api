package com.codeinsight.api.infrastructure.config;

import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
import com.codeinsight.api.application.pipeline.stage.RepositoryLoaderStage;
import com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage;
import com.codeinsight.api.application.port.in.AnalyzeRepositoryUseCase;
import com.codeinsight.api.application.port.in.FetchCodeUseCase;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.application.service.AnalyzeRepositoryService;
import com.codeinsight.api.application.service.FetchCodeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BeanConfiguration {

    @Bean
    public FetchCodeUseCase fetchCodeUseCase(List<CodeFetcherPort> fetchers) {
        return new FetchCodeService(fetchers);
    }

    @Bean
    public AnalyzeRepositoryUseCase analyzeRepositoryUseCase(RepositoryLoaderStage repositoryLoader,
                                                               FileScannerStage fileScanner,
                                                               TechnologyDetectorStage technologyDetector) {
        return new AnalyzeRepositoryService(repositoryLoader, fileScanner, technologyDetector);
    }
}
