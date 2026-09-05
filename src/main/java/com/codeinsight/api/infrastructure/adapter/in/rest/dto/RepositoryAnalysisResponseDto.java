package com.codeinsight.api.infrastructure.adapter.in.rest.dto;

import com.codeinsight.api.domain.model.AnalysisContext;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.TechnologyStack;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta que contiene el resultado consolidado del análisis de ingeniería inversa.
 */
public class RepositoryAnalysisResponseDto {
    /** Identificador del proyecto analizado. */
    private String projectKey;
    /** Tipo de fuente procesada (GITHUB_REPO o ZIP_FILE). */
    private String sourceType;
    /** Conteo total de archivos escaneados. */
    private int totalFiles;
    /** Conteo total de directorios. */
    private int totalDirectories;
    /** Stack tecnológico detectado. */
    private TechnologyStack technologyStack;
    /** Análisis de componentes y estereotipos. */
    private ComponentAnalysisResult componentAnalysis;
    /** Evidencias y señales de arquitectura. */
    private ArchitectureEvidenceResult architectureEvidence;
    /** Contexto de prompts estructurados para el motor de IA. */
    private AnalysisContext analysisContext;
    /** Síntesis técnica y evaluación del modelo de IA (Ollama). */
    private String aiSynthesis;
    /** Resumen del propósito funcional extraído del análisis de IA. */
    private String functionalSummary;
    /** Distribución de conteo por extensión de archivo. */
    private Map<String, Integer> extensionCounts;
    /** Marca de tiempo de conclusión del análisis. */
    private LocalDateTime timestamp;

    public RepositoryAnalysisResponseDto() {}

    public RepositoryAnalysisResponseDto(String projectKey, String sourceType, int totalFiles,
                                         int totalDirectories, TechnologyStack technologyStack,
                                         ComponentAnalysisResult componentAnalysis,
                                         ArchitectureEvidenceResult architectureEvidence,
                                         AnalysisContext analysisContext, String aiSynthesis,
                                         String functionalSummary,
                                         Map<String, Integer> extensionCounts, LocalDateTime timestamp) {
        this.projectKey = projectKey;
        this.sourceType = sourceType;
        this.totalFiles = totalFiles;
        this.totalDirectories = totalDirectories;
        this.technologyStack = technologyStack;
        this.componentAnalysis = componentAnalysis;
        this.architectureEvidence = architectureEvidence;
        this.analysisContext = analysisContext;
        this.aiSynthesis = aiSynthesis;
        this.functionalSummary = functionalSummary;
        this.extensionCounts = extensionCounts;
        this.timestamp = timestamp;
    }

    public String getProjectKey() { return projectKey; }
    public String getSourceType() { return sourceType; }
    public int getTotalFiles() { return totalFiles; }
    public int getTotalDirectories() { return totalDirectories; }
    public TechnologyStack getTechnologyStack() { return technologyStack; }
    public ComponentAnalysisResult getComponentAnalysis() { return componentAnalysis; }
    public ArchitectureEvidenceResult getArchitectureEvidence() { return architectureEvidence; }
    public AnalysisContext getAnalysisContext() { return analysisContext; }
    public String getAiSynthesis() { return aiSynthesis; }
    public String getFunctionalSummary() { return functionalSummary; }
    public Map<String, Integer> getExtensionCounts() { return extensionCounts; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String projectKey;
        private String sourceType;
        private int totalFiles;
        private int totalDirectories;
        private TechnologyStack technologyStack;
        private ComponentAnalysisResult componentAnalysis;
        private ArchitectureEvidenceResult architectureEvidence;
        private AnalysisContext analysisContext;
        private String aiSynthesis;
        private String functionalSummary;
        private Map<String, Integer> extensionCounts;
        private LocalDateTime timestamp;

        public Builder projectKey(String projectKey) { this.projectKey = projectKey; return this; }
        public Builder sourceType(String sourceType) { this.sourceType = sourceType; return this; }
        public Builder totalFiles(int totalFiles) { this.totalFiles = totalFiles; return this; }
        public Builder totalDirectories(int totalDirectories) { this.totalDirectories = totalDirectories; return this; }
        public Builder technologyStack(TechnologyStack technologyStack) { this.technologyStack = technologyStack; return this; }
        public Builder componentAnalysis(ComponentAnalysisResult componentAnalysis) { this.componentAnalysis = componentAnalysis; return this; }
        public Builder architectureEvidence(ArchitectureEvidenceResult architectureEvidence) { this.architectureEvidence = architectureEvidence; return this; }
        public Builder analysisContext(AnalysisContext analysisContext) { this.analysisContext = analysisContext; return this; }
        public Builder aiSynthesis(String aiSynthesis) { this.aiSynthesis = aiSynthesis; return this; }
        public Builder functionalSummary(String functionalSummary) { this.functionalSummary = functionalSummary; return this; }
        public Builder extensionCounts(Map<String, Integer> extensionCounts) { this.extensionCounts = extensionCounts; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public RepositoryAnalysisResponseDto build() {
            return new RepositoryAnalysisResponseDto(projectKey, sourceType, totalFiles, totalDirectories, technologyStack, componentAnalysis, architectureEvidence, analysisContext, aiSynthesis, functionalSummary, extensionCounts, timestamp);
        }
    }
}
