package com.codeinsight.api.domain.model;

import java.io.InputStream;

/**
 * Modelo de Dominio: Solicitud de Adquisición de Código.
 *
 * Contiene los parámetros necesarios para adquirir el repositorio fuente
 * (projectKey, tipo de fuente, URL de Git o stream de archivo ZIP).
 */
public class FetchCodeRequest {

  /** Clave o identificador asignado al proyecto. */
  private String projectKey;
  /** Tipo de fuente del código (GITHUB_REPO o ZIP_FILE). */
  private SourceType sourceType;
  /** URL pública del repositorio de GitHub (si aplica). */
  private String repoUrl;
  /** Stream del archivo ZIP cargado (si aplica). */
  private InputStream zipInputStream;

  public FetchCodeRequest() {}

  public FetchCodeRequest(
    String projectKey,
    SourceType sourceType,
    String repoUrl,
    InputStream zipInputStream
  ) {
    this.projectKey = projectKey;
    this.sourceType = sourceType;
    this.repoUrl = repoUrl;
    this.zipInputStream = zipInputStream;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }

  public SourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(SourceType sourceType) {
    this.sourceType = sourceType;
  }

  public String getRepoUrl() {
    return repoUrl;
  }

  public void setRepoUrl(String repoUrl) {
    this.repoUrl = repoUrl;
  }

  public InputStream getZipInputStream() {
    return zipInputStream;
  }

  public void setZipInputStream(InputStream zipInputStream) {
    this.zipInputStream = zipInputStream;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String projectKey;
    private SourceType sourceType;
    private String repoUrl;
    private InputStream zipInputStream;

    public Builder projectKey(String projectKey) {
      this.projectKey = projectKey;
      return this;
    }

    public Builder sourceType(SourceType sourceType) {
      this.sourceType = sourceType;
      return this;
    }

    public Builder repoUrl(String repoUrl) {
      this.repoUrl = repoUrl;
      return this;
    }

    public Builder zipInputStream(InputStream zipInputStream) {
      this.zipInputStream = zipInputStream;
      return this;
    }

    public FetchCodeRequest build() {
      return new FetchCodeRequest(
        projectKey,
        sourceType,
        repoUrl,
        zipInputStream
      );
    }
  }
}
