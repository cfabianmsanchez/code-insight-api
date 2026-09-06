package com.codeinsight.api.application.pipeline.rules;

import java.util.List;
import java.util.Map;

/**
 * Catálogo centralizado de reglas de detección de tecnologías.
 * <p>
 * Para escalar el sistema (nuevo framework, nueva base de datos, nueva librería, nuevo
 * tipo de manifiesto) basta con editar las constantes de esta clase;
 * {@link com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage}
 * no necesita ningún cambio.
 * <p>
 * <b>Cómo agregar soporte a un nuevo framework en package.json:</b>
 * <ol>
 *   <li>Añadir una {@link FrameworkRule} en {@link ManifestRules#frameworkRules()} de la
 *       entrada {@code "package.json"} de {@link #MANIFEST_RULES_BY_FILE}.</li>
 * </ol>
 * <b>Cómo agregar detección de una nueva librería en pom.xml:</b>
 * <ol>
 *   <li>Añadir una {@link LibraryRule} en {@link ManifestRules#libraryRules()} de la
 *       entrada {@code "pom.xml"}.</li>
 * </ol>
 */
public final class TechnologyDetectionRules {

    private TechnologyDetectionRules() {}

    // -------------------------------------------------------------------------
    // Lenguajes (extensión → nombre de lenguaje para mostrar)
    // -------------------------------------------------------------------------

    /**
     * Mapeo ordenado de extensión de archivo a nombre de lenguaje.
     * El orden importa: se evalúa de arriba a abajo y se devuelve el primero
     * cuya extensión tenga la mayor frecuencia de archivos en el proyecto.
     */
    public static final List<LanguageRule> LANGUAGE_RULES = List.of(
            new LanguageRule(".java", "Java"),
            new LanguageRule(".ts",   "TypeScript"),
            new LanguageRule(".py",   "Python"),
            new LanguageRule(".js",   "JavaScript"),
            new LanguageRule(".go",   "Go")
    );

    /** Nombre de lenguaje a usar cuando no se puede determinar el principal. */
    public static final String DEFAULT_LANGUAGE = "Java / Multi-lenguaje";

    // -------------------------------------------------------------------------
    // Reglas de manifiesto
    // -------------------------------------------------------------------------

