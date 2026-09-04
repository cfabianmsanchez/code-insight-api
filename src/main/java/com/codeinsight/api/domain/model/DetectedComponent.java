package com.codeinsight.api.domain.model;

import java.util.Objects;

public class DetectedComponent {
    private final String name;
    private final ComponentType type;
    private final String relativePath;

    public DetectedComponent(String name, ComponentType type, String relativePath) {
        this.name = name;
        this.type = type;
        this.relativePath = relativePath;
    }

    public String getName() {
        return name;
    }

    public ComponentType getType() {
        return type;
    }

    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectedComponent that = (DetectedComponent) o;
        return Objects.equals(name, that.name) && type == that.type && Objects.equals(relativePath, that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, relativePath);
    }

    @Override
    public String toString() {
        return "DetectedComponent{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", relativePath='" + relativePath + '\'' +
                '}';
    }
}
