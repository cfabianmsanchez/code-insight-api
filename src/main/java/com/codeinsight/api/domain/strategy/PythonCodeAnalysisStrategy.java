package com.codeinsight.api.domain.strategy;

import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.AnalysisType;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PythonCodeAnalysisStrategy implements CodeAnalysisStrategy {

    @Override
    public boolean supports(AnalysisType analysisType) {
        return AnalysisType.PYTHON.equals(analysisType);
    }

    @Override
    public AnalysisReport analyze(CodeAnalysisRequest request) {
        String code = request.getSourceCode() != null ? request.getSourceCode() : "";
        int loc = code.split("\r\n|\r|\n").length;
        
        int functionsCount = code.split("def ").length - 1;
        int cyclomaticComplexity = Math.max(1, functionsCount * 3);

        return AnalysisReport.builder()
                .id(UUID.randomUUID().toString())
                .projectKey(request.getProjectKey())
                .analysisType(AnalysisType.PYTHON)
                .linesOfCode(loc)
                .cyclomaticComplexity(cyclomaticComplexity)
                .estimatedBugs(code.contains("except:") ? 2 : 0)
                .securityVulnerabilities(code.contains("eval(") || code.contains("exec(") ? 1 : 0)
                .summary("Python Code Analysis completed successfully.")
                .recommendations(List.of(
                        "Avoid bare except clauses; catch specific exceptions",
                        "Follow PEP8 naming conventions for functions and variables",
                        "Use type hinting for function signatures"
                ))
                .metrics(Map.of(
                        "functionsDetected", Math.max(1, functionsCount),
                        "pep8ComplianceScore", 92,
                        "pythonVersion", "Python 3.x"
                ))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
