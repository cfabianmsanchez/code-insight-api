package com.codeinsight.api.application.pipeline.rules;

import java.util.List;
import java.util.Map;

/**
 * Catálogo de reglas de detección de lenguajes, frameworks, bases de datos y librerías.
 */
public final class TechnologyDetectionRules {

  private TechnologyDetectionRules() {}

  /** Mapeo de extensión a lenguaje principal. */
  public static final List<LanguageRule> LANGUAGE_RULES = List.of(
    new LanguageRule(".java", "Java"),
    new LanguageRule(".ts", "TypeScript"),
    new LanguageRule(".py", "Python"),
    new LanguageRule(".js", "JavaScript"),
    new LanguageRule(".go", "Go"),
    new LanguageRule(".tf", "HCL / Terraform")
  );

  public static final String DEFAULT_LANGUAGE = "Java / Multi-lenguaje";

  /** Reglas de detección por archivo de manifiesto. */
  public static final Map<String, ManifestRules> MANIFEST_RULES_BY_FILE =
    Map.of(
      "pom.xml",
      new ManifestRules(
        "Maven",
        List.of(
          new FrameworkRule(List.of("spring-boot"), "Spring Boot", "Java")
        ),
        List.of(
          new DatabaseRule(
            List.of("postgresql", "org.postgresql"),
            "PostgreSQL"
          ),
          new DatabaseRule(List.of("h2database", "com.h2database"), "H2"),
          new DatabaseRule(List.of("mysql-connector", "mysql"), "MySQL"),
          new DatabaseRule(
            List.of("mongodb", "spring-boot-starter-data-mongodb"),
            "MongoDB"
          )
        ),
        List.of(
          new LibraryRule(
            List.of("spring-boot-starter-data-jpa", "spring-data-jpa"),
            "Spring Data JPA"
          ),
          new LibraryRule(List.of("<artifactid>lombok</artifactid>"), "Lombok"),
          new LibraryRule(List.of("springdoc", "swagger"), "OpenAPI / Swagger")
        )
      ),

      "build.gradle",
      new ManifestRules(
        "Gradle",
        List.of(
          new FrameworkRule(List.of("spring-boot"), "Spring Boot", "Java")
        ),
        List.of(
          new DatabaseRule(List.of("postgresql"), "PostgreSQL"),
          new DatabaseRule(List.of("h2database", "com.h2database"), "H2"),
          new DatabaseRule(List.of("mysql"), "MySQL")
        ),
        List.of()
      ),

      "package.json",
      new ManifestRules(
        "npm / Node.js",
        List.of(
          new FrameworkRule(List.of("@angular/core"), "Angular", "TypeScript"),
          new FrameworkRule(List.of("@nestjs/core"), "NestJS", "TypeScript"),
          new FrameworkRule(List.of("react"), "React", null),
          new FrameworkRule(List.of("express"), "Express.js", null)
        ),
        List.of(
          new DatabaseRule(List.of("pg", "postgres"), "PostgreSQL"),
          new DatabaseRule(List.of("mysql"), "MySQL"),
          new DatabaseRule(List.of("mongoose", "mongodb"), "MongoDB")
        ),
        List.of()
      ),

      "requirements.txt",
      new ManifestRules(
        "pip / Python",
        List.of(
          new FrameworkRule(List.of("fastapi"), "FastAPI", "Python"),
          new FrameworkRule(List.of("django"), "Django", "Python"),
          new FrameworkRule(List.of("flask"), "Flask", "Python")
        ),
        List.of(new DatabaseRule(List.of("psycopg2"), "PostgreSQL")),
        List.of(new LibraryRule(List.of("sqlalchemy"), "SQLAlchemy"))
      ),

      "pyproject.toml",
      new ManifestRules(
        "pip / Python",
        List.of(
          new FrameworkRule(List.of("fastapi"), "FastAPI", "Python"),
          new FrameworkRule(List.of("django"), "Django", "Python"),
          new FrameworkRule(List.of("flask"), "Flask", "Python")
        ),
        List.of(new DatabaseRule(List.of("psycopg2"), "PostgreSQL")),
        List.of(new LibraryRule(List.of("sqlalchemy"), "SQLAlchemy"))
      ),

      "dockerfile",
      new ManifestRules(
        null,
        List.of(),
        List.of(),
        List.of(new LibraryRule(List.of("from "), "Docker Containerization"))
      ),

      "main.tf",
      new ManifestRules(
        "Terraform CLI",
        List.of(
          new FrameworkRule(
            List.of("resource", "module", "terraform", "provider"),
            "Terraform IaC",
            "HCL / Terraform"
          )
        ),
        List.of(),
        List.of(
          new LibraryRule(List.of("provider \"aws\"", "aws_"), "AWS Provider"),
          new LibraryRule(
            List.of("provider \"azurerm\"", "azurerm_"),
            "Azure Provider"
          ),
          new LibraryRule(
            List.of("provider \"google\"", "google_"),
            "GCP Provider"
          ),
          new LibraryRule(
            List.of("provider \"kubernetes\"", "kubernetes_"),
            "Kubernetes Provider"
          )
        )
      )
    );

  public record LanguageRule(String extension, String languageName) {}

  public record ManifestRules(
    String buildTool,
    List<FrameworkRule> frameworkRules,
    List<DatabaseRule> databaseRules,
    List<LibraryRule> libraryRules
  ) {}

  public record FrameworkRule(
    List<String> keywords,
    String frameworkName,
    String language
  ) {}

  public record DatabaseRule(List<String> keywords, String databaseName) {}

  public record LibraryRule(List<String> keywords, String libraryName) {}
}
