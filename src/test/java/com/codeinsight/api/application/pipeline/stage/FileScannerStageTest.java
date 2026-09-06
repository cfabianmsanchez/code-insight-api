package com.codeinsight.api.application.pipeline.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeinsight.api.domain.model.ScannedFileMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileScannerStageTest {

  private final FileScannerStage fileScannerStage = new FileScannerStage();

  @Test
  void scan_shouldScanFilesFilterNoiseAndDetectManifests(@TempDir Path tempDir)
    throws IOException {
    Path src = Files.createDirectories(
      tempDir.resolve("src/main/java/com/demo")
    );
    Files.writeString(
      src.resolve("UserController.java"),
      "public class UserController {}"
    );
    Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");

    Path gitDir = Files.createDirectories(tempDir.resolve(".git"));
    Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/main");

    Path targetDir = Files.createDirectories(tempDir.resolve("target"));
    Files.writeString(targetDir.resolve("App.class"), "class content");

    ScannedFileMap result = fileScannerStage.scan(tempDir);

    assertNotNull(result);
    assertEquals(2, result.getTotalFiles());
    assertEquals(1, result.getExtensionCounts().get(".java"));
    assertEquals(1, result.getExtensionCounts().get(".xml"));
    assertEquals(1, result.getManifestFiles().size());
    assertTrue(
      result
        .getManifestFiles()
        .get(0)
        .getFileName()
        .toString()
        .equals("pom.xml")
    );
  }

  @Test
  void scan_shouldIncludeHexagonalPortOutDirectory(@TempDir Path tempDir)
    throws IOException {
    Path portOut = Files.createDirectories(
      tempDir.resolve("src/main/java/com/demo/application/port/out")
    );
    Files.writeString(
      portOut.resolve("SomePort.java"),
      "public interface SomePort {}"
    );

    ScannedFileMap result = fileScannerStage.scan(tempDir);

    assertNotNull(result);
    assertTrue(
      result
        .getRelativeFilePaths()
        .stream()
        .anyMatch(path ->
          path.replace("\\", "/").contains("application/port/out/SomePort.java")
        )
    );
  }
}
