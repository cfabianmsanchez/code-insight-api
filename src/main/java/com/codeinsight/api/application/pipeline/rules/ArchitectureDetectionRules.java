package com.codeinsight.api.application.pipeline.rules;

import com.codeinsight.api.domain.model.FrontendFramework;

import java.util.List;
import java.util.Set;

/**
 * Catálogo centralizado de reglas de detección de evidencias arquitectónicas.
 * <p>
 * Para escalar el sistema (nuevo framework frontend, nuevo indicador de stack backend,
 * nueva capa arquitectónica, nuevos patrones de test) basta con editar las constantes
 * de esta clase; {@link com.codeinsight.api.application.pipeline.stage.ArchitectureEvidenceDetectorStage}
 * no necesita ningún cambio.
 * <p>
 * <b>Cómo agregar soporte a un nuevo framework frontend (ej. Qwik):</b>
 * <ol>
 *   <li>Añadir un valor a {@link FrontendFramework} (enum de dominio).</li>
 *   <li>Añadir una entrada {@link FrontendFrameworkRule} en {@link #FRONTEND_FRAMEWORK_RULES}
 *       con sus señales de detección. El orden de la lista determina la prioridad.</li>
 * </ol>
 * <b>Cómo agregar un nuevo indicador de backend (ej. Ruby con Gemfile):</b>
 * <ol>
 *   <li>Añadir la extensión a {@link #BACKEND_FILE_EXTENSIONS} o el nombre de archivo
 *       a {@link #BACKEND_FILE_NAMES}.</li>
 * </ol>
 */
public final class ArchitectureDetectionRules {

    private ArchitectureDetectionRules() {}

    // ─────────────────────────────────────────────────────────────────────────
    // Detección de archivos de test (multiplataforma)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sufijos de nombre de archivo que identifican tests en cualquier stack.
     * <ul>
     *   <li>Java: {@code *Test.java}, {@code *Tests.java}, {@code *Spec.java}</li>
     *   <li>Node: {@code *.test.js/ts}, {@code *.spec.js/ts/mjs}</li>
     * </ul>
     */
    public static final Set<String> TEST_FILE_SUFFIXES = Set.of(
            "Test.java", "Tests.java", "Spec.java",
            ".test.js", ".test.ts", ".spec.js", ".spec.ts",
            ".test.mjs", ".spec.mjs"
    );

    /**
     * Prefijos de nombre de archivo Python que identifican tests.
     * Python utiliza {@code test_*.py} (prefijo) en lugar de sufijos.
     */
    public static final Set<String> TEST_FILE_PREFIXES_PYTHON = Set.of("test_");

    /**
     * Sufijos de nombre de archivo Python que identifican tests.
     */
    public static final Set<String> TEST_FILE_SUFFIXES_PYTHON = Set.of("_test.py");

    /**
     * Extensión de archivo Python para poder aplicar los prefijos/sufijos anteriores
     * solo a archivos {@code .py}.
     */
    public static final String PYTHON_EXTENSION = ".py";

