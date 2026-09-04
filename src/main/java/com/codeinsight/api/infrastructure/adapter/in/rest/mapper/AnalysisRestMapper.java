package com.codeinsight.api.infrastructure.adapter.in.rest.mapper;

import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.AnalysisType;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.AnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.AnalysisResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AnalysisRestMapper {

    public CodeAnalysisRequest toDomain(AnalysisRequestDto dto) {
        AnalysisType type;
        try {
            type = AnalysisType.valueOf(dto.getType().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            type = AnalysisType.GENERIC;
        }

        return CodeAnalysisRequest.builder()
                .projectKey(dto.getProjectKey())
                .sourceCode(dto.getSourceCode())
                .type(type)
                .metadata(dto.getMetadata())
                .build();
    }

    public AnalysisResponseDto toResponseDto(AnalysisReport report) {
        return AnalysisResponseDto.builder()
                .id(report.getId())
                .projectKey(report.getProjectKey())
                .analysisType(report.getAnalysisType() != null ? report.getAnalysisType().name() : null)
                .linesOfCode(report.getLinesOfCode())
                .cyclomaticComplexity(report.getCyclomaticComplexity())
                .estimatedBugs(report.getEstimatedBugs())
                .securityVulnerabilities(report.getSecurityVulnerabilities())
                .summary(report.getSummary())
                .recommendations(report.getRecommendations())
                .metrics(report.getMetrics())
                .timestamp(report.getTimestamp())
                .build();
    }
}
