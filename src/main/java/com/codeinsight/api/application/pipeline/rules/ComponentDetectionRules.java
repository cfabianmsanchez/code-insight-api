package com.codeinsight.api.application.pipeline.rules;

import com.codeinsight.api.domain.model.ComponentType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Catálogo centralizado de reglas de detección de componentes.
 * <p>
 * Para escalar el sistema (nuevo lenguaje, nuevo tipo de componente, nuevo patrón de ruta)
 * basta con editar las constantes de esta clase; {@link com.codeinsight.api.application.pipeline.stage.ComponentDetectorStage}
 * no necesita ningún cambio.
 * <p>
 * <b>Cómo agregar soporte a un nuevo lenguaje:</b>
 * <ol>
 *   <li>Añadir su extensión a {@link #SOURCE_EXTENSIONS}.</li>
 *   <li>Crear una lista {@code List<ComponentRule>} con sus patrones y añadirla a {@link #RULES_BY_EXTENSION}.</li>
 * </ol>
 * <b>Cómo agregar un nuevo patrón de componente existente:</b>
 * <ol>
 *   <li>Añadir la entrada correspondiente en la lista del lenguaje afectado.</li>
 * </ol>
 */
public final class ComponentDetectionRules {

    private ComponentDetectionRules() {}

    // -------------------------------------------------------------------------
    // Extensiones de archivos de código fuente analizables
    // -------------------------------------------------------------------------

    /** Extensiones que se consideran código fuente analizable. */
    public static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java", ".ts", ".tsx", ".js", ".jsx", ".py", ".go"
    );

    // -------------------------------------------------------------------------
    // Patrones de exclusión de archivos de test
    // -------------------------------------------------------------------------

    /** Segmentos de ruta que indican que el archivo pertenece a un directorio de tests. */
    public static final List<String> TEST_PATH_SEGMENTS = List.of(
            "src/test/", "/test/", "__tests__/"
    );

    /** Sufijos de nombre de archivo que identifican archivos de test. */
    public static final List<String> TEST_FILE_SUFFIXES = List.of(
            ".test.", ".spec.",
            "test.java", "test.ts", "test.js", "test.py", "_test.go"
    );

    // -------------------------------------------------------------------------
    // Reglas de contenido por extensión de archivo
    // -------------------------------------------------------------------------

    private static final List<ComponentRule> JAVA_RULES = List.of(
            new ComponentRule(c -> hasAnyAnnotation(c, "RestController", "Controller"), ComponentType.CONTROLLER),
            new ComponentRule(c -> hasAnnotation(c, "Service"),                         ComponentType.SERVICE),
            new ComponentRule(c -> hasAnnotation(c, "Repository")
                    || hasAnyInterfaceExtension(c, "JpaRepository", "CrudRepository"), ComponentType.REPOSITORY),
            new ComponentRule(c -> hasAnyAnnotation(c, "Entity", "Table"),             ComponentType.ENTITY),
            new ComponentRule(c -> hasAnnotation(c, "Configuration"),                  ComponentType.CONFIGURATION),
            new ComponentRule(c -> hasAnnotation(c, "Component"),                      ComponentType.COMPONENT)
    );

    private static final List<ComponentRule> TS_JS_RULES = List.of(
            new ComponentRule(c -> hasAnyAnnotation(c, "RestController", "Controller"),                                  ComponentType.CONTROLLER),
            new ComponentRule(c -> hasAnnotation(c, "Component"),                                                        ComponentType.FRONTEND_COMPONENT),
            new ComponentRule(c -> hasAnnotation(c, "Injectable"),                                                       ComponentType.FRONTEND_SERVICE),
            new ComponentRule(c -> hasAnnotation(c, "Module"),                                                           ComponentType.CONFIGURATION),
            new ComponentRule(c -> hasAnyAnnotation(c, "Entity", "Table"),                                               ComponentType.ENTITY),
            new ComponentRule(c -> containsAny(c, "express.router()", "router()", "app.get(", "app.post(", "app.put(", "app.delete("), ComponentType.CONTROLLER),
            new ComponentRule(c -> containsAny(c, "import react", "from 'react'", "from \"react\"", "usestate(", "useeffect("), ComponentType.FRONTEND_COMPONENT)
    );

    private static final List<ComponentRule> PYTHON_RULES = List.of(
            new ComponentRule(c -> containsAny(c, "@app.get", "@app.post", "@app.put", "@app.delete",
                    "@router.get", "@router.post", "apirouter()", "@app.route", "apiview", "@api_view"), ComponentType.CONTROLLER),
            new ComponentRule(c -> containsAny(c, "models.model", "declarative_base()", "column("),    ComponentType.ENTITY)
    );

    private static final List<ComponentRule> GO_RULES = List.of(
            new ComponentRule(c -> containsAny(c, "gin.default()", "gin.new()", "http.handlefunc",
                    "r.get(", "r.post(", "fiber.new()"), ComponentType.CONTROLLER)
    );

    /**
     * Mapa de extensión de archivo (en minúsculas) a su lista de reglas de contenido.
     * <p>
     * Cuando un archivo tiene varias extensiones candidatas (ej. {@code .ts} y {@code .tsx}),
     * ambas pueden apuntar a la misma lista de reglas.
     */
    public static final Map<String, List<ComponentRule>> RULES_BY_EXTENSION = Map.of(
            ".java", JAVA_RULES,
            ".ts",   TS_JS_RULES,
            ".tsx",  TS_JS_RULES,
            ".js",   TS_JS_RULES,
            ".jsx",  TS_JS_RULES,
            ".py",   PYTHON_RULES,
            ".go",   GO_RULES
    );

    // -------------------------------------------------------------------------
    // Reglas de convención de ruta (fallback cuando el contenido no es suficiente)
    // -------------------------------------------------------------------------

    /**
     * Reglas basadas en la ruta del archivo (directorios y sufijos de nombre).
     * Se evalúan en orden; la primera que coincide gana.
     * <p>
     * Para agregar una nueva convención basta añadir una entrada aquí.
     */
    public static final List<PathConventionRule> PATH_CONVENTION_RULES = List.of(
            new PathConventionRule(
                    List.of("/controllers/", "/controller/", "/routes/", "/handlers/", "/endpoints/"),
                    List.of("controller.java", "controller.ts", "controller.js", "controller.py",
                            "handler.go", "router.ts", "router.js"),
                    ComponentType.CONTROLLER
            ),
            new PathConventionRule(
                    List.of("/services/", "/service/", "/usecases/", "/usecase/"),
                    List.of("service.java", "service.ts", "service.js", "service.py"),
                    ComponentType.SERVICE
            ),
            new PathConventionRule(
                    List.of("/repositories/", "/repository/", "/dao/", "/persistence/"),
                    List.of("repository.java", "repository.ts", "repository.js", "dao.py"),
                    ComponentType.REPOSITORY
            ),
            new PathConventionRule(
                    List.of("/entities/", "/entity/", "/models/", "/model/", "/schemas/", "/schema/"),
                    List.of(),
                    ComponentType.ENTITY
            ),
            new PathConventionRule(
                    List.of("/components/", "/pages/", "/views/"),
                    List.of(".tsx", ".jsx"),
                    ComponentType.FRONTEND_COMPONENT
            )
    );

    // -------------------------------------------------------------------------
    // Records públicos que modelan las reglas
    // -------------------------------------------------------------------------

    /**
     * Regla que evalúa el contenido limpio (sin comentarios, en minúsculas) de un archivo
     * y retorna un {@link ComponentType} si el predicado se cumple.
     */
    public record ComponentRule(Predicate<String> matcher, ComponentType type) {}

    /**
     * Regla de convención de ruta: se cumple si la ruta contiene algún segmento de directorio
     * o termina con algún sufijo de nombre de archivo de la lista.
     */
    public record PathConventionRule(
            List<String> directorySegments,
            List<String> fileSuffixes,
            ComponentType type
    ) {}

    // -------------------------------------------------------------------------
    // Helpers de matching (paquete-privados, usados por las reglas lambda)
    // -------------------------------------------------------------------------

    static boolean hasAnnotation(String content, String annotation) {
        return Pattern.compile("@" + Pattern.quote(annotation.toLowerCase()) + "\\b")
                .matcher(content).find();
    }

    static boolean hasAnyAnnotation(String content, String... annotations) {
        for (String ann : annotations) {
            if (hasAnnotation(content, ann)) return true;
        }
        return false;
    }

    static boolean hasInterfaceExtension(String content, String interfaceName) {
        return Pattern.compile("\\bextends\\s+.*" + Pattern.quote(interfaceName.toLowerCase()) + "\\b")
                .matcher(content).find();
    }

    static boolean hasAnyInterfaceExtension(String content, String... interfaceNames) {
        for (String iface : interfaceNames) {
            if (hasInterfaceExtension(content, iface)) return true;
        }
        return false;
    }

    static boolean containsAny(String content, String... targets) {
        for (String target : targets) {
            if (content.contains(target)) return true;
        }
        return false;
    }
}
