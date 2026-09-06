package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.pipeline.rules.TechnologyDetectionRules;
import com.codeinsight.api.application.pipeline.rules.TechnologyDetectionRules.DatabaseRule;
import com.codeinsight.api.application.pipeline.rules.TechnologyDetectionRules.FrameworkRule;
import com.codeinsight.api.application.pipeline.rules.TechnologyDetectionRules.LibraryRule;
import com.codeinsight.api.application.pipeline.rules.TechnologyDetectionRules.ManifestRules;
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
import java.util.Map;
import java.util.Set;

/**
 * Etapa 3: Detecta el stack tecnológico a partir de manifiestos y extensiones.
 */
@Component
public class TechnologyDetectorStage {

    private static final Logger log = LoggerFactory.getLogger(TechnologyDetectorStage.class);

    /**
     * Examina las extensiones de archivos y los archivos de manifiesto del proyecto
     * para construir el stack tecnológico.
     *
     * @param scannedMap Resultado del escaneo de archivos de la Etapa 2.
     * @return {@link TechnologyStack} con el lenguaje, framework, motor de build y
     *         librerías detectadas.
     */
    public TechnologyStack detect(ScannedFileMap scannedMap) {
        if (scannedMap == null) {
            throw new InvalidRepositoryException("ScannedFileMap cannot be null");
        }

        DetectionState state = new DetectionState(determineMainLanguage(scannedMap.getExtensionCounts()));

        if (scannedMap.getManifestFiles() != null) {
            for (Path manifest : scannedMap.getManifestFiles()) {
                processManifest(manifest, state);
            }
        }

        return state.buildStack();
    }

    /**
     * Delega el procesamiento del manifiesto a las reglas definidas en
     * {@link TechnologyDetectionRules#MANIFEST_RULES_BY_FILE}.
     *
     * Los archivos {@code build.gradle} se normalizan a la clave
     * {@code "build.gradle"}
     * para cubrir variantes como {@code build.gradle.kts}.
     */
    private void processManifest(Path manifest, DetectionState state) {
        String fileName = manifest.getFileName().toString().toLowerCase();

        // Normalizar variantes de build.gradle y archivos .tf de Terraform
        String lookupKey = fileName.startsWith("build.gradle") ? "build.gradle"
                : (fileName.endsWith(".tf") ? "main.tf" : fileName);

        ManifestRules rules = TechnologyDetectionRules.MANIFEST_RULES_BY_FILE.get(lookupKey);
        if (rules == null) {
            return;
        }

        String content = readContent(manifest);
        applyManifestRules(rules, content, state);
    }

    /**
     * Aplica el conjunto de reglas de un manifiesto al estado de detección:
     * herramienta de build, framework principal, bases de datos y librerías.
     */
    private void applyManifestRules(ManifestRules rules, String content, DetectionState state) {
        if (rules.buildTool() != null) {
            state.buildTool = rules.buildTool();
        }

        // Framework principal: se aplica la primera regla que coincide
        for (FrameworkRule rule : rules.frameworkRules()) {
            if (anyKeywordPresent(content, rule.keywords())) {
                state.mainFramework = rule.frameworkName();
                if (rule.language() != null) {
                    state.mainLanguage = rule.language();
                } else if (rule.frameworkName().equals("React")) {
                    // React puede ser TS o JS según el contenido del manifiesto
                    state.mainLanguage = content.contains("typescript") ? "TypeScript" : "JavaScript";
                }
                break;
            }
        }

        for (DatabaseRule rule : rules.databaseRules()) {
            if (anyKeywordPresent(content, rule.keywords())) {
                state.databases.add(rule.databaseName());
            }
        }

        for (LibraryRule rule : rules.libraryRules()) {
            if (anyKeywordPresent(content, rule.keywords())) {
                state.libraries.add(rule.libraryName());
            }
        }
    }

    private boolean anyKeywordPresent(String content, java.util.List<String> keywords) {
        return keywords.stream().anyMatch(content::contains);
    }

    /**
     * Determina el lenguaje principal del proyecto según la frecuencia dominante de
     * extensiones de archivo, usando
     * {@link TechnologyDetectionRules#LANGUAGE_RULES}.
     */
    private String determineMainLanguage(Map<String, Integer> extCounts) {
        if (extCounts == null || extCounts.isEmpty()) {
            return TechnologyDetectionRules.DEFAULT_LANGUAGE;
        }

        for (TechnologyDetectionRules.LanguageRule rule : TechnologyDetectionRules.LANGUAGE_RULES) {
            int count = extCounts.getOrDefault(rule.extension(), 0);
            if (count > 0 && isHighestOrEqual(extCounts, count)) {
                return rule.languageName();
            }
        }

        return TechnologyDetectionRules.DEFAULT_LANGUAGE;
    }

    private boolean isHighestOrEqual(Map<String, Integer> extCounts, int targetCount) {
        return TechnologyDetectionRules.LANGUAGE_RULES.stream()
                .mapToInt(rule -> extCounts.getOrDefault(rule.extension(), 0))
                .allMatch(count -> targetCount >= count);
    }

    /**
     * Lee el contenido completo de un archivo de manifiesto en minúsculas de forma
     * segura.
     */
    private String readContent(Path path) {
        try {
            return Files.readString(path).toLowerCase();
        } catch (IOException e) {
            log.warn("Could not read content from manifest file {}: {}", path, e.getMessage());
            return "";
        }
    }

    private static class DetectionState {
        String mainLanguage;
        String mainFramework = "Desconocido / Genérico";
        String buildTool = "Desconocido";
        final Set<String> databases = new HashSet<>();
        final Set<String> libraries = new HashSet<>();

        DetectionState(String mainLanguage) {
            this.mainLanguage = mainLanguage;
        }

        TechnologyStack buildStack() {
            return TechnologyStack.builder()
                    .mainLanguage(mainLanguage)
                    .mainFramework(mainFramework)
                    .buildTool(buildTool)
                    .databasesDetected(new ArrayList<>(databases))
                    .keyLibraries(new ArrayList<>(libraries))
                    .build();
        }
    }
}
