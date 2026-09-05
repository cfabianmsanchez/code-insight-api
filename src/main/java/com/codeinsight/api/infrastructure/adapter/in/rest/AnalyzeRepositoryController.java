package com.codeinsight.api.infrastructure.adapter.in.rest;

import com.codeinsight.api.application.port.in.AnalyzeRepositoryUseCase;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.GithubAnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.RepositoryAnalysisResponseDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.mapper.AnalyzeRepositoryRestMapper;
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

/**
 * Adaptador de Entrada REST (Inbound REST Adapter): Controlador de Análisis de Repositorios.
 * 
 * Expone endpoints HTTP para iniciar el análisis efímero de ingeniería inversa desde
 * repositorios públicos de GitHub o archivos subidos en formato .ZIP.
 */
@RestController
@RequestMapping("/api/v1/analyses")
@CrossOrigin(origins = "*")
@Tag(name = "Repository Analysis Pipeline", description = "Endpoints for executing the automated reverse engineering pipeline")
public class AnalyzeRepositoryController {

    private final AnalyzeRepositoryUseCase analyzeRepositoryUseCase;
    private final AnalyzeRepositoryRestMapper mapper;

    /**
     * Inyecta las dependencias necesarias para procesar las peticiones REST.
     */
    public AnalyzeRepositoryController(AnalyzeRepositoryUseCase analyzeRepositoryUseCase,
                                       AnalyzeRepositoryRestMapper mapper) {
        this.analyzeRepositoryUseCase = analyzeRepositoryUseCase;
        this.mapper = mapper;
    }

    /**
     * Endpoint para analizar un repositorio público de GitHub mediante su URL.
     *
     * @param requestDto DTO con el projectKey y la URL del repositorio Git.
     * @return {@link ResponseEntity} con la respuesta detallada del análisis.
     */
    @PostMapping("/github")
    @Operation(summary = "Analyze GitHub repository", description = "Clones a public GitHub repo, scans files, detects technology stack, and cleans up efimerally.")
    public ResponseEntity<RepositoryAnalysisResponseDto> analyzeGithubRepository(@Valid @RequestBody GithubAnalysisRequestDto requestDto) {
        FetchCodeRequest domainRequest = mapper.toDomain(requestDto);
        RepositoryAnalysisResult result = analyzeRepositoryUseCase.analyzeRepository(domainRequest);
        return ResponseEntity.ok(mapper.toResponseDto(result));
    }

    /**
     * Endpoint para analizar un archivo cargado en formato comprimido .ZIP.
     *
     * @param projectKey Clave o identificador asignado al proyecto.
     * @param file       Archivo .ZIPMultipart subido en la petición.
     * @return {@link ResponseEntity} con la respuesta detallada del análisis.
     * @throws IOException Si ocurre un error de E/S al procesar el stream del archivo.
     */
    @PostMapping(value = "/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Analyze .ZIP repository file", description = "Unpacks an uploaded .ZIP file, scans files, detects technology stack, and cleans up efimerally.")
    public ResponseEntity<RepositoryAnalysisResponseDto> analyzeZipFile(
            @RequestParam("projectKey") String projectKey,
            @RequestParam("file") MultipartFile file) throws IOException {

        FetchCodeRequest domainRequest = mapper.toDomain(projectKey, file);
        RepositoryAnalysisResult result = analyzeRepositoryUseCase.analyzeRepository(domainRequest);
        return ResponseEntity.ok(mapper.toResponseDto(result));
    }
}
