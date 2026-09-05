package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TechnologyDetectorStage {

    private static final Logger log = LoggerFactory.getLogger(TechnologyDetectorStage.class);

    public TechnologyStack detect(ScannedFileMap scannedMap) {
        if (scannedMap == null) {
            throw new InvalidRepositoryException("ScannedFileMap cannot be null");
        }

        String mainLanguage = determineMainLanguage(scannedMap.getExtensionCounts());
        String mainFramework = "Desconocido / Genérico";
        String buildTool = "Desconocido";
        Set<String> databases = new HashSet<>();
        Set<String> libraries = new HashSet<>();

        if (scannedMap.getManifestFiles() != null) {
            for (Path manifest : scannedMap.getManifestFiles()) {
                String fileName = manifest.getFileName().toString().toLowerCase();
                String content = readContent(manifest);

                if (fileName.equals("pom.xml")) {
                    buildTool = "Maven";
                    if (content.contains("spring-boot")) {
                        mainFramework = "Spring Boot";
                        mainLanguage = "Java";
                    }
                    if (content.contains("postgresql") || content.contains("org.postgresql"))
                        databases.add("PostgreSQL");
                    if (content.contains("h2database") || content.contains("com.h2database"))
                        databases.add("H2");
                    if (content.contains("mysql-connector") || content.contains("mysql"))
                        databases.add("MySQL");
                    if (content.contains("mongodb") || content.contains("spring-boot-starter-data-mongodb"))
                        databases.add("MongoDB");
                    if (content.contains("spring-boot-starter-data-jpa") || content.contains("spring-data-jpa"))
                        libraries.add("Spring Data JPA");
                    if (content.contains("lombok") || content.contains("org.projectlombok"))
                        libraries.add("Lombok");
                    if (content.contains("springdoc") || content.contains("swagger"))
                        libraries.add("OpenAPI / Swagger");
                } else if (fileName.startsWith("build.gradle")) {
                    buildTool = "Gradle";
                    if (content.contains("spring-boot")) {
                        mainFramework = "Spring Boot";
                        mainLanguage = "Java";
                    }
                    if (content.contains("postgresql"))
                        databases.add("PostgreSQL");
                    if (content.contains("h2database") || content.contains("com.h2database"))
                        databases.add("H2");
                    if (content.contains("mysql"))
                        databases.add("MySQL");
                } else if (fileName.equals("package.json")) {
                    buildTool = "npm / Node.js";
                    if (content.contains("@angular/core")) {
                        mainFramework = "Angular";
                        mainLanguage = "TypeScript";
                    } else if (content.contains("@nestjs/core")) {
                        mainFramework = "NestJS";
                        mainLanguage = "TypeScript";
                    } else if (content.contains("react")) {
                        mainFramework = "React";
                        mainLanguage = content.contains("typescript") ? "TypeScript" : "JavaScript";
                    } else if (content.contains("express")) {
                        mainFramework = "Express.js";
                    }
                    if (content.contains("pg") || content.contains("postgres"))
                        databases.add("PostgreSQL");
                    if (content.contains("mysql"))
                        databases.add("MySQL");
                    if (content.contains("mongoose") || content.contains("mongodb"))
                        databases.add("MongoDB");
                } else if (fileName.equals("requirements.txt") || fileName.equals("pyproject.toml")) {
                    buildTool = "pip / Python";
                    mainLanguage = "Python";
                    if (content.contains("fastapi"))
                        mainFramework = "FastAPI";
                    else if (content.contains("django"))
                        mainFramework = "Django";
                    else if (content.contains("flask"))
                        mainFramework = "Flask";

                    if (content.contains("psycopg2"))
                        databases.add("PostgreSQL");
                    if (content.contains("sqlalchemy"))
                        libraries.add("SQLAlchemy");
                } else if (fileName.equals("dockerfile")) {
                    libraries.add("Docker Containerization");
                }
            }
        }

        return TechnologyStack.builder()
                .mainLanguage(mainLanguage)
                .mainFramework(mainFramework)
                .buildTool(buildTool)
                .databasesDetected(new ArrayList<>(databases))
                .keyLibraries(new ArrayList<>(libraries))
                .build();
    }

    private String determineMainLanguage(Map<String, Integer> extCounts) {
        if (extCounts == null || extCounts.isEmpty()) {
            return "Java / Multi-lenguaje";
        }

        int javaCount = extCounts.getOrDefault(".java", 0);
        int tsCount = extCounts.getOrDefault(".ts", 0);
        int pyCount = extCounts.getOrDefault(".py", 0);
        int jsCount = extCounts.getOrDefault(".js", 0);
        int goCount = extCounts.getOrDefault(".go", 0);

        if (javaCount >= tsCount && javaCount >= pyCount && javaCount >= jsCount && javaCount >= goCount
                && javaCount > 0) {
            return "Java";
        }
        if (tsCount >= pyCount && tsCount >= jsCount && tsCount >= goCount && tsCount > 0) {
            return "TypeScript";
        }
        if (pyCount >= jsCount && pyCount >= goCount && pyCount > 0) {
            return "Python";
        }
        if (jsCount >= goCount && jsCount > 0) {
            return "JavaScript";
        }
        if (goCount > 0) {
            return "Go";
        }

        return "Java / Multi-lenguaje";
    }

    private String readContent(Path path) {
        try {
            return Files.readString(path).toLowerCase();
        } catch (IOException e) {
            log.warn("Could not read content from manifest file {}: {}", path, e.getMessage());
            return "";
        }
    }
}
