package com.codeinsight.api.infrastructure.adapter.in.rest;

import com.codeinsight.api.application.port.in.FetchCodeUseCase;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.FetchCodeResult;
import com.codeinsight.api.domain.model.SourceType;
import com.codeinsight.api.infrastructure.adapter.in.rest.dto.GithubAnalysisRequestDto;
import com.codeinsight.api.infrastructure.adapter.in.rest.mapper.FetchCodeRestMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodeFetcherController.class)
@Import(FetchCodeRestMapper.class)
class CodeFetcherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FetchCodeUseCase fetchCodeUseCase;

    @Test
    void fetchGithubRepository_shouldReturnOkResult() throws Exception {
        GithubAnalysisRequestDto dto = GithubAnalysisRequestDto.builder()
                .projectKey("github-repo-test")
                .repoUrl("https://github.com/octocat/Hello-World.git")
                .build();

        FetchCodeResult result = FetchCodeResult.builder()
                .projectKey("github-repo-test")
                .sourceType(SourceType.GITHUB_REPO)
                .filesExtracted(5)
                .directoriesCreated(2)
                .message("Fetched into temp folder")
                .timestamp(LocalDateTime.now())
                .build();

        when(fetchCodeUseCase.fetchAndProcess(any(FetchCodeRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/fetch/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("github-repo-test"))
                .andExpect(jsonPath("$.sourceType").value("GITHUB_REPO"))
                .andExpect(jsonPath("$.filesExtracted").value(5));
    }

    @Test
    void extractZipFile_shouldReturnOkResult() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", new byte[]{1, 2, 3});

        FetchCodeResult result = FetchCodeResult.builder()
                .projectKey("zip-repo-test")
                .sourceType(SourceType.ZIP_FILE)
                .filesExtracted(10)
                .directoriesCreated(3)
                .message("Extracted into temp folder")
                .timestamp(LocalDateTime.now())
                .build();

        when(fetchCodeUseCase.fetchAndProcess(any(FetchCodeRequest.class))).thenReturn(result);

        mockMvc.perform(multipart("/api/v1/fetch/zip")
                        .file(file)
                        .param("projectKey", "zip-repo-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("zip-repo-test"))
                .andExpect(jsonPath("$.sourceType").value("ZIP_FILE"))
                .andExpect(jsonPath("$.filesExtracted").value(10));
    }
}
