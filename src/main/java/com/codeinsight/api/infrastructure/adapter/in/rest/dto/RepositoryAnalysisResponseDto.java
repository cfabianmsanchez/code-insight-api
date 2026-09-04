package com.codeinsight.api.infrastructure.adapter.in.rest.dto;

import com.codeinsight.api.domain.model.TechnologyStack;

import java.time.LocalDateTime;
import java.util.Map;

public class RepositoryAnalysisResponseDto {
    private String projectKey;
    private String sourceType;
    private int totalFiles;
    private int totalDirectories;
    private TechnologyStack technologyStack;
    private Map<String, Integer> extensionCounts;
    private LocalDateTime timestamp;

    public RepositoryAnalysisResponseDto() {}

    public RepositoryAnalysisResponseDto(String projectKey, String sourceType, int totalFiles,
                                         int totalDirectories, TechnologyStack technologyStack,
                                         Map<String, Integer> extensionCounts, LocalDateTime timestamp) {
        this.projectKey = projectKey;
        this.sourceType = sourceType;
        this.totalFiles = totalFiles;
        this.totalDirectories = totalDirectories;
        this.technologyStack = technologyStack;
        this.extensionCounts = extensionCounts;
        this.timestamp = timestamp;
    }

    public String getProjectKey() { return projectKey; }
    public String getSourceType() { return sourceType; }
    public int getTotalFiles() { return totalFiles; }
    public int getTotalDirectories() { return totalDirectories; }
    public TechnologyStack getTechnologyStack() { return technologyStack; }
    public Map<String, Integer> getExtensionCounts() { return extensionCounts; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String projectKey;
        private String sourceType;
        private int totalFiles;
        private int totalDirectories;
        private TechnologyStack technologyStack;
        private Map<String, Integer> extensionCounts;
        private LocalDateTime timestamp;

        public Builder projectKey(String projectKey) { this.projectKey = projectKey; return this; }
        public Builder sourceType(String sourceType) { this.sourceType = sourceType; return this; }
        public Builder totalFiles(int totalFiles) { this.totalFiles = totalFiles; return this; }
        public Builder totalDirectories(int totalDirectories) { this.totalDirectories = totalDirectories; return this; }
        public Builder technologyStack(TechnologyStack technologyStack) { this.technologyStack = technologyStack; return this; }
        public Builder extensionCounts(Map<String, Integer> extensionCounts) { this.extensionCounts = extensionCounts; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public RepositoryAnalysisResponseDto build() {
            return new RepositoryAnalysisResponseDto(projectKey, sourceType, totalFiles, totalDirectories, technologyStack, extensionCounts, timestamp);
        }
    }
}
