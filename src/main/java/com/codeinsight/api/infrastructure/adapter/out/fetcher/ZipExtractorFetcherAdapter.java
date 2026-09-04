package com.codeinsight.api.infrastructure.adapter.out.fetcher;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.SourceType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipExtractorFetcherAdapter implements CodeFetcherPort {

    @Override
    public boolean supports(FetchCodeRequest request) {
        return SourceType.ZIP_FILE.equals(request.getSourceType())
                && request.getZipInputStream() != null;
    }

    @Override
    public TempCodeDirectory fetchCode(FetchCodeRequest request) {
        try {
            Path tempDir = Files.createTempDirectory("code-insight-zip-");
            extractZipStream(request.getZipInputStream(), tempDir);
            return new TempCodeDirectory(tempDir, SourceType.ZIP_FILE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract ZIP file into temporary directory. Details: " + e.getMessage(), e);
        }
    }

    private void extractZipStream(InputStream inputStream, Path targetDir) throws IOException {
        byte[] buffer = new byte[8192];
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(targetDir.toFile(), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
