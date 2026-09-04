package com.codeinsight.api.infrastructure.config;

import com.codeinsight.api.application.port.in.AnalyzeCodeUseCase;
import com.codeinsight.api.application.port.out.SaveAnalysisReportPort;
import com.codeinsight.api.application.service.AnalyzeCodeService;
import com.codeinsight.api.domain.strategy.CodeAnalysisStrategy;
import com.codeinsight.api.domain.strategy.CodeAnalysisStrategyFactory;
import com.codeinsight.api.domain.strategy.JavaCodeAnalysisStrategy;
import com.codeinsight.api.domain.strategy.PythonCodeAnalysisStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BeanConfiguration {

    @Bean
    public JavaCodeAnalysisStrategy javaCodeAnalysisStrategy() {
        return new JavaCodeAnalysisStrategy();
    }

    @Bean
    public PythonCodeAnalysisStrategy pythonCodeAnalysisStrategy() {
        return new PythonCodeAnalysisStrategy();
    }

    @Bean
    public CodeAnalysisStrategyFactory codeAnalysisStrategyFactory(List<CodeAnalysisStrategy> strategies) {
        return new CodeAnalysisStrategyFactory(strategies);
    }

    @Bean
    public AnalyzeCodeUseCase analyzeCodeUseCase(CodeAnalysisStrategyFactory strategyFactory,
                                                SaveAnalysisReportPort saveAnalysisReportPort) {
        return new AnalyzeCodeService(strategyFactory, saveAnalysisReportPort);
    }
}
