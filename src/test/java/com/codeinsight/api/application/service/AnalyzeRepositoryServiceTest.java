package com.codeinsight.api.application.service;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
import com.codeinsight.api.application.pipeline.stage.RepositoryLoaderStage;
import com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;
import com.codeinsight.api.domain.model.SourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeRepositoryServiceTest {

    @Mock
    private CodeFetcherPort fetcherPort;

    @Test
    void analyzeRepository_shouldOrchestrateStagesAndReturnAnalysisResult(@TempDir Path tempDir) throws IOException {
        Path pomPath = tempDir.resolve("pom.xml");
        String pomContent = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(pomPath, pomContent);

        when(fetcherPort.supports(any())).thenReturn(true);
        TempCodeDirectory mockTempDir = new TempCodeDirectory(tempDir, SourceType.GITHUB_REPO);
        when(fetcherPort.fetchCode(any())).thenReturn(mockTempDir);

        RepositoryLoaderStage loaderStage = new RepositoryLoaderStage(List.of(fetcherPort));
        FileScannerStage scannerStage = new FileScannerStage();
        TechnologyDetectorStage techStage = new TechnologyDetectorStage();

        AnalyzeRepositoryService service = new AnalyzeRepositoryService(loaderStage, scannerStage, techStage);

        FetchCodeRequest request = FetchCodeRequest.builder()
                .projectKey("pipeline-integration-test")
                .sourceType(SourceType.GITHUB_REPO)
                .repoUrl("https://github.com/user/demo.git")
                .build();

        RepositoryAnalysisResult result = service.analyzeRepository(request);

        assertNotNull(result);
        assertEquals("pipeline-integration-test", result.getProjectKey());
        assertEquals(SourceType.GITHUB_REPO, result.getSourceType());
        assertEquals(1, result.getTotalFiles());
        assertEquals("Java", result.getTechnologyStack().getMainLanguage());
        assertEquals("Spring Boot", result.getTechnologyStack().getMainFramework());
    }
}
