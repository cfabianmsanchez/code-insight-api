package com.codeinsight.api.application.port.out;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.domain.model.FetchCodeRequest;

public interface CodeFetcherPort {
    boolean supports(FetchCodeRequest request);
    TempCodeDirectory fetchCode(FetchCodeRequest request);
}
