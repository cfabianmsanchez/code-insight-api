package com.codeinsight.api.application.pipeline.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TechnologyDetectorStageTest {

  private final TechnologyDetectorStage stage = new TechnologyDetectorStage();

  @Test
  void detect_shouldIdentifySpringBootAndPostgresFromPomXml(
    @TempDir Path tempDir
  ) throws IOException {
    Path pomPath = tempDir.resolve("pom.xml");
    String pomContent = """
    <project>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
            </dependency>
        </dependencies>
    </project>
    """;
    Files.writeString(pomPath, pomContent);

    ScannedFileMap fileMap = ScannedFileMap.builder()
      .rootPath(tempDir)
      .extensionCounts(Map.of(".java", 12))
      .manifestFiles(List.of(pomPath))
      .build();

    TechnologyStack techStack = stage.detect(fileMap);

    assertNotNull(techStack);
    assertEquals("Java", techStack.getMainLanguage());
    assertEquals("Spring Boot", techStack.getMainFramework());
    assertEquals("Maven", techStack.getBuildTool());
    assertTrue(techStack.getDatabasesDetected().contains("PostgreSQL"));
  }

  @Test
  void detect_shouldIdentifyAngularFromPackageJson(@TempDir Path tempDir)
    throws IOException {
    Path pkgPath = tempDir.resolve("package.json");
    String pkgContent = """
    {
      "dependencies": {
        "@angular/core": "^17.0.0"
      }
    }
    """;
    Files.writeString(pkgPath, pkgContent);

    ScannedFileMap fileMap = ScannedFileMap.builder()
      .rootPath(tempDir)
      .extensionCounts(Map.of(".ts", 8, ".html", 4))
      .manifestFiles(List.of(pkgPath))
      .build();

    TechnologyStack techStack = stage.detect(fileMap);

    assertNotNull(techStack);
    assertEquals("TypeScript", techStack.getMainLanguage());
    assertEquals("Angular", techStack.getMainFramework());
    assertEquals("npm / Node.js", techStack.getBuildTool());
  }

  @Test
  void detect_shouldIdentifyTerraformIacAndProvidersFromTfFile(
    @TempDir Path tempDir
  ) throws IOException {
    Path mainTf = tempDir.resolve("main.tf");
    String tfContent = """
    provider "aws" {
      region = "us-east-1"
    }
    resource "aws_s3_bucket" "b" {
      bucket = "my-tf-test-bucket"
    }
    """;
    Files.writeString(mainTf, tfContent);

    ScannedFileMap fileMap = ScannedFileMap.builder()
      .rootPath(tempDir)
      .extensionCounts(Map.of(".tf", 5))
      .manifestFiles(List.of(mainTf))
      .build();

    TechnologyStack techStack = stage.detect(fileMap);

    assertNotNull(techStack);
    assertEquals("HCL / Terraform", techStack.getMainLanguage());
    assertEquals("Terraform IaC", techStack.getMainFramework());
    assertEquals("Terraform CLI", techStack.getBuildTool());
    assertTrue(techStack.getKeyLibraries().contains("AWS Provider"));
  }

  @Test
  void detect_shouldPrioritizeMavenOverNpmInJavaProjectWithPackageJson(
    @TempDir Path tempDir
  ) throws IOException {
    Path pomPath = tempDir.resolve("pom.xml");
    Files.writeString(
      pomPath,
      "<project><dependencies></dependencies></project>"
    );

    Path pkgPath = tempDir.resolve("package.json");
    Files.writeString(pkgPath, "{\"name\": \"java-web-hybrid\"}");

    // Manifests in order where package.json comes AFTER pom.xml
    ScannedFileMap fileMap = ScannedFileMap.builder()
      .rootPath(tempDir)
      .extensionCounts(Map.of(".java", 15, ".ts", 2))
      .manifestFiles(List.of(pomPath, pkgPath))
      .build();

    TechnologyStack techStack = stage.detect(fileMap);

    assertNotNull(techStack);
    assertEquals("Java", techStack.getMainLanguage());
    assertEquals("Maven", techStack.getBuildTool());
  }
}
