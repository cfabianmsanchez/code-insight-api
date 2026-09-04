package com.codeinsight.api.application.port.out;

import com.codeinsight.api.domain.model.AnalysisReport;

import java.util.Optional;

public interface SaveAnalysisReportPort {
    AnalysisReport save(AnalysisReport report);
    Optional<AnalysisReport> findById(String id);
}
