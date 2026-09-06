package com.codeinsight.api.infrastructure.adapter.out.fetcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.SourceType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class ZipExtractorFetcherAdapterTest {

  private final ZipExtractorFetcherAdapter adapter =
    new ZipExtractorFetcherAdapter();

  @Test
  void fetchCode_shouldUnpackZipIntoTempDirectoryAndCleanupOnClose()
    throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry entry = new ZipEntry("sample.txt");
      zos.putNextEntry(entry);
      zos.write("Hello Code Insight".getBytes());
      zos.closeEntry();
    }

    FetchCodeRequest request = FetchCodeRequest.builder()
      .projectKey("zip-test")
      .sourceType(SourceType.ZIP_FILE)
      .zipInputStream(new ByteArrayInputStream(baos.toByteArray()))
      .build();

    Path createdTempPath;
    try (TempCodeDirectory tempDir = adapter.fetchCode(request)) {
      assertNotNull(tempDir);
      createdTempPath = tempDir.getTempPath();
      assertTrue(Files.exists(createdTempPath));
      assertTrue(Files.exists(createdTempPath.resolve("sample.txt")));
    }

    assertFalse(Files.exists(createdTempPath));
  }

  @Test
  void fetchCode_shouldFilterMacOsFilesAndExtractNestedDirectories()
    throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      // Fichero normal en subcarpeta
      ZipEntry entry1 = new ZipEntry("src/app/app.component.ts");
      zos.putNextEntry(entry1);
      zos.write("export class AppComponent {}".getBytes());
      zos.closeEntry();

      // Fichero __MACOSX que debe ser ignorado
      ZipEntry macEntry = new ZipEntry("__MACOSX/._app.component.ts");
      zos.putNextEntry(macEntry);
      zos.write("mac-junk".getBytes());
      zos.closeEntry();
    }

    FetchCodeRequest request = FetchCodeRequest.builder()
      .projectKey("mac-zip-test")
      .sourceType(SourceType.ZIP_FILE)
      .zipInputStream(new ByteArrayInputStream(baos.toByteArray()))
      .build();

    Path createdTempPath;
    try (TempCodeDirectory tempDir = adapter.fetchCode(request)) {
      assertNotNull(tempDir);
      createdTempPath = tempDir.getTempPath();
      assertTrue(
        Files.exists(createdTempPath.resolve("src/app/app.component.ts"))
      );
      assertFalse(Files.exists(createdTempPath.resolve("__MACOSX")));
    }
    assertFalse(Files.exists(createdTempPath));
  }
}
