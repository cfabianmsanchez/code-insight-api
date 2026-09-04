package com.codeinsight.api.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ComponentAnalysisResult {
    private final int totalComponents;
    private final Map<String, Integer> componentCounts;
    private final List<DetectedComponent> components;

    public ComponentAnalysisResult(int totalComponents, Map<String, Integer> componentCounts, List<DetectedComponent> components) {
        this.totalComponents = totalComponents;
        this.componentCounts = componentCounts != null ? Collections.unmodifiableMap(componentCounts) : Collections.emptyMap();
        this.components = components != null ? Collections.unmodifiableList(components) : Collections.emptyList();
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
            return new ComponentAnalysisResult(totalComponents, componentCounts, components);
        }
    }
}
