package com.codeinsight.api.domain.strategy;

import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.AnalysisType;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JavaCodeAnalysisStrategy implements CodeAnalysisStrategy {

    @Override
    public boolean supports(AnalysisType analysisType) {
        return AnalysisType.JAVA.equals(analysisType);
    }

    @Override
    public AnalysisReport analyze(CodeAnalysisRequest request) {
        String code = request.getSourceCode() != null ? request.getSourceCode() : "";
        int loc = code.split("\r\n|\r|\n").length;
        
        int classesCount = code.split("class ").length - 1;
        int methodsCount = code.split("void|public|private|protected").length - 1;
        int cyclomaticComplexity = Math.max(1, methodsCount * 2);

        return AnalysisReport.builder()
                .id(UUID.randomUUID().toString())
                .projectKey(request.getProjectKey())
                .analysisType(AnalysisType.JAVA)
                .linesOfCode(loc)
                .cyclomaticComplexity(cyclomaticComplexity)
                .estimatedBugs(loc > 100 ? loc / 50 : 0)
                .securityVulnerabilities(code.contains("System.out.println") ? 1 : 0)
                .summary("Java Code Analysis completed successfully.")
                .recommendations(List.of(
                        "Consider using SLF4J logger instead of System.out.println",
                        "Ensure classes have proper unit test coverage",
                        "Keep method length under 30 lines of code"
                ))
                .metrics(Map.of(
                        "classesDetected", Math.max(1, classesCount),
                        "methodsDetected", Math.max(1, methodsCount),
                        "languageVersion", "Java 17+"
                ))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
