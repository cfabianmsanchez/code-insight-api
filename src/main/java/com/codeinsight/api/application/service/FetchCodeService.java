package com.codeinsight.api.application.service;

import com.codeinsight.api.application.port.in.FetchCodeUseCase;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.FetchCodeResult;
import com.codeinsight.api.domain.model.TempCodeDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FetchCodeService implements FetchCodeUseCase {

    private final List<CodeFetcherPort> fetchers;

    public FetchCodeService(List<CodeFetcherPort> fetchers) {
        this.fetchers = fetchers;
    }

    @Override
    public FetchCodeResult fetchAndProcess(FetchCodeRequest request) {
        CodeFetcherPort fetcher = fetchers.stream()
                .filter(f -> f.supports(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No fetcher supported for source type: " + request.getSourceType()));

        AtomicInteger fileCount = new AtomicInteger(0);
        AtomicInteger dirCount = new AtomicInteger(0);

        // Garantiza la eliminación del directorio temporal al finalizar
        try (TempCodeDirectory tempDir = fetcher.fetchCode(request)) {
            Path path = tempDir.getTempPath();
            if (path != null && Files.exists(path)) {
                try (Stream<Path> stream = Files.walk(path)) {
                    stream.forEach(p -> {
                        if (Files.isDirectory(p)) {
                            dirCount.incrementAndGet();
                        } else {
                            fileCount.incrementAndGet();
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException("Error scanning temporary folder structure: " + e.getMessage(), e);
                }
            }

            return FetchCodeResult.builder()
                    .projectKey(request.getProjectKey())
                    .sourceType(request.getSourceType())
                    .filesExtracted(fileCount.get())
                    .directoriesCreated(dirCount.get())
                    .message("Successfully fetched and extracted repository into temporary folder.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
