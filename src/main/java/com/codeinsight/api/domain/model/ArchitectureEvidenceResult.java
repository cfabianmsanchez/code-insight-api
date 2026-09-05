package com.codeinsight.api.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Modelo de Dominio: Resultado de Evidencias Arquitectónicas (Etapa 5).
 *
 * Contiene los hallazgos factuales recolectados de la estructura del proyecto:
 * rutas, palabras clave, profundidad máxima, distribución de componentes,
 * relaciones puerto→implementación y evidencias de ingeniería (DI y tests).
 */
public class ArchitectureEvidenceResult {

    /** Lista de rutas de directorios estructurales identificadas. */
    private final List<String> structuralPaths;

    /** Lista de palabras clave de arquitectura encontradas en las rutas (ej. domain, adapter). */
    private final List<String> detectedKeywords;

    /** Distribución de componentes por paquete o capa principal. */
    private final Map<String, Integer> packageComponentDistribution;

    /** Profundidad máxima en segmentos de directorio alcanzada. */
    private final int maxPathDepth;

    /** Cantidad total de rutas estructurales distintas. */
    private final int totalStructuralPaths;

    /** Notas y observaciones factuales generadas sobre la estructura. */
    private final List<String> evidenceNotes;

    /**
     * Implementaciones de puertos de entrada (inbound).
     * Clave: nombre de la interfaz (UseCase/Port inbound), Valor: clase que la implementa.
     * Ejemplo: AnalyzeRepositoryUseCase → AnalyzeRepositoryService.
     */
    private final Map<String, String> inboundPortImplementations;

    /**
     * Adaptadores de puertos de salida (outbound).
     * Clave: nombre del adaptador, Valor: nombre del puerto (interfaz) que implementa.
     * Ejemplo: OllamaAdapter → ArchitectureSynthesisPort.
     */
    private final Map<String, String> outboundAdapterImplementations;

    /**
     * Evidencias de buenas prácticas de ingeniería detectadas en el proyecto
     * (inyección de dependencias, tests, etc.).
     */
    private final EngineeringEvidence engineeringEvidence;

    // ── Constructor ──────────────────────────────────────────────────────────

