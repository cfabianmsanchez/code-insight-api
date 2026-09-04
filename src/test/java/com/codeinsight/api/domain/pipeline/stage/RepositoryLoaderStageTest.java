package com.codeinsight.api.domain.pipeline.stage;

import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.SourceType;
import com.codeinsight.api.domain.model.TempCodeDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryLoaderStageTest {

    @Mock
    private CodeFetcherPort githubFetcherPort;

    @Test
    void loadRepository_shouldSelectSupportedFetcherAndReturnTempDirectory() {
        when(githubFetcherPort.supports(any())).thenAnswer(invocation -> {
            FetchCodeRequest req = invocation.getArgument(0);
            return req != null && SourceType.GITHUB_REPO.equals(req.getSourceType());
        });

        RepositoryLoaderStage repositoryLoaderStage = new RepositoryLoaderStage(List.of(githubFetcherPort));

        FetchCodeRequest request = FetchCodeRequest.builder()
                .projectKey("test-repo")
                .sourceType(SourceType.GITHUB_REPO)
                .repoUrl("https://github.com/user/demo.git")
                .build();

        Path mockPath = Path.of("/tmp/mock-dir");
        TempCodeDirectory mockTempDir = new TempCodeDirectory(mockPath, SourceType.GITHUB_REPO);
        when(githubFetcherPort.fetchCode(request)).thenReturn(mockTempDir);

        TempCodeDirectory result = repositoryLoaderStage.loadRepository(request);

        assertNotNull(result);
        assertEquals(mockPath, result.getTempPath());
        assertEquals(SourceType.GITHUB_REPO, result.getSourceType());
        verify(githubFetcherPort).fetchCode(request);
    }

    @Test
    void loadRepository_shouldThrowExceptionIfRequestIsNull() {
        RepositoryLoaderStage repositoryLoaderStage = new RepositoryLoaderStage(List.of(githubFetcherPort));
        assertThrows(IllegalArgumentException.class, () -> repositoryLoaderStage.loadRepository(null));
    }
}
