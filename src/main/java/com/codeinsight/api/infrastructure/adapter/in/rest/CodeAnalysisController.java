package com.codeinsight.api.infrastructure.adapter.in.rest;

import com.codeinsight.api.application.port.in.AnalyzeCodeUseCase;
import com.codeinsight.api.domain.model.AnalysisReport;
import com.codeinsight.api.domain.model.CodeAnalysisRequest;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.AnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.AnalysisResponseDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.mapper.AnalysisRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
@CrossOrigin(origins = "*")
@Tag(name = "Code Analysis", description = "Endpoints for inline code analysis and insights")
public class CodeAnalysisController {

    private final AnalyzeCodeUseCase analyzeCodeUseCase;
    private final AnalysisRestMapper mapper;

    public CodeAnalysisController(AnalyzeCodeUseCase analyzeCodeUseCase, AnalysisRestMapper mapper) {
        this.analyzeCodeUseCase = analyzeCodeUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Analyze inline code snippet", description = "Submits an inline code snippet analysis request.")
    public ResponseEntity<AnalysisResponseDto> analyzeCode(@Valid @RequestBody AnalysisRequestDto requestDto) {
        CodeAnalysisRequest domainRequest = mapper.toDomain(requestDto);
        AnalysisReport report = analyzeCodeUseCase.analyzeCode(domainRequest);
        AnalysisResponseDto responseDto = mapper.toResponseDto(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
