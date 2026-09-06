package com.codeinsight.api.application.pipeline.stage;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeinsight.api.domain.model.AnalysisContext;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.ComponentType;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.SourceType;
import com.codeinsight.api.domain.model.TechnologyStack;
import com.codeinsight.api.infrastructure.adapter.out.prompt.ResourcePromptProvider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class ContextBuilderStageTest {

  private ResourcePromptProvider promptProvider;
  private ContextBuilderStage stage;

  @BeforeEach
  void setUp() {
    promptProvider = new ResourcePromptProvider(
      new DefaultResourceLoader(),
      "v1"
    );
    promptProvider.init();
    stage = new ContextBuilderStage(promptProvider);
  }

  @Test
  void buildContext_shouldAssembleRichMarkdownPromptCorrectly() {
    FetchCodeRequest request = FetchCodeRequest.builder()
      .projectKey("kata-demo")
      .sourceType(SourceType.GITHUB_REPO)
      .build();

    ScannedFileMap scannedFiles = ScannedFileMap.builder()
      .totalFiles(15)
      .totalDirectories(8)
      .extensionCounts(Map.of(".java", 12, ".xml", 1, ".yml", 2))
      .build();

    TechnologyStack techStack = TechnologyStack.builder()
      .mainLanguage("Java")
      .mainFramework("Spring Boot")
      .buildTool("Maven")
      .keyLibraries(List.of("Lombok", "Swagger"))
      .build();

    ComponentAnalysisResult componentAnalysis =
      ComponentAnalysisResult.builder()
        .totalComponents(2)
        .componentCounts(Map.of("CONTROLLER", 1, "SERVICE", 1))
        .components(
          List.of(
            new DetectedComponent(
              "UserController",
              ComponentType.CONTROLLER,
              "src/main/java/com/demo/UserController.java"
            ),
            new DetectedComponent(
              "UserService",
              ComponentType.SERVICE,
              "src/main/java/com/demo/UserService.java"
            )
          )
        )
        .build();

    ArchitectureEvidenceResult archEvidence =
      ArchitectureEvidenceResult.builder()
        .totalStructuralPaths(5)
        .maxPathDepth(6)
        .detectedKeywords(List.of("domain", "application", "infrastructure"))
        .packageComponentDistribution(
          Map.of("infrastructure", 1, "application", 1)
        )
        .evidenceNotes(List.of("Identificadas 5 rutas estructurales distintas"))
        .build();

    AnalysisContext result = stage.buildContext(
      request,
      scannedFiles,
      techStack,
      componentAnalysis,
      archEvidence
    );

    assertNotNull(result);
    assertNotNull(result.getSystemPrompt());
    assertNotNull(result.getUserPrompt());
    assertNotNull(result.getFormattedContextPrompt());

    assertTrue(result.getUserPrompt().contains("kata-demo"));
    assertTrue(result.getUserPrompt().contains("Spring Boot"));
    assertTrue(result.getUserPrompt().contains("UserController"));
    assertTrue(
      result.getUserPrompt().contains("domain, application, infrastructure")
    );
    assertTrue(result.getSystemPrompt().contains("Arquitecto de Soluciones"));
  }
}
