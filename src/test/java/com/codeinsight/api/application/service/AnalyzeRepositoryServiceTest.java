package com.codeinsight.api.application.service;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.pipeline.stage.ArchitectureEvidenceDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ComponentDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ContextBuilderStage;
import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
import com.codeinsight.api.application.pipeline.stage.AiSynthesisStage;
import com.codeinsight.api.application.pipeline.stage.RepositoryLoaderStage;
import com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage;
import com.codeinsight.api.application.port.out.ArchitectureSynthesisPort;
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

        Path controllerPath = tempDir.resolve("UserController.java");
        String controllerContent = """
                package com.example.demo;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class UserController {}
                """;
        Files.writeString(controllerPath, controllerContent);

        when(fetcherPort.supports(any())).thenReturn(true);
        TempCodeDirectory mockTempDir = new TempCodeDirectory(tempDir, SourceType.GITHUB_REPO);
        when(fetcherPort.fetchCode(any())).thenReturn(mockTempDir);

        RepositoryLoaderStage loaderStage = new RepositoryLoaderStage(List.of(fetcherPort));
        FileScannerStage scannerStage = new FileScannerStage();
        TechnologyDetectorStage techStage = new TechnologyDetectorStage();
        ComponentDetectorStage componentStage = new ComponentDetectorStage();
        ArchitectureEvidenceDetectorStage archStage = new ArchitectureEvidenceDetectorStage();
        com.codeinsight.api.application.ai.PromptProvider promptProvider = new com.codeinsight.api.infrastructure.adapter.out.prompt.ResourcePromptProvider(new org.springframework.core.io.DefaultResourceLoader(), "v1");
        ((com.codeinsight.api.infrastructure.adapter.out.prompt.ResourcePromptProvider) promptProvider).init();
        ContextBuilderStage contextStage = new ContextBuilderStage(promptProvider);
        ArchitectureSynthesisPort mockSynthesisPort = new ArchitectureSynthesisPort() {
            @Override
            public String synthesize(String sys, String user) { return "Mocked AI Architecture Synthesis Report"; }
            @Override
            public String getActiveModel() { return "mock-model"; }
        };
        AiSynthesisStage aiStage = new AiSynthesisStage(mockSynthesisPort);

        AnalyzeRepositoryService service = new AnalyzeRepositoryService(loaderStage, scannerStage, techStage, componentStage, archStage, contextStage, aiStage);

        FetchCodeRequest request = FetchCodeRequest.builder()
                .projectKey("pipeline-integration-test")
                .sourceType(SourceType.GITHUB_REPO)
                .repoUrl("https://github.com/user/demo.git")
                .build();

        RepositoryAnalysisResult result = service.analyzeRepository(request);

        assertNotNull(result);
        assertEquals("pipeline-integration-test", result.getProjectKey());
        assertEquals(SourceType.GITHUB_REPO, result.getSourceType());
        assertEquals(2, result.getTotalFiles());
        assertEquals("Java", result.getTechnologyStack().getMainLanguage());
        assertEquals("Spring Boot", result.getTechnologyStack().getMainFramework());

        assertNotNull(result.getComponentAnalysis());
        assertEquals(1, result.getComponentAnalysis().getTotalComponents());

        assertNotNull(result.getArchitectureEvidence());
        assertNotNull(result.getAnalysisContext());
        assertNotNull(result.getAiSynthesis());
        assertEquals("Mocked AI Architecture Synthesis Report", result.getAiSynthesis());
    }
}
