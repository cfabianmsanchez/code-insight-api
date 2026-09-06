package com.codeinsight.api.domain.model;

import java.util.Objects;

/**
 * Modelo de Dominio: Contexto de Análisis para el Motor de IA.
 *
 * Almacena los prompts estructurados generados en la Etapa 6 (systemPrompt, userPrompt y formattedContextPrompt)
 * listos para ser consumidos por el modelo de IA.
 */
public class AnalysisContext {

  /** Prompt del sistema con rol y directivas anti-alucinaciones. */
  private final String systemPrompt;
  /** Prompt del usuario con la radiografía factual del proyecto en Markdown. */
  private final String userPrompt;
  /** Prompt consolidado optimizado listo para enviar al LLM. */
  private final String formattedContextPrompt;

  public AnalysisContext(
    String systemPrompt,
    String userPrompt,
    String formattedContextPrompt
  ) {
    this.systemPrompt = systemPrompt;
    this.userPrompt = userPrompt;
    this.formattedContextPrompt = formattedContextPrompt;
  }

  public String getSystemPrompt() {
    return systemPrompt;
  }

  public String getUserPrompt() {
    return userPrompt;
  }

  public String getFormattedContextPrompt() {
    return formattedContextPrompt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String systemPrompt;
    private String userPrompt;
    private String formattedContextPrompt;

    public Builder systemPrompt(String systemPrompt) {
      this.systemPrompt = systemPrompt;
      return this;
    }

    public Builder userPrompt(String userPrompt) {
      this.userPrompt = userPrompt;
      return this;
    }

    public Builder formattedContextPrompt(String formattedContextPrompt) {
      this.formattedContextPrompt = formattedContextPrompt;
      return this;
    }

    public AnalysisContext build() {
      return new AnalysisContext(
        systemPrompt,
        userPrompt,
        formattedContextPrompt
      );
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnalysisContext that = (AnalysisContext) o;
    return (
      Objects.equals(systemPrompt, that.systemPrompt) &&
      Objects.equals(userPrompt, that.userPrompt) &&
      Objects.equals(formattedContextPrompt, that.formattedContextPrompt)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(systemPrompt, userPrompt, formattedContextPrompt);
  }
}