    /**
     * Mapa de nombre de archivo de manifiesto (en minúsculas) a su conjunto de reglas.
     * <p>
     * Para soportar un nuevo tipo de manifiesto (ej. {@code Cargo.toml} para Rust),
     * añadir una nueva entrada aquí con su {@link ManifestRules}.
     */
    public static final Map<String, ManifestRules> MANIFEST_RULES_BY_FILE = Map.of(

            "pom.xml", new ManifestRules(
                    "Maven",
                    List.of(
                            new FrameworkRule(List.of("spring-boot"), "Spring Boot", "Java")
                    ),
                    List.of(
                            new DatabaseRule(List.of("postgresql", "org.postgresql"),                  "PostgreSQL"),
                            new DatabaseRule(List.of("h2database", "com.h2database"),                  "H2"),
                            new DatabaseRule(List.of("mysql-connector", "mysql"),                      "MySQL"),
                            new DatabaseRule(List.of("mongodb", "spring-boot-starter-data-mongodb"),   "MongoDB")
                    ),
                    List.of(
                            new LibraryRule(List.of("spring-boot-starter-data-jpa", "spring-data-jpa"), "Spring Data JPA"),
                            new LibraryRule(List.of("<artifactid>lombok</artifactid>"),                 "Lombok"),
                            new LibraryRule(List.of("springdoc", "swagger"),                           "OpenAPI / Swagger")
                    )
            ),

            "build.gradle", new ManifestRules(
                    "Gradle",
                    List.of(
                            new FrameworkRule(List.of("spring-boot"), "Spring Boot", "Java")
                    ),
                    List.of(
                            new DatabaseRule(List.of("postgresql"),              "PostgreSQL"),
                            new DatabaseRule(List.of("h2database", "com.h2database"), "H2"),
                            new DatabaseRule(List.of("mysql"),                   "MySQL")
                    ),
                    List.of()
            ),

            "package.json", new ManifestRules(
                    "npm / Node.js",
                    List.of(
                            new FrameworkRule(List.of("@angular/core"),  "Angular",     "TypeScript"),
                            new FrameworkRule(List.of("@nestjs/core"),   "NestJS",      "TypeScript"),
                            new FrameworkRule(List.of("react"),          "React",       null),   // lenguaje se infiere por "typescript" en el contenido
                            new FrameworkRule(List.of("express"),        "Express.js",  null)
                    ),
                    List.of(
                            new DatabaseRule(List.of("pg", "postgres"), "PostgreSQL"),
                            new DatabaseRule(List.of("mysql"),          "MySQL"),
                            new DatabaseRule(List.of("mongoose", "mongodb"), "MongoDB")
                    ),
                    List.of()
            ),

            "requirements.txt", new ManifestRules(
                    "pip / Python",
                    List.of(
                            new FrameworkRule(List.of("fastapi"), "FastAPI", "Python"),
                            new FrameworkRule(List.of("django"),  "Django",  "Python"),
                            new FrameworkRule(List.of("flask"),   "Flask",   "Python")
                    ),
                    List.of(
                            new DatabaseRule(List.of("psycopg2"), "PostgreSQL")
                    ),
                    List.of(
                            new LibraryRule(List.of("sqlalchemy"), "SQLAlchemy")
                    )
            ),

            "pyproject.toml", new ManifestRules(
                    "pip / Python",
                    List.of(
                            new FrameworkRule(List.of("fastapi"), "FastAPI", "Python"),
                            new FrameworkRule(List.of("django"),  "Django",  "Python"),
                            new FrameworkRule(List.of("flask"),   "Flask",   "Python")
                    ),
                    List.of(
                            new DatabaseRule(List.of("psycopg2"), "PostgreSQL")
                    ),
                    List.of(
                            new LibraryRule(List.of("sqlalchemy"), "SQLAlchemy")
                    )
            ),

            "dockerfile", new ManifestRules(
                    null,   // no impone buildTool
                    List.of(),
                    List.of(),
                    List.of(
                            new LibraryRule(List.of("from "), "Docker Containerization")  // cualquier Dockerfile válido contiene FROM
                    )
            )
    );

    // -------------------------------------------------------------------------
    // Records públicos que modelan las reglas
    // -------------------------------------------------------------------------

    /**
     * Asocia una extensión de archivo con el nombre del lenguaje a mostrar.
     */
    public record LanguageRule(String extension, String languageName) {}

    /**
     * Agrupa todas las reglas aplicables a un tipo de archivo de manifiesto.
     *
     * @param buildTool      Nombre de la herramienta de build que implica este manifiesto
     *                       ({@code null} si el manifiesto no determina la herramienta de build).
     * @param frameworkRules Reglas para detectar el framework principal.
     * @param databaseRules  Reglas para detectar bases de datos.
     * @param libraryRules   Reglas para detectar librerías clave.
     */
    public record ManifestRules(
            String buildTool,
            List<FrameworkRule> frameworkRules,
            List<DatabaseRule> databaseRules,
            List<LibraryRule> libraryRules
    ) {}

    /**
     * Detecta un framework: si el contenido del manifiesto contiene alguna de las
     * {@code keywords}, se establece el framework y opcionalmente el lenguaje principal.
     *
     * @param keywords      Palabras clave a buscar en el contenido del manifiesto.
     * @param frameworkName Nombre del framework a registrar.
     * @param language      Lenguaje que implica este framework ({@code null} si no lo determina).
     */
    public record FrameworkRule(List<String> keywords, String frameworkName, String language) {}

    /**
     * Detecta una base de datos: si el contenido contiene alguna de las {@code keywords},
     * se agrega {@code databaseName} al conjunto de bases de datos detectadas.
     */
    public record DatabaseRule(List<String> keywords, String databaseName) {}

    /**
     * Detecta una librería: si el contenido contiene alguna de las {@code keywords},
     * se agrega {@code libraryName} al conjunto de librerías clave detectadas.
     */
    public record LibraryRule(List<String> keywords, String libraryName) {}
}
