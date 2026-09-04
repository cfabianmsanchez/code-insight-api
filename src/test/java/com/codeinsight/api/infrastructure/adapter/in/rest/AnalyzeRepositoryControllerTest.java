package com.codeinsight.api.infrastructure.adapter.in.rest;

import com.codeinsight.api.application.port.in.AnalyzeRepositoryUseCase;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.RepositoryAnalysisResult;
import com.codeinsight.api.domain.model.SourceType;
import com.codeinsight.api.domain.model.TechnologyStack;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.GithubAnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.mapper.AnalyzeRepositoryRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyzeRepositoryController.class)
@Import(AnalyzeRepositoryRestMapper.class)
class AnalyzeRepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalyzeRepositoryUseCase analyzeRepositoryUseCase;

    @Test
    void analyzeGithubRepository_shouldReturnAnalysisResult() throws Exception {
        GithubAnalysisRequestDto dto = GithubAnalysisRequestDto.builder()
                .projectKey("pipeline-github-test")
                .repoUrl("https://github.com/spring-projects/spring-petclinic.git")
                .build();

        TechnologyStack techStack = TechnologyStack.builder()
                .mainLanguage("Java")
                .mainFramework("Spring Boot")
                .buildTool("Maven")
                .databasesDetected(List.of("PostgreSQL"))
                .build();

        RepositoryAnalysisResult mockResult = RepositoryAnalysisResult.builder()
                .projectKey("pipeline-github-test")
                .sourceType(SourceType.GITHUB_REPO)
                .totalFiles(45)
                .totalDirectories(12)
                .technologyStack(techStack)
                .extensionCounts(Map.of(".java", 30))
                .timestamp(LocalDateTime.now())
                .build();

        when(analyzeRepositoryUseCase.analyzeRepository(any(FetchCodeRequest.class))).thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/analyses/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("pipeline-github-test"))
                .andExpect(jsonPath("$.technologyStack.mainLanguage").value("Java"))
                .andExpect(jsonPath("$.technologyStack.mainFramework").value("Spring Boot"));
    }

    @Test
    void analyzeZipFile_shouldReturnAnalysisResult() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "project.zip", "application/zip", new byte[]{1, 2, 3});

        TechnologyStack techStack = TechnologyStack.builder()
                .mainLanguage("TypeScript")
                .mainFramework("Angular")
                .buildTool("npm / Node.js")
                .build();

        RepositoryAnalysisResult mockResult = RepositoryAnalysisResult.builder()
                .projectKey("pipeline-zip-test")
                .sourceType(SourceType.ZIP_FILE)
                .totalFiles(20)
                .totalDirectories(5)
                .technologyStack(techStack)
                .extensionCounts(Map.of(".ts", 15))
                .timestamp(LocalDateTime.now())
                .build();

        when(analyzeRepositoryUseCase.analyzeRepository(any(FetchCodeRequest.class))).thenReturn(mockResult);

        mockMvc.perform(multipart("/api/v1/analyses/zip")
                        .file(file)
                        .param("projectKey", "pipeline-zip-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("pipeline-zip-test"))
                .andExpect(jsonPath("$.technologyStack.mainLanguage").value("TypeScript"))
                .andExpect(jsonPath("$.technologyStack.mainFramework").value("Angular"));
    }
}
