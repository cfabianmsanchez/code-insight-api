package com.codeinsight.api.domain.strategy;

import com.codeinsight.api.domain.exception.UnsupportedAnalysisTypeException;
import com.codeinsight.api.domain.model.AnalysisType;

import java.util.List;

/**
 * Factory / Context manager for Code Analysis Strategies.
 * Resolves the matching strategy for a given AnalysisType.
 */
public class CodeAnalysisStrategyFactory {

    private final List<CodeAnalysisStrategy> strategies;

    public CodeAnalysisStrategyFactory(List<CodeAnalysisStrategy> strategies) {
        this.strategies = strategies;
    }

    public CodeAnalysisStrategy getStrategy(AnalysisType analysisType) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(analysisType))
                .findFirst()
                .orElseThrow(() -> new UnsupportedAnalysisTypeException(analysisType));
    }
}
