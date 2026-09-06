package com.codeinsight.api.infrastructure.adapter.out.fetcher;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.exception.RepositoryFetchException;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.SourceType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

/**
 * Adaptador de Salida (Outbound Adapter): Cargador de Archivos ZIP.
 *
 * Implementa {@link CodeFetcherPort} descomprimiendo el contenido de un stream de archivo ZIP
 * en una carpeta temporal efímera, omitiendo archivos y carpetas de ruido de macOS (__MACOSX, ._).
 * Utiliza {@link ZipFile} para soportar descriptores de extensión (EXT descriptors) de macOS Finder y 7-Zip.
 */
@Component
public class ZipExtractorFetcherAdapter implements CodeFetcherPort {

  /**
   * Comprueba si la solicitud incluye un InputStream válido de archivo ZIP.
   */
  @Override
  public boolean supports(FetchCodeRequest request) {
    return (
      SourceType.ZIP_FILE.equals(request.getSourceType()) &&
      request.getZipInputStream() != null
    );
  }

  /**
   * Extrae el contenido del archivo ZIP en una carpeta temporal efímera.
   * Si la extracción falla, elimina inmediatamente la carpeta temporal para evitar fugas en disco.
   *
   * @throws RepositoryFetchException Si falla la descompresión o la creación de carpetas.
   */
  @Override
  public TempCodeDirectory fetchCode(FetchCodeRequest request) {
    Path tempDir = null;
    try {
      tempDir = Files.createTempDirectory("code-insight-zip-");
      extractZipStream(request.getZipInputStream(), tempDir);
      return new TempCodeDirectory(tempDir, SourceType.ZIP_FILE);
    } catch (Exception e) {
      deleteQuietly(tempDir);
      if (e instanceof RepositoryFetchException rfe) {
        throw rfe;
      }
      throw new RepositoryFetchException(
        "Failed to extract ZIP file into temporary directory. Details: " +
          e.getMessage(),
        e
      );
    }
  }

  /**
   * Recorre el archivo ZIP guardándolo temporalmente en disco y procesándolo con {@link ZipFile}
   * para soportar todos los formatos de compresión, descriptores EXT (macOS Finder, 7-Zip)
   * y prevención de Zip Slip.
   */
  private void extractZipStream(InputStream inputStream, Path targetDir)
    throws IOException {
    Path tempZipFile = Files.createTempFile("code-insight-upload-", ".zip");
    try {
      Files.copy(
        inputStream,
        tempZipFile,
        java.nio.file.StandardCopyOption.REPLACE_EXISTING
      );
      byte[] buffer = new byte[8192];
      try (ZipFile zipFile = new ZipFile(tempZipFile.toFile())) {
        var entries = zipFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry zipEntry = entries.nextElement();
          String entryName = zipEntry.getName();
          String fileNameOnly = new File(entryName).getName();
          if (entryName.contains("__MACOSX") || fileNameOnly.startsWith("._")) {
            continue;
          }

          File newFile = newFile(targetDir.toFile(), zipEntry);
          if (zipEntry.isDirectory()) {
            if (!newFile.isDirectory() && !newFile.mkdirs()) {
              throw new RepositoryFetchException(
                "Failed to create directory " + newFile
              );
            }
          } else {
            File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
              throw new RepositoryFetchException(
                "Failed to create directory " + parent
              );
            }
            try (
              InputStream entryStream = zipFile.getInputStream(zipEntry);
              FileOutputStream fos = new FileOutputStream(newFile)
            ) {
              int len;
              while ((len = entryStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
              }
            }
          }
        }
      }
    } finally {
      try {
        Files.deleteIfExists(tempZipFile);
      } catch (IOException ignored) {}
    }
  }

  /**
   * Valida la ruta de destino para prevenir vulnerabilidades de descompresión (Zip Slip).
   */
  private File newFile(File destinationDir, ZipEntry zipEntry)
    throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());
    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new RepositoryFetchException(
        "Zip entry is outside of the target dir (Zip Slip vulnerability attempt): " +
          zipEntry.getName()
      );
    }
    return destFile;
  }

  /**
   * Elimina el directorio temporal de forma silenciosa en caso de error durante la descompresión.
   */
  private void deleteQuietly(Path path) {
    if (path != null && Files.exists(path)) {
      try {
        FileSystemUtils.deleteRecursively(path);
      } catch (Exception ignored) {
        // Silencioso para no opacar la excepción original
      }
    }
  }
}
