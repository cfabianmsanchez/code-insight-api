package com.codeinsight.api.application.pipeline.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.ComponentType;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.ProjectKind;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchitectureEvidenceDetectorStageTest {

  private final ArchitectureEvidenceDetectorStage stage =
    new ArchitectureEvidenceDetectorStage();

  @Test
  void detect_shouldGatherFactualArchitecturalEvidencesCorrectly(
    @TempDir Path tempDir
  ) {
    List<String> files = List.of(
      "src/main/java/com/demo/domain/model/User.java",
      "src/main/java/com/demo/application/pipeline/stage/ScannerStage.java",
      "src/main/java/com/demo/infrastructure/adapter/in/rest/UserController.java"
    );

    ScannedFileMap scannedFiles = ScannedFileMap.builder()
      .rootPath(tempDir)
      .relativeFilePaths(files)
      .build();

    ComponentAnalysisResult componentAnalysis =
      ComponentAnalysisResult.builder()
        .totalComponents(1)
        .components(
          List.of(
            new DetectedComponent(
              "UserController",
              ComponentType.CONTROLLER,
              "src/main/java/com/demo/infrastructure/adapter/in/rest/UserController.java"
            )
          )
        )
        .build();

    ArchitectureEvidenceResult result = stage.detect(
      scannedFiles,
      componentAnalysis,
      null
    );

    assertNotNull(result);
    assertEquals(3, result.getTotalStructuralPaths());
    assertTrue(result.getMaxPathDepth() > 0);
    assertTrue(result.getDetectedKeywords().contains("domain"));
    assertTrue(result.getDetectedKeywords().contains("application"));
    assertTrue(result.getDetectedKeywords().contains("infrastructure"));
    assertTrue(result.getDetectedKeywords().contains("adapter"));
    assertEquals(
      1,
      result.getPackageComponentDistribution().get("infrastructure")
    );
    assertTrue(
      result
        .getEvidenceNotes()
        .stream()
        .anyMatch(note ->
          note.contains("Palabras clave de arquitectura encontradas")
        )
    );
  }

  @Test
  void detect_shouldExtractNodeProjectAndTestingEvidence(@TempDir Path tempDir)
    throws java.io.IOException {
    Path packageJsonPath = tempDir.resolve("package.json");
    String packageJsonContent = """
    {
      "name": "demo-service",
      "description": "Microservicio de pruebas",
      "main": "index.js",
      "scripts": {
        "test": "jest"
      }
    }
    """;
    java.nio.file.Files.writeString(packageJsonPath, packageJsonContent);

    Path testDirPath = tempDir.resolve("tests");
    java.nio.file.Files.createDirectories(testDirPath);
    Path testFilePath = testDirPath.resolve("app.test.js");
    java.nio.file.Files.writeString(
      testFilePath,
      "test('should work', () => {});"
    );

    List<String> files = List.of("package.json", "tests/app.test.js");
    ScannedFileMap scannedFiles = ScannedFileMap.builder()
      .rootPath(tempDir)
      .relativeFilePaths(files)
      .build();

    ComponentAnalysisResult componentAnalysis =
      ComponentAnalysisResult.builder()
        .totalComponents(0)
        .components(List.of())
        .build();

    ArchitectureEvidenceResult result = stage.detect(
      scannedFiles,
      componentAnalysis,
      null
    );

    assertNotNull(result);
    assertNotNull(result.getEngineeringEvidence());

    var eng = result.getEngineeringEvidence();
    assertEquals("demo-service", eng.projectName());
    assertEquals("Microservicio de pruebas", eng.projectDescription());
    assertEquals("index.js", eng.mainEntry());
    assertEquals("jest", eng.testScript());
    assertTrue(eng.testScriptDetected());
    assertTrue(eng.testFilesDetected() > 0);
    assertTrue(eng.testDirectories().contains("tests"));
  }

  @Test
  void detect_nestJsProjectShouldBeClassifiedAsBackend(@TempDir Path tempDir) {
    List<String> files = List.of("src/app.module.ts", "src/app.controller.ts");

    ScannedFileMap scannedFiles = ScannedFileMap.builder()
      .rootPath(tempDir)
      .relativeFilePaths(files)
      .build();

    ComponentAnalysisResult componentAnalysis =
      ComponentAnalysisResult.builder()
        .totalComponents(0)
        .components(List.of())
        .build();

    // TechnologyStack ya detectó NestJS — aunque los archivos sean .ts debe clasificarse BACKEND
    TechnologyStack nestStack = TechnologyStack.builder()
      .mainLanguage("TypeScript")
      .mainFramework("NestJS")
      .buildTool("npm / Node.js")
      .build();

    ArchitectureEvidenceResult result = stage.detect(
      scannedFiles,
      componentAnalysis,
      nestStack
    );

    assertNotNull(result);
    assertEquals(ProjectKind.BACKEND, result.getProjectKind());
  }
}
