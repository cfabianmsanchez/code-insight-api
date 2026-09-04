package com.codeinsight.api.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

public class RepositoryAnalysisResult {
    private String projectKey;
    private SourceType sourceType;
    private int totalFiles;
    private int totalDirectories;
    private TechnologyStack technologyStack;
    private ComponentAnalysisResult componentAnalysis;
    private ArchitectureEvidenceResult architectureEvidence;
    private AnalysisContext analysisContext;
    private Map<String, Integer> extensionCounts;
    private LocalDateTime timestamp;

    public RepositoryAnalysisResult() {}

    public RepositoryAnalysisResult(String projectKey, SourceType sourceType, int totalFiles,
                                    int totalDirectories, TechnologyStack technologyStack,
                                    ComponentAnalysisResult componentAnalysis,
                                    ArchitectureEvidenceResult architectureEvidence,
                                    AnalysisContext analysisContext,
                                    Map<String, Integer> extensionCounts, LocalDateTime timestamp) {
        this.projectKey = projectKey;
        this.sourceType = sourceType;
        this.totalFiles = totalFiles;
        this.totalDirectories = totalDirectories;
        this.technologyStack = technologyStack;
        this.componentAnalysis = componentAnalysis;
        this.architectureEvidence = architectureEvidence;
        this.analysisContext = analysisContext;
        this.extensionCounts = extensionCounts;
        this.timestamp = timestamp;
    }

    public String getProjectKey() { return projectKey; }
    public SourceType getSourceType() { return sourceType; }
    public int getTotalFiles() { return totalFiles; }
    public int getTotalDirectories() { return totalDirectories; }
    public TechnologyStack getTechnologyStack() { return technologyStack; }
    public ComponentAnalysisResult getComponentAnalysis() { return componentAnalysis; }
    public ArchitectureEvidenceResult getArchitectureEvidence() { return architectureEvidence; }
    public AnalysisContext getAnalysisContext() { return analysisContext; }
    public Map<String, Integer> getExtensionCounts() { return extensionCounts; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String projectKey;
        private SourceType sourceType;
        private int totalFiles;
        private int totalDirectories;
        private TechnologyStack technologyStack;
        private ComponentAnalysisResult componentAnalysis;
        private ArchitectureEvidenceResult architectureEvidence;
        private AnalysisContext analysisContext;
        private Map<String, Integer> extensionCounts;
        private LocalDateTime timestamp;

        public Builder projectKey(String projectKey) { this.projectKey = projectKey; return this; }
        public Builder sourceType(SourceType sourceType) { this.sourceType = sourceType; return this; }
        public Builder totalFiles(int totalFiles) { this.totalFiles = totalFiles; return this; }
        public Builder totalDirectories(int totalDirectories) { this.totalDirectories = totalDirectories; return this; }
        public Builder technologyStack(TechnologyStack technologyStack) { this.technologyStack = technologyStack; return this; }
        public Builder componentAnalysis(ComponentAnalysisResult componentAnalysis) { this.componentAnalysis = componentAnalysis; return this; }
        public Builder architectureEvidence(ArchitectureEvidenceResult architectureEvidence) { this.architectureEvidence = architectureEvidence; return this; }
        public Builder analysisContext(AnalysisContext analysisContext) { this.analysisContext = analysisContext; return this; }
        public Builder extensionCounts(Map<String, Integer> extensionCounts) { this.extensionCounts = extensionCounts; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public RepositoryAnalysisResult build() {
            return new RepositoryAnalysisResult(projectKey, sourceType, totalFiles, totalDirectories, technologyStack, componentAnalysis, architectureEvidence, analysisContext, extensionCounts, timestamp);
        }
    }
}
