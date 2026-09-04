package com.codeinsight.api.application.service;

import com.codeinsight.api.application.port.out.SaveAnalysisReportPort;
import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.AnalysisType;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;
import com.codeinsight.api.domain.strategy.CodeAnalysisStrategy;
import com.codeinsight.api.domain.strategy.CodeAnalysisStrategyFactory;
import com.codeinsight.api.domain.strategy.JavaCodeAnalysisStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeCodeServiceTest {

    @Mock
    private SaveAnalysisReportPort saveAnalysisReportPort;

    private AnalyzeCodeService analyzeCodeService;

    @BeforeEach
    void setUp() {
        List<CodeAnalysisStrategy> strategies = List.of(new JavaCodeAnalysisStrategy());
        CodeAnalysisStrategyFactory factory = new CodeAnalysisStrategyFactory(strategies);
        analyzeCodeService = new AnalyzeCodeService(factory, saveAnalysisReportPort);
    }

    @Test
    void analyzeCode_shouldSelectJavaStrategyAndSaveReport() {
        CodeAnalysisRequest request = CodeAnalysisRequest.builder()
                .projectKey("test-project")
                .sourceCode("public class Test { public static void main(String[] args) {} }")
                .type(AnalysisType.JAVA)
                .build();

        when(saveAnalysisReportPort.save(any(AnalysisReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisReport result = analyzeCodeService.analyzeCode(request);

        assertNotNull(result);
        assertEquals("test-project", result.getProjectKey());
        assertEquals(AnalysisType.JAVA, result.getAnalysisType());
        verify(saveAnalysisReportPort).save(any(AnalysisReport.class));
    }
}
