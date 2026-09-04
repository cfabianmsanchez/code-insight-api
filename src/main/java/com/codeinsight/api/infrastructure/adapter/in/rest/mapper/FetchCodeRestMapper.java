package com.codeinsight.api.infrastructure.adapter.in.rest.mapper;

import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.FetchCodeResult;
import com.codeinsight.api.domain.model.SourceType;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.FetchCodeResponseDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.GithubAnalysisRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FetchCodeRestMapper {

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

    public FetchCodeResponseDto toResponseDto(FetchCodeResult result) {
        return FetchCodeResponseDto.builder()
                .projectKey(result.getProjectKey())
                .sourceType(result.getSourceType() != null ? result.getSourceType().name() : null)
                .filesExtracted(result.getFilesExtracted())
                .directoriesCreated(result.getDirectoriesCreated())
                .message(result.getMessage())
                .timestamp(result.getTimestamp())
                .build();
    }
}
