package com.codeinsight.api.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Modelo de Dominio: Resultado de Evidencias Arquitectónicas (Etapa 5).
 * 
 * Contiene los hallazgos factuales recolectados de la estructura del proyecto (rutas, palabras clave,
 * profundidad máxima y distribución de componentes por paquetes).
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
     * Relaciones detectadas puerto→adaptador.
     * Clave: nombre del puerto (interfaz), Valor: nombre del adaptador (clase que la implementa).
     */
    private final Map<String, String> portAdapterRelations;

    public ArchitectureEvidenceResult(List<String> structuralPaths,
                                       List<String> detectedKeywords,
                                       Map<String, Integer> packageComponentDistribution,
                                       int maxPathDepth,
                                       int totalStructuralPaths,
                                       List<String> evidenceNotes,
                                       Map<String, String> portAdapterRelations) {
        this.structuralPaths = structuralPaths != null ? Collections.unmodifiableList(structuralPaths) : Collections.emptyList();
        this.detectedKeywords = detectedKeywords != null ? Collections.unmodifiableList(detectedKeywords) : Collections.emptyList();
        this.packageComponentDistribution = packageComponentDistribution != null ? Collections.unmodifiableMap(packageComponentDistribution) : Collections.emptyMap();
        this.maxPathDepth = maxPathDepth;
        this.totalStructuralPaths = totalStructuralPaths;
        this.evidenceNotes = evidenceNotes != null ? Collections.unmodifiableList(evidenceNotes) : Collections.emptyList();
        this.portAdapterRelations = portAdapterRelations != null ? Collections.unmodifiableMap(portAdapterRelations) : Collections.emptyMap();
    }

    public List<String> getStructuralPaths() {
        return structuralPaths;
    }

    public List<String> getDetectedKeywords() {
        return detectedKeywords;
    }

    public Map<String, Integer> getPackageComponentDistribution() {
        return packageComponentDistribution;
    }

    public int getMaxPathDepth() {
        return maxPathDepth;
    }

    public int getTotalStructuralPaths() {
        return totalStructuralPaths;
    }

    public List<String> getEvidenceNotes() {
        return evidenceNotes;
    }

    /** Devuelve las relaciones detectadas puerto→adaptador (clave=puerto, valor=adaptador). */
    public Map<String, String> getPortAdapterRelations() {
        return portAdapterRelations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> structuralPaths;
        private List<String> detectedKeywords;
        private Map<String, Integer> packageComponentDistribution;
        private int maxPathDepth;
        private int totalStructuralPaths;
        private List<String> evidenceNotes;
        private Map<String, String> portAdapterRelations;

        public Builder structuralPaths(List<String> structuralPaths) {
            this.structuralPaths = structuralPaths;
            return this;
        }

        public Builder detectedKeywords(List<String> detectedKeywords) {
            this.detectedKeywords = detectedKeywords;
            return this;
        }

        public Builder packageComponentDistribution(Map<String, Integer> packageComponentDistribution) {
            this.packageComponentDistribution = packageComponentDistribution;
            return this;
        }

        public Builder maxPathDepth(int maxPathDepth) {
            this.maxPathDepth = maxPathDepth;
            return this;
        }

        public Builder totalStructuralPaths(int totalStructuralPaths) {
            this.totalStructuralPaths = totalStructuralPaths;
            return this;
        }

        public Builder evidenceNotes(List<String> evidenceNotes) {
            this.evidenceNotes = evidenceNotes;
            return this;
        }

        /** Asigna las relaciones puerto→adaptador detectadas. */
        public Builder portAdapterRelations(Map<String, String> portAdapterRelations) {
            this.portAdapterRelations = portAdapterRelations;
            return this;
        }

        public ArchitectureEvidenceResult build() {
            return new ArchitectureEvidenceResult(structuralPaths, detectedKeywords, packageComponentDistribution, maxPathDepth, totalStructuralPaths, evidenceNotes, portAdapterRelations);
        }
    }
}
