package com.codeinsight.api.infrastructure.adapter.out.persistence;

import com.codeinsight.api.application.port.out.SaveAnalysisReportPort;
import com.codeinsight.api.domain.model.AnalysisReport;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryReportPersistenceAdapter implements SaveAnalysisReportPort {

    private final Map<String, AnalysisReport> storage = new ConcurrentHashMap<>();

    @Override
    public AnalysisReport save(AnalysisReport report) {
        storage.put(report.getId(), report);
        return report;
    }

    @Override
    public Optional<AnalysisReport> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }
}
