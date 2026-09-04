package com.codeinsight.api.infrastructure.adapter.in.rest.mapper;

import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;
import com.codeinsight.api.domain.model.SourceType;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.GithubAnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.RepositoryAnalysisResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class AnalyzeRepositoryRestMapper {

    public FetchCodeRequest toDomain(GithubAnalysisRequestDto dto) {
        return FetchCodeRequest.builder()
                .projectKey(dto.getProjectKey())
                .sourceType(SourceType.GITHUB_REPO)
                .repoUrl(dto.getRepoUrl())
                .build();
    }

    public FetchCodeRequest toDomain(String projectKey, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded ZIP file cannot be empty");
        }
        return FetchCodeRequest.builder()
                .projectKey(projectKey)
                .sourceType(SourceType.ZIP_FILE)
                .zipInputStream(file.getInputStream())
                .build();
    }

    public RepositoryAnalysisResponseDto toResponseDto(RepositoryAnalysisResult result) {
        return RepositoryAnalysisResponseDto.builder()
                .projectKey(result.getProjectKey())
                .sourceType(result.getSourceType() != null ? result.getSourceType().name() : null)
                .totalFiles(result.getTotalFiles())
                .totalDirectories(result.getTotalDirectories())
                .technologyStack(result.getTechnologyStack())
                .componentAnalysis(result.getComponentAnalysis())
                .architectureEvidence(result.getArchitectureEvidence())
                .analysisContext(result.getAnalysisContext())
                .extensionCounts(result.getExtensionCounts())
                .timestamp(result.getTimestamp())
                .build();
    }
}
