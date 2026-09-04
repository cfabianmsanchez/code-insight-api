package com.codeinsight.api.infrastructure.adapter.in.rest;

import com.codeinsight.api.application.port.in.FetchCodeUseCase;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.FetchCodeResult;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.FetchCodeResponseDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.GithubAnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.mapper.FetchCodeRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/fetch")
@CrossOrigin(origins = "*")
@Tag(name = "Code Fetcher", description = "Endpoints for temporary code cloning and zip extraction")
public class CodeFetcherController {

    private final FetchCodeUseCase fetchCodeUseCase;
    private final FetchCodeRestMapper mapper;

    public CodeFetcherController(FetchCodeUseCase fetchCodeUseCase, FetchCodeRestMapper mapper) {
        this.fetchCodeUseCase = fetchCodeUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/github")
    @Operation(summary = "Clone GitHub repository into temporary folder", description = "Clones a public GitHub repo URL into a temporary directory, counts extracted files, and immediately cleans up.")
    public ResponseEntity<FetchCodeResponseDto> fetchGithubRepository(@Valid @RequestBody GithubAnalysisRequestDto requestDto) {
        FetchCodeRequest request = mapper.toDomain(requestDto);
        FetchCodeResult result = fetchCodeUseCase.fetchAndProcess(request);
        return ResponseEntity.ok(mapper.toResponseDto(result));
    }

    @PostMapping(value = "/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Unpack .ZIP file into temporary folder", description = "Extracts an uploaded .ZIP file into a temporary directory, counts extracted files, and immediately cleans up.")
    public ResponseEntity<FetchCodeResponseDto> extractZipFile(
            @RequestParam("projectKey") String projectKey,
            @RequestParam("file") MultipartFile file) throws IOException {

        FetchCodeRequest request = mapper.toDomain(projectKey, file);
        FetchCodeResult result = fetchCodeUseCase.fetchAndProcess(request);
        return ResponseEntity.ok(mapper.toResponseDto(result));
    }
}
