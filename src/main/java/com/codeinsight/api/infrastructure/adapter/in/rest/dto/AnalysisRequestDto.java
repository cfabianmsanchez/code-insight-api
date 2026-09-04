package com.codeinsight.api.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class AnalysisRequestDto {

    @NotBlank(message = "projectKey is required")
    private String projectKey;

    @NotBlank(message = "sourceCode is required")
    private String sourceCode;

    @NotBlank(message = "type is required (e.g., JAVA, PYTHON)")
    private String type;

    private Map<String, Object> metadata;

    public AnalysisRequestDto() {}

    public AnalysisRequestDto(String projectKey, String sourceCode, String type, Map<String, Object> metadata) {
        this.projectKey = projectKey;
        this.sourceCode = sourceCode;
        this.type = type;
        this.metadata = metadata;
    }

    public String getProjectKey() { return projectKey; }
    public void setProjectKey(String projectKey) { this.projectKey = projectKey; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String projectKey;
        private String sourceCode;
        private String type;
        private Map<String, Object> metadata;

        public Builder projectKey(String projectKey) { this.projectKey = projectKey; return this; }
        public Builder sourceCode(String sourceCode) { this.sourceCode = sourceCode; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

        public AnalysisRequestDto build() {
            return new AnalysisRequestDto(projectKey, sourceCode, type, metadata);
        }
    }
}
