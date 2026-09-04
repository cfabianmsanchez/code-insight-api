package com.codeinsight.api.domain.model;

import java.util.Map;

public class CodeAnalysisRequest {
    private String projectKey;
    private String sourceCode;
    private AnalysisType type;
    private Map<String, Object> metadata;

    public CodeAnalysisRequest() {}

    public CodeAnalysisRequest(String projectKey, String sourceCode, AnalysisType type, Map<String, Object> metadata) {
        this.projectKey = projectKey;
        this.sourceCode = sourceCode;
        this.type = type;
        this.metadata = metadata;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public AnalysisType getType() {
        return type;
    }

    public void setType(AnalysisType type) {
        this.type = type;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String projectKey;
        private String sourceCode;
        private AnalysisType type;
        private Map<String, Object> metadata;

        public Builder projectKey(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }

        public Builder sourceCode(String sourceCode) {
            this.sourceCode = sourceCode;
            return this;
        }

        public Builder type(AnalysisType type) {
            this.type = type;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public CodeAnalysisRequest build() {
            return new CodeAnalysisRequest(projectKey, sourceCode, type, metadata);
        }
    }
}
