package com.codeinsight.api.infrastructure.adapter.out.fetcher;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.exception.RepositoryFetchException;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.SourceType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

/**
 * Adaptador de Salida (Outbound Adapter): Cargador de Repositorios Git.
 *
 * Implementa {@link CodeFetcherPort} utilizando JGit para clonar repositorios públicos
 * de GitHub en directorios temporales efímeros (con profundidad 1).
 */
@Component
public class GitRepositoryFetcherAdapter implements CodeFetcherPort {

  /**
   * Comprueba si la solicitud corresponde a una URL válida de repositorio GitHub.
   */
  @Override
  public boolean supports(FetchCodeRequest request) {
    return (
      SourceType.GITHUB_REPO.equals(request.getSourceType()) &&
      request.getRepoUrl() != null &&
      !request.getRepoUrl().isBlank()
    );
  }

  /**
   * Clona efímeramente el repositorio de GitHub en una carpeta temporal con profundidad 1.
   * Si la clonación falla, elimina inmediatamente la carpeta temporal para evitar fugas en disco.
   *
   * @throws RepositoryFetchException Si la clonación falla por problemas de red o URL inválida.
   */
  @Override
  public TempCodeDirectory fetchCode(FetchCodeRequest request) {
    Path tempDir = null;
    try {
      tempDir = Files.createTempDirectory("code-insight-git-");

      String repoUrl = request.getRepoUrl().trim();
      if (!repoUrl.endsWith(".git") && !repoUrl.contains("/archive/")) {
        repoUrl = repoUrl + ".git";
      }

      Git git = Git.cloneRepository()
        .setURI(repoUrl)
        .setDirectory(tempDir.toFile())
        .setDepth(1)
        .call();

      git.close();

      return new TempCodeDirectory(tempDir, SourceType.GITHUB_REPO);
    } catch (Exception e) {
      deleteQuietly(tempDir);
      throw new RepositoryFetchException(
        "Failed to clone GitHub repository from URL: " +
          request.getRepoUrl() +
          ". Details: " +
          e.getMessage(),
        e
      );
    }
  }

  /**
   * Elimina el directorio temporal de forma silenciosa en caso de error durante la descarga.
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
