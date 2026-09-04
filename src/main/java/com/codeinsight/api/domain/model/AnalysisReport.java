package com.codeinsight.api.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalysisReport {
    private String id;
    private String projectKey;
    private AnalysisType analysisType;
    private int linesOfCode;
    private int cyclomaticComplexity;
    private int estimatedBugs;
    private int securityVulnerabilities;
    private String summary;
    private List<String> recommendations;
    private Map<String, Object> metrics;
    private LocalDateTime timestamp;

    public AnalysisReport() {}

    public AnalysisReport(String id, String projectKey, AnalysisType analysisType, int linesOfCode,
                          int cyclomaticComplexity, int estimatedBugs, int securityVulnerabilities,
                          String summary, List<String> recommendations, Map<String, Object> metrics,
                          LocalDateTime timestamp) {
        this.id = id;
        this.projectKey = projectKey;
        this.analysisType = analysisType;
        this.linesOfCode = linesOfCode;
        this.cyclomaticComplexity = cyclomaticComplexity;
        this.estimatedBugs = estimatedBugs;
        this.securityVulnerabilities = securityVulnerabilities;
        this.summary = summary;
        this.recommendations = recommendations;
        this.metrics = metrics;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getProjectKey() { return projectKey; }
    public AnalysisType getAnalysisType() { return analysisType; }
    public int getLinesOfCode() { return linesOfCode; }
    public int getCyclomaticComplexity() { return cyclomaticComplexity; }
    public int getEstimatedBugs() { return estimatedBugs; }
    public int getSecurityVulnerabilities() { return securityVulnerabilities; }
    public String getSummary() { return summary; }
    public List<String> getRecommendations() { return recommendations; }
    public Map<String, Object> getMetrics() { return metrics; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String projectKey;
        private AnalysisType analysisType;
        private int linesOfCode;
        private int cyclomaticComplexity;
        private int estimatedBugs;
        private int securityVulnerabilities;
        private String summary;
        private List<String> recommendations;
        private Map<String, Object> metrics;
        private LocalDateTime timestamp;

        public Builder id(String id) { this.id = id; return this; }
        public Builder projectKey(String projectKey) { this.projectKey = projectKey; return this; }
        public Builder analysisType(AnalysisType analysisType) { this.analysisType = analysisType; return this; }
        public Builder linesOfCode(int linesOfCode) { this.linesOfCode = linesOfCode; return this; }
        public Builder cyclomaticComplexity(int cyclomaticComplexity) { this.cyclomaticComplexity = cyclomaticComplexity; return this; }
        public Builder estimatedBugs(int estimatedBugs) { this.estimatedBugs = estimatedBugs; return this; }
        public Builder securityVulnerabilities(int securityVulnerabilities) { this.securityVulnerabilities = securityVulnerabilities; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder recommendations(List<String> recommendations) { this.recommendations = recommendations; return this; }
        public Builder metrics(Map<String, Object> metrics) { this.metrics = metrics; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AnalysisReport build() {
            return new AnalysisReport(id, projectKey, analysisType, linesOfCode, cyclomaticComplexity,
                    estimatedBugs, securityVulnerabilities, summary, recommendations, metrics, timestamp);
        }
    }
}
