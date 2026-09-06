package com.codeinsight.api.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para la solicitud de análisis de repositorios de GitHub via REST.
 */
public class GithubAnalysisRequestDto {

  /** Identificador único asignado al proyecto. */
  @NotBlank(message = "projectKey is required")
  private String projectKey;

  /** URL del repositorio público de GitHub a clonar. */
  @NotBlank(message = "repoUrl is required (e.g. https://github.com/user/repo)")
  private String repoUrl;

  public GithubAnalysisRequestDto() {}

  public GithubAnalysisRequestDto(String projectKey, String repoUrl) {
    this.projectKey = projectKey;
    this.repoUrl = repoUrl;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }

  public String getRepoUrl() {
    return repoUrl;
  }

  public void setRepoUrl(String repoUrl) {
    this.repoUrl = repoUrl;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String projectKey;
    private String repoUrl;

    public Builder projectKey(String projectKey) {
      this.projectKey = projectKey;
      return this;
    }

    public Builder repoUrl(String repoUrl) {
      this.repoUrl = repoUrl;
      return this;
    }

    public GithubAnalysisRequestDto build() {
      return new GithubAnalysisRequestDto(projectKey, repoUrl);
    }
  }
}
