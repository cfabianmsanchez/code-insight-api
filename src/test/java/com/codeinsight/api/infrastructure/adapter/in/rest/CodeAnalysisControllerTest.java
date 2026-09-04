package com.codeinsight.api.infrastructure.adapter.in.rest;

import com.codeinsight.api.application.port.in.AnalyzeCodeUseCase;
import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.AnalysisType;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.AnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.mapper.AnalysisRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodeAnalysisController.class)
@Import(AnalysisRestMapper.class)
class CodeAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalyzeCodeUseCase analyzeCodeUseCase;

    @Test
    void analyzeCode_shouldReturnCreatedReport() throws Exception {
        AnalysisRequestDto requestDto = AnalysisRequestDto.builder()
                .projectKey("my-java-app")
                .sourceCode("public class App {}")
                .type("JAVA")
                .build();

        AnalysisReport mockReport = AnalysisReport.builder()
                .id("report-123")
                .projectKey("my-java-app")
                .analysisType(AnalysisType.JAVA)
                .linesOfCode(1)
                .cyclomaticComplexity(2)
                .summary("Analysis finished")
                .recommendations(List.of("Rec 1"))
                .timestamp(LocalDateTime.now())
                .build();

        when(analyzeCodeUseCase.analyzeCode(any(CodeAnalysisRequest.class))).thenReturn(mockReport);

        mockMvc.perform(post("/api/v1/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("report-123"))
                .andExpect(jsonPath("$.projectKey").value("my-java-app"))
                .andExpect(jsonPath("$.analysisType").value("JAVA"));
    }
}
