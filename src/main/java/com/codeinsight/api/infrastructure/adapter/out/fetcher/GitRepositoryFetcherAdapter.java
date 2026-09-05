package com.codeinsight.api.infrastructure.adapter.out.fetcher;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.exception.RepositoryFetchException;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.SourceType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        return SourceType.GITHUB_REPO.equals(request.getSourceType())
                && request.getRepoUrl() != null
                && !request.getRepoUrl().isBlank();
    }

    /**
     * Clona efímeramente el repositorio de GitHub en una carpeta temporal con profundidad 1.
     *
     * @throws RepositoryFetchException Si la clonación falla por problemas de red o URL inválida.
     */
    @Override
    public TempCodeDirectory fetchCode(FetchCodeRequest request) {
        try {
            Path tempDir = Files.createTempDirectory("code-insight-git-");
            
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
        } catch (IOException | GitAPIException e) {
            throw new RepositoryFetchException("Failed to clone GitHub repository from URL: " + request.getRepoUrl() + ". Details: " + e.getMessage(), e);
        }
    }
}
