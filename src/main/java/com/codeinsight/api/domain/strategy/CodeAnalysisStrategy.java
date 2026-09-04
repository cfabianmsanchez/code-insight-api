package com.codeinsight.api.domain.strategy;

import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.AnalysisType;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;

/**
 * Strategy Interface for Code Analysis algorithms.
 * Implementations define language or framework-specific inspection logic.
 */
public interface CodeAnalysisStrategy {
    
    /**
     * Determines if this strategy supports the requested analysis type.
     */
    boolean supports(AnalysisType analysisType);

    /**
     * Executes code analysis and generates a report.
     */
    AnalysisReport analyze(CodeAnalysisRequest request);
}
