package com.codeinsight.api.domain.model;

import java.util.List;

public class TechnologyStack {
    private String mainLanguage;
    private String mainFramework;
    private String buildTool;
    private List<String> databasesDetected;
    private List<String> keyLibraries;

    public TechnologyStack() {}

    public TechnologyStack(String mainLanguage, String mainFramework, String buildTool,
                           List<String> databasesDetected, List<String> keyLibraries) {
        this.mainLanguage = mainLanguage;
        this.mainFramework = mainFramework;
        this.buildTool = buildTool;
        this.databasesDetected = databasesDetected;
        this.keyLibraries = keyLibraries;
    }

    public String getMainLanguage() { return mainLanguage; }
    public String getMainFramework() { return mainFramework; }
    public String getBuildTool() { return buildTool; }
    public List<String> getDatabasesDetected() { return databasesDetected; }
    public List<String> getKeyLibraries() { return keyLibraries; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String mainLanguage;
        private String mainFramework;
        private String buildTool;
        private List<String> databasesDetected;
        private List<String> keyLibraries;

        public Builder mainLanguage(String mainLanguage) { this.mainLanguage = mainLanguage; return this; }
        public Builder mainFramework(String mainFramework) { this.mainFramework = mainFramework; return this; }
        public Builder buildTool(String buildTool) { this.buildTool = buildTool; return this; }
        public Builder databasesDetected(List<String> databasesDetected) { this.databasesDetected = databasesDetected; return this; }
        public Builder keyLibraries(List<String> keyLibraries) { this.keyLibraries = keyLibraries; return this; }

        public TechnologyStack build() {
            return new TechnologyStack(mainLanguage, mainFramework, buildTool, databasesDetected, keyLibraries);
        }
    }
}
