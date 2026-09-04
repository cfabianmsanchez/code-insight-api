package com.codeinsight.api.domain.model;

import java.util.Objects;

public class AnalysisContext {
    private final String systemPrompt;
    private final String userPrompt;
    private final String formattedContextPrompt;

    public AnalysisContext(String systemPrompt, String userPrompt, String formattedContextPrompt) {
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
            return new AnalysisContext(systemPrompt, userPrompt, formattedContextPrompt);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisContext that = (AnalysisContext) o;
        return Objects.equals(systemPrompt, that.systemPrompt) &&
                Objects.equals(userPrompt, that.userPrompt) &&
                Objects.equals(formattedContextPrompt, that.formattedContextPrompt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemPrompt, userPrompt, formattedContextPrompt);
    }
}
