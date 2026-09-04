package com.codeinsight.api.application.port.in;

import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;

public interface AnalyzeCodeUseCase {
    AnalysisReport analyzeCode(CodeAnalysisRequest request);
}
