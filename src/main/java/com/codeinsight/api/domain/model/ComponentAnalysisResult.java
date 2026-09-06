package com.codeinsight.api.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Modelo de Dominio: Resultado del Análisis de Componentes (Etapa 4).
 *
 * Contiene el resumen de componentes de software detectados en el proyecto,
 * agrupados por estereotipo y enumerados con sus rutas asociadas.
 */
public class ComponentAnalysisResult {

  /** Número total de componentes reconocidos. */
  private final int totalComponents;
  /** Conteo acumulado por estereotipo (CONTROLLER, SERVICE, REPOSITORY, etc.). */
  private final Map<String, Integer> componentCounts;
  /** Lista detallada de los componentes detectados. */
  private final List<DetectedComponent> components;

  public ComponentAnalysisResult(
    int totalComponents,
    Map<String, Integer> componentCounts,
    List<DetectedComponent> components
  ) {
    this.totalComponents = totalComponents;
    this.componentCounts =
      componentCounts != null
        ? Collections.unmodifiableMap(componentCounts)
        : Collections.emptyMap();
    this.components =
      components != null
        ? Collections.unmodifiableList(components)
        : Collections.emptyList();
  }

  public int getTotalComponents() {
    return totalComponents;
  }

  public Map<String, Integer> getComponentCounts() {
    return componentCounts;
  }

  public List<DetectedComponent> getComponents() {
    return components;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private int totalComponents;
    private Map<String, Integer> componentCounts;
    private List<DetectedComponent> components;

    public Builder totalComponents(int totalComponents) {
      this.totalComponents = totalComponents;
      return this;
    }

    public Builder componentCounts(Map<String, Integer> componentCounts) {
      this.componentCounts = componentCounts;
      return this;
    }

    public Builder components(List<DetectedComponent> components) {
      this.components = components;
      return this;
    }

    public ComponentAnalysisResult build() {
      return new ComponentAnalysisResult(
        totalComponents,
        componentCounts,
        components
      );
    }
  }
}
