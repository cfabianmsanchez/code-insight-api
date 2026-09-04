package com.codeinsight.api.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;

public class FetchCodeResponseDto {
    private String projectKey;
    private String sourceType;
    private int filesExtracted;
    private int directoriesCreated;
    private String message;
    private LocalDateTime timestamp;

    public FetchCodeResponseDto() {}

    public FetchCodeResponseDto(String projectKey, String sourceType, int filesExtracted,
                                int directoriesCreated, String message, LocalDateTime timestamp) {
        this.projectKey = projectKey;
        this.sourceType = sourceType;
        this.filesExtracted = filesExtracted;
        this.directoriesCreated = directoriesCreated;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getProjectKey() { return projectKey; }
    public String getSourceType() { return sourceType; }
    public int getFilesExtracted() { return filesExtracted; }
    public int getDirectoriesCreated() { return directoriesCreated; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String projectKey;
        private String sourceType;
        private int filesExtracted;
        private int directoriesCreated;
        private String message;
        private LocalDateTime timestamp;

        public Builder projectKey(String projectKey) { this.projectKey = projectKey; return this; }
        public Builder sourceType(String sourceType) { this.sourceType = sourceType; return this; }
        public Builder filesExtracted(int filesExtracted) { this.filesExtracted = filesExtracted; return this; }
        public Builder directoriesCreated(int directoriesCreated) { this.directoriesCreated = directoriesCreated; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public FetchCodeResponseDto build() {
            return new FetchCodeResponseDto(projectKey, sourceType, filesExtracted, directoriesCreated, message, timestamp);
        }
    }
}
