package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.exception.UnsupportedSourceTypeException;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepositoryLoaderStage {

    private final List<CodeFetcherPort> fetchers;

    public RepositoryLoaderStage(List<CodeFetcherPort> fetchers) {
        this.fetchers = fetchers;
    }

    public TempCodeDirectory loadRepository(FetchCodeRequest request) {
        if (request == null) {
            throw new InvalidRepositoryException("FetchCodeRequest cannot be null");
        }

        CodeFetcherPort fetcher = fetchers.stream()
                .filter(f -> f.supports(request))
                .findFirst()
                .orElseThrow(() -> new UnsupportedSourceTypeException(
                        "No supported fetcher found for source type: " + request.getSourceType()));

        return fetcher.fetchCode(request);
    }
}