    public ArchitectureEvidenceResult(List<String> structuralPaths,
                                      List<String> detectedKeywords,
                                      Map<String, Integer> packageComponentDistribution,
                                      int maxPathDepth,
                                      int totalStructuralPaths,
                                      List<String> evidenceNotes,
                                      Map<String, String> inboundPortImplementations,
                                      Map<String, String> outboundAdapterImplementations,
                                      EngineeringEvidence engineeringEvidence) {
        this.structuralPaths = structuralPaths != null ? Collections.unmodifiableList(structuralPaths) : Collections.emptyList();
        this.detectedKeywords = detectedKeywords != null ? Collections.unmodifiableList(detectedKeywords) : Collections.emptyList();
        this.packageComponentDistribution = packageComponentDistribution != null ? Collections.unmodifiableMap(packageComponentDistribution) : Collections.emptyMap();
        this.maxPathDepth = maxPathDepth;
        this.totalStructuralPaths = totalStructuralPaths;
        this.evidenceNotes = evidenceNotes != null ? Collections.unmodifiableList(evidenceNotes) : Collections.emptyList();
        this.inboundPortImplementations = inboundPortImplementations != null ? Collections.unmodifiableMap(inboundPortImplementations) : Collections.emptyMap();
        this.outboundAdapterImplementations = outboundAdapterImplementations != null ? Collections.unmodifiableMap(outboundAdapterImplementations) : Collections.emptyMap();
        this.engineeringEvidence = engineeringEvidence != null ? engineeringEvidence
                : new EngineeringEvidence(0, Collections.emptyList(), false, false, 0, false, null, null, null, null);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public List<String> getStructuralPaths() { return structuralPaths; }
    public List<String> getDetectedKeywords() { return detectedKeywords; }
    public Map<String, Integer> getPackageComponentDistribution() { return packageComponentDistribution; }
    public int getMaxPathDepth() { return maxPathDepth; }
    public int getTotalStructuralPaths() { return totalStructuralPaths; }
    public List<String> getEvidenceNotes() { return evidenceNotes; }

    /** Implementaciones de puertos de entrada (application use-case implementations). */
    public Map<String, String> getInboundPortImplementations() { return inboundPortImplementations; }

    /** Adaptadores de puertos de salida (infrastructure adapter implementations). */
    public Map<String, String> getOutboundAdapterImplementations() { return outboundAdapterImplementations; }

    /** Evidencias de buenas prácticas de ingeniería (DI, tests, etc.). */
    public EngineeringEvidence getEngineeringEvidence() { return engineeringEvidence; }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<String> structuralPaths;
        private List<String> detectedKeywords;
        private Map<String, Integer> packageComponentDistribution;
        private int maxPathDepth;
        private int totalStructuralPaths;
        private List<String> evidenceNotes;
        private Map<String, String> inboundPortImplementations;
        private Map<String, String> outboundAdapterImplementations;
        private EngineeringEvidence engineeringEvidence;

        public Builder structuralPaths(List<String> v) { this.structuralPaths = v; return this; }
        public Builder detectedKeywords(List<String> v) { this.detectedKeywords = v; return this; }
        public Builder packageComponentDistribution(Map<String, Integer> v) { this.packageComponentDistribution = v; return this; }
        public Builder maxPathDepth(int v) { this.maxPathDepth = v; return this; }
        public Builder totalStructuralPaths(int v) { this.totalStructuralPaths = v; return this; }
        public Builder evidenceNotes(List<String> v) { this.evidenceNotes = v; return this; }
        public Builder inboundPortImplementations(Map<String, String> v) { this.inboundPortImplementations = v; return this; }
        public Builder outboundAdapterImplementations(Map<String, String> v) { this.outboundAdapterImplementations = v; return this; }
        public Builder engineeringEvidence(EngineeringEvidence v) { this.engineeringEvidence = v; return this; }

        public ArchitectureEvidenceResult build() {
            return new ArchitectureEvidenceResult(
                    structuralPaths, detectedKeywords, packageComponentDistribution,
                    maxPathDepth, totalStructuralPaths, evidenceNotes,
                    inboundPortImplementations, outboundAdapterImplementations,
                    engineeringEvidence);
        }
    }

    // ── Evidencias de ingeniería ──────────────────────────────────────────────

    // ── Evidencias de ingeniería (stack-agnostic) ─────────────────────────────

    /**
     * Evidencias de buenas prácticas de ingeniería detectadas de forma estática.
     *
     * <p>Campos genéricos (válidos para cualquier stack):
     * <ul>
     *   <li>{@code testFilesDetected}: cantidad total de archivos de test encontrados.</li>
     *   <li>{@code testDirectories}: directorios de test identificados (test/, tests/, __tests__/, etc.).</li>
     *   <li>{@code testScriptDetected}: si existe un script {@code test} en {@code package.json}.</li>
     * </ul>
     * Campos específicos de Spring/Java:
     * <ul>
     *   <li>{@code springConfigurationDetected}: clase {@code @Configuration} detectada.</li>
     *   <li>{@code beanDefinitions}: número de métodos {@code @Bean}.</li>
     *   <li>{@code constructorInjectionDetected}: inyección por constructor detectada.</li>
     * </ul>
     * Metadata del proyecto (extraída de manifiestos como {@code package.json}):
     * <ul>
     *   <li>{@code projectName}: nombre declarado en el manifiesto.</li>
     *   <li>{@code projectDescription}: descripción declarada en el manifiesto.</li>
     *   <li>{@code mainEntry}: punto de entrada principal declarado.</li>
     *   <li>{@code testScript}: comando de test declarado en scripts.</li>
     * </ul>
     * Ninguna ausencia de campo niega la existencia de una práctica.
     */
    public record EngineeringEvidence(
            // --- Genéricos (multiplataforma) ---
            int testFilesDetected,
            List<String> testDirectories,
            boolean testScriptDetected,
            // --- Spring/Java-specific ---
            boolean springConfigurationDetected,
            int beanDefinitions,
            boolean constructorInjectionDetected,
            // --- Metadata de manifiesto (package.json, pom.xml name, etc.) ---
            String projectName,
            String projectDescription,
            String mainEntry,
            String testScript
    ) {}
}