    /** Directorios que convencionalmente contienen tests en distintos stacks. */
    public static final Set<String> TEST_DIRECTORY_NAMES = Set.of(
            "test", "tests", "__tests__", "spec", "specs",
            "src/test", "test/unit", "test/integration",
            "e2e", "cypress", "jest"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Análisis de rutas arquitectónicas
    // ─────────────────────────────────────────────────────────────────────────

    /** Palabras clave de arquitectura reconocidas en segmentos de ruta de directorio. */
    public static final Set<String> KNOWN_ARCHITECTURAL_KEYWORDS = Set.of(
            "domain", "application", "infrastructure",
            "port", "ports", "in", "out",
            "adapter", "adapters",
            "usecase", "usecases",
            "controller", "controllers",
            "service", "services",
            "repository", "repositories",
            "entity", "entities",
            "model", "models",
            "dto", "config", "configuration",
            "feature", "features",
            "component", "components",
            "mapper", "mappers",
            "exception", "exceptions",
            "shared", "core", "web", "view", "views"
    );

    /** Subconjunto de palabras clave que representan capas del sistema. */
    public static final Set<String> LAYER_KEYWORDS = Set.of(
            "domain", "application", "infrastructure",
            "feature", "features",
            "core", "web",
            "service", "services",
            "repository", "repositories",
            "controller", "controllers"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Detección de puertos hexagonales
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sufijos de nombre de interfaz que indican un puerto en arquitectura hexagonal.
     * Una interfaz cuyo nombre termine en alguno de estos sufijos se considera candidata
     * a ser un puerto inbound u outbound.
     */
    public static final List<String> PORT_INTERFACE_SUFFIXES = List.of(
            "Port", "UseCase", "Repository", "Gateway", "Facade", "Handler", "Client"
    );

    /**
     * Segmentos de ruta que indican que un archivo implementa un puerto inbound
     * (la clase está en la capa de aplicación).
     */
    public static final List<String> INBOUND_PATH_SEGMENTS = List.of(
            "port/in", "ports/in", "application"
    );

    /**
     * Segmentos de ruta que indican que un archivo implementa un adaptador outbound
     * (la clase está en la capa de infraestructura o adaptadores).
     */
    public static final List<String> OUTBOUND_PATH_SEGMENTS = List.of(
            "adapter", "port/out", "ports/out", "infrastructure"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Clasificación de tipo de proyecto (ProjectKind)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extensiones de archivo que implican un proyecto backend Java, Go o .NET.
     */
    public static final Set<String> BACKEND_FILE_EXTENSIONS = Set.of(
            ".java", ".go", ".cs", ".csproj"
    );

    /**
     * Nombres de archivo de manifiesto que implican proyecto backend.
     */
    public static final Set<String> BACKEND_FILE_NAMES = Set.of(
            "pom.xml", "build.gradle", "go.mod"
    );

    /**
     * Keywords en la ruta del archivo que indican backend Python
     * (junto con la extensión {@code .py}).
     */
    public static final Set<String> PYTHON_BACKEND_KEYWORDS = Set.of(
            "django", "fastapi", "flask"
    );

    /**
     * Nombre de archivo de requerimientos Python que implica backend.
     */
    public static final String PYTHON_REQUIREMENTS_FILE = "requirements.txt";

    /**
     * Extensiones de archivo que implican archivos frontend (JS, HTML, CSS, Vue, etc.).
     * Se evalúan ignorando la carpeta {@code node_modules}.
     */
    public static final Set<String> FRONTEND_FILE_EXTENSIONS = Set.of(
            ".ts", ".js", ".html", ".scss", ".vue", ".jsx", ".tsx"
    );

    /**
     * Keywords en el nombre de archivo que señalan un proyecto móvil
     * (Ionic / Capacitor / Cordova).
     */
    public static final Set<String> MOBILE_FILE_KEYWORDS = Set.of(
            "ionic", "capacitor", "cordova"
    );

    /**
     * Nombres de framework que el {@link com.codeinsight.api.domain.model.TechnologyStack} ya detectó
     * y que implican inequívocamente un proyecto backend.
     * <p>
     * Estos valores se usan para que {@code determineProjectKind} priorice el stack detectado
     * sobre las heurísticas de extensión de archivo, evitando que proyectos NestJS/Express
     * se clasifiquen como FRONTEND por tener archivos {@code .ts} / {@code .js}.
     */
    public static final Set<String> BACKEND_FRAMEWORKS = Set.of(
            "Spring Boot", "NestJS", "Express.js",
            "FastAPI", "Django", "Flask"
    );

    /**
     * Nombres de framework que implican inequívocamente un proyecto frontend.
     * Se usan de forma simétrica a {@link #BACKEND_FRAMEWORKS}.
     */
    public static final Set<String> FRONTEND_FRAMEWORKS = Set.of(
            "Angular", "React", "Vue", "Next.js", "Svelte"
    );

    /**
     * Nombres de framework que implican un proyecto móvil.
     */
    public static final Set<String> MOBILE_FRAMEWORKS = Set.of(
            "Ionic"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Detección de framework frontend (FrontendFramework)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reglas para identificar el framework frontend activo.
     * <p>
     * Se evalúan en orden: la primera que coincide gana. El orden refleja la prioridad
     * de detección (frameworks más específicos primero).
     * <p>
     * Cada {@link FrontendFrameworkRule} define señales independientes que actúan como OR:
     * basta con que UNA señal coincida en cualquier archivo del proyecto.
     */
    public static final List<FrontendFrameworkRule> FRONTEND_FRAMEWORK_RULES = List.of(
            new FrontendFrameworkRule(
                    FrontendFramework.IONIC,
                    Set.of(),
                    Set.of("capacitor.config", "ionic.config"),
                    Set.of(),
                    Set.of(),
                    Set.of("@ionic/")
            ),
            new FrontendFrameworkRule(
                    FrontendFramework.NEXT_JS,
                    Set.of(),
                    Set.of("next.config"),
                    Set.of("/next/"),
                    Set.of(),
                    Set.of("from 'next'", "from \"next\"")
            ),
            new FrontendFrameworkRule(
                    FrontendFramework.ANGULAR,
                    Set.of("angular.json"),
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of("@angular/core", "@Component")
            ),
            new FrontendFrameworkRule(
                    FrontendFramework.VUE,
                    Set.of(),
                    Set.of(),                          // vite.config eliminado: Vite también lo usan React, Svelte, vanilla
                    Set.of(),
                    Set.of(".vue"),
                    Set.of("from 'vue'", "from \"vue\"")
            ),
            new FrontendFrameworkRule(
                    FrontendFramework.REACT,
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of(".jsx", ".tsx"),
                    Set.of("from 'react'", "from \"react\"")
            ),
            new FrontendFrameworkRule(
                    FrontendFramework.SVELTE,
                    Set.of(),
                    Set.of("svelte.config"),
                    Set.of(),
                    Set.of(".svelte"),
                    Set.of()
            )
    );

    /**
     * Extensiones de archivo de código fuente frontend en las que se buscan señales
     * de contenido para detectar el framework.
     */
    public static final Set<String> FRONTEND_CONTENT_SCAN_EXTENSIONS = Set.of(
            ".ts", ".js"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Detección de capas de arquitectura frontend
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Segmentos de ruta que indican una arquitectura orientada a características (Feature-based).
     */
    public static final Set<String> FRONTEND_FEATURE_SEGMENTS = Set.of(
            "/features/", "/feature/"
    );

    /** Segmentos de ruta que indican una capa o módulo compartido. */
    public static final Set<String> FRONTEND_SHARED_SEGMENTS = Set.of(
            "/shared/", "/shared"
    );

    /** Segmentos de ruta que indican una capa o módulo central (core). */
    public static final Set<String> FRONTEND_CORE_SEGMENTS = Set.of(
            "/core/", "/core"
    );

    /** Segmentos de ruta que indican una capa de acceso a datos / servicios. */
    public static final Set<String> FRONTEND_DATA_ACCESS_SEGMENTS = Set.of(
            "/data-access/", "/services/", "/api/"
    );

    /** Segmentos de ruta que indican componentes de página / contenedor. */
    public static final Set<String> FRONTEND_PAGE_SEGMENTS = Set.of(
            "/pages/", "/views/", "/containers/"
    );

    /** Segmentos de ruta o sufijos de nombre de archivo que indican patrón Facade. */
    public static final Set<String> FRONTEND_FACADE_SEGMENTS = Set.of("facade");

    /** Sufijo de archivo que identifica explícitamente una clase Facade TypeScript. */
    public static final String FRONTEND_FACADE_FILE_SUFFIX = "facade.ts";

    /**
     * Extensiones de archivo fuente frontend en las que se analiza el contenido
     * para detectar capas, patrones y señales de reactividad.
     */
    public static final Set<String> FRONTEND_SOURCE_EXTENSIONS = Set.of(
            ".ts", ".js", ".tsx", ".vue"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Detección de inyección de dependencias Spring
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Anotaciones Spring que indican que una clase es gestionada por el contenedor de DI.
     * Se utiliza para filtrar el análisis de inyección por constructor solo a clases
     * relevantes, evitando falsos positivos.
     */
    public static final Set<String> SPRING_MANAGED_ANNOTATIONS = Set.of(
            "@Component",
            "@Service",
            "@Repository",
            "@RestController",
            "@Controller",
            "@Configuration"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Records públicos que modelan las reglas
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Regla de detección de framework frontend.
     * <p>
     * Una regla se cumple (para un archivo dado) si:
     * <ul>
     *   <li>El nombre del archivo coincide exactamente con algún valor de {@code fileNameEquals}, O</li>
     *   <li>El nombre del archivo contiene algún valor de {@code fileNameContains}, O</li>
     *   <li>La ruta del archivo contiene algún valor de {@code pathContains}, O</li>
     *   <li>El nombre del archivo termina con alguna extensión de {@code fileExtensions}, O</li>
     *   <li>El contenido del archivo contiene algún valor de {@code contentSignals}
     *       (solo se evalúa en archivos con extensiones de {@link #FRONTEND_CONTENT_SCAN_EXTENSIONS}).</li>
     * </ul>
     *
     * @param framework       El framework que identifica esta regla.
     * @param fileNameEquals  Nombres exactos de archivo que señalan este framework.
     * @param fileNameContains Fragmentos del nombre de archivo que señalan este framework.
     * @param pathContains    Fragmentos de la ruta que señalan este framework.
     * @param fileExtensions  Extensiones de archivo exclusivas de este framework.
     * @param contentSignals  Strings a buscar en el contenido del archivo.
     */
    public record FrontendFrameworkRule(
            FrontendFramework framework,
            Set<String> fileNameEquals,
            Set<String> fileNameContains,
            Set<String> pathContains,
            Set<String> fileExtensions,
            Set<String> contentSignals
    ) {}
}
