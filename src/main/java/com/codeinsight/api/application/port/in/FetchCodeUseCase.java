package com.codeinsight.api.application.port.in;

import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.FetchCodeResult;

public interface FetchCodeUseCase {
    FetchCodeResult fetchAndProcess(FetchCodeRequest request);
}
