package com.codeinsight.api.application.service;

import com.codeinsight.api.application.port.in.AnalyzeCodeUseCase;
import com.codeinsight.api.application.port.out.SaveAnalysisReportPort;
import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;
import com.codeinsight.api.domain.strategy.CodeAnalysisStrategy;
import com.codeinsight.api.domain.strategy.CodeAnalysisStrategyFactory;

public class AnalyzeCodeService implements AnalyzeCodeUseCase {

    private final CodeAnalysisStrategyFactory strategyFactory;
    private final SaveAnalysisReportPort saveAnalysisReportPort;

    public AnalyzeCodeService(CodeAnalysisStrategyFactory strategyFactory,
                              SaveAnalysisReportPort saveAnalysisReportPort) {
        this.strategyFactory = strategyFactory;
        this.saveAnalysisReportPort = saveAnalysisReportPort;
    }

    @Override
    public AnalysisReport analyzeCode(CodeAnalysisRequest request) {
        CodeAnalysisStrategy strategy = strategyFactory.getStrategy(request.getType());
        AnalysisReport report = strategy.analyze(request);
        return saveAnalysisReportPort.save(report);
    }
}
