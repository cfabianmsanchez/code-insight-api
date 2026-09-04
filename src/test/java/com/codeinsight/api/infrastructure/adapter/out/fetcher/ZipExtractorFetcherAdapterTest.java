package com.codeinsight.api.infrastructure.adapter.out.fetcher;

import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.SourceType;
import com.codeinsight.api.domain.model.TempCodeDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipExtractorFetcherAdapterTest {

    private final ZipExtractorFetcherAdapter adapter = new ZipExtractorFetcherAdapter();

    @Test
    void fetchCode_shouldUnpackZipIntoTempDirectoryAndCleanupOnClose() throws IOException {
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

        // Después del bloque try-with-resources (close), la carpeta debe estar totalmente eliminada
        assertFalse(Files.exists(createdTempPath));
    }
}
