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

@Component
public class GitRepositoryFetcherAdapter implements CodeFetcherPort {

    @Override
    public boolean supports(FetchCodeRequest request) {
        return SourceType.GITHUB_REPO.equals(request.getSourceType())
                && request.getRepoUrl() != null
                && !request.getRepoUrl().isBlank();
    }

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
