package com.codeinsight.api.application.port.in;

import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;

public interface AnalyzeRepositoryUseCase {
    RepositoryAnalysisResult analyzeRepository(FetchCodeRequest request);
}
