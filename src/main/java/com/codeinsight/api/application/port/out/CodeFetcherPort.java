package com.codeinsight.api.application.port.out;

import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.TempCodeDirectory;

public interface CodeFetcherPort {
    boolean supports(FetchCodeRequest request);
    TempCodeDirectory fetchCode(FetchCodeRequest request);
}
