package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.model.AnalysisContext;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Etapa 6 del Pipeline: Context Builder.
 * Consolida las evidencias y métricas recolectadas en las Etapas 2 a 5 en un prompt
 * estructurado en formato Markdown, optimizado para ser consumido por el motor de IA (Etapa 7).
 */
@Component
public class ContextBuilderStage {

    public AnalysisContext buildContext(FetchCodeRequest request,
                                        ScannedFileMap scannedFiles,
                                        TechnologyStack techStack,
                                        ComponentAnalysisResult componentAnalysis,
                                        ArchitectureEvidenceResult architectureEvidence) {

        String systemPrompt = """
                Eres un Ingeniero de Software Senior y Arquitecto de Soluciones experto en Ingeniería Inversa de Software.
                Analiza exclusivamente las evidencias determinísticas proporcionadas del repositorio para redactar un informe técnico profesional y riguroso.

                Reglas estrictas de análisis:
                - No inventes componentes, dependencias, vulnerabilidades, métricas ni tecnologías no presentes en las evidencias.
                - Distingue claramente los hechos observados (rutas, archivos, estereotipos) de las inferencias arquitectónicas.
                - Si la evidencia no permite concluir un aspecto específico, indícalo explícitamente como "No concluyente".
                - No afirmes calidad interna, seguridad, complejidad ciclomática o cobertura de código sin evidencias explícitas.
                - Toda inferencia arquitectónica debe citar textualmente las evidencias de rutas y componentes que la sustentan.
                """;

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("# Radiografía de Ingeniería Inversa del Repositorio\n\n");

        // 1. Ficha Técnica y Métricas
        userPromptBuilder.append("## 1. Ficha Técnica y Métricas Generales\n");
        if (request != null) {
            userPromptBuilder.append("- **Project Key**: ").append(request.getProjectKey()).append("\n");
            userPromptBuilder.append("- **Fuente**: ").append(request.getSourceType()).append("\n");
        }
        if (scannedFiles != null) {
            userPromptBuilder.append("- **Total de Archivos**: ").append(scannedFiles.getTotalFiles()).append("\n");
            userPromptBuilder.append("- **Total de Directorios**: ").append(scannedFiles.getTotalDirectories()).append("\n");
            userPromptBuilder.append("- **Conteo por Extensión**: ").append(scannedFiles.getExtensionCounts()).append("\n");
        }
        userPromptBuilder.append("\n");

        // 2. Stack Tecnológico
        userPromptBuilder.append("## 2. Stack Tecnológico Detectado\n");
        if (techStack != null) {
            userPromptBuilder.append("- **Lenguaje Principal**: ").append(techStack.getMainLanguage()).append("\n");
            userPromptBuilder.append("- **Framework Principal**: ").append(techStack.getMainFramework()).append("\n");
            userPromptBuilder.append("- **Herramienta de Compilación**: ").append(techStack.getBuildTool()).append("\n");
            userPromptBuilder.append("- **Bases de Datos Detectadas**: ").append(techStack.getDatabasesDetected()).append("\n");
            userPromptBuilder.append("- **Librerías Clave**: ").append(techStack.getKeyLibraries()).append("\n");
        }
        userPromptBuilder.append("\n");

        // 3. Catálogo de Componentes
        userPromptBuilder.append("## 3. Catálogo de Componentes Identificados\n");
        if (componentAnalysis != null) {
            userPromptBuilder.append("- **Total de Componentes**: ").append(componentAnalysis.getTotalComponents()).append("\n");
            userPromptBuilder.append("- **Distribución por Estereotipo**: ").append(componentAnalysis.getComponentCounts()).append("\n");
            userPromptBuilder.append("- **Lista de Componentes y Rutas de Evidencia**:\n");
            if (componentAnalysis.getComponents() != null) {
                for (DetectedComponent comp : componentAnalysis.getComponents()) {
                    userPromptBuilder.append("  - `").append(comp.getName()).append("` [")
                            .append(comp.getType()).append("] -> ")
                            .append(comp.getRelativePath()).append("\n");
                }
            }
        }
        userPromptBuilder.append("\n");

        // 4. Evidencias Estructurales de Arquitectura
        userPromptBuilder.append("## 4. Evidencias y Señales Estructurales de Arquitectura\n");
        if (architectureEvidence != null) {
            userPromptBuilder.append("- **Rutas Estructurales Distintas**: ").append(architectureEvidence.getTotalStructuralPaths()).append("\n");
            userPromptBuilder.append("- **Profundidad Máxima de Rutas**: ").append(architectureEvidence.getMaxPathDepth()).append(" segmentos\n");
            userPromptBuilder.append("- **Palabras Clave de Arquitectura Encontradas**: ").append(architectureEvidence.getDetectedKeywords()).append("\n");
            userPromptBuilder.append("- **Distribución de Componentes por Paquete/Capa**: ").append(architectureEvidence.getPackageComponentDistribution()).append("\n");
            userPromptBuilder.append("- **Notas Factuales de Evidencia**:\n");
            if (architectureEvidence.getEvidenceNotes() != null) {
                for (String note : architectureEvidence.getEvidenceNotes()) {
                    userPromptBuilder.append("  - ").append(note).append("\n");
                }
            }
        }
        userPromptBuilder.append("\n");

        // 5. Directivas de Análisis para la IA
        userPromptBuilder.append("""
                ## 5. Directivas para la Síntesis Arquitectónica (Ollama)
                Con base en la radiografía factual anterior, redacta el análisis en Markdown respondiendo a:
                1. Determina el estilo o patrón arquitectónico más probable (Hexagonal / Puertos y Adaptadores, Clean Architecture, Layered MVC, Monolito Modular, etc.). Incluye:
                   - Estilo principal inferido
                   - Nivel de confianza estimado (de 0.0 a 1.0)
                   - Evidencias concretas de rutas y componentes que lo sustentan
                   - Estilos alternativos plausibles
                   - Aspectos o limitaciones que no pueden comprobarse con la evidencia disponible (ej. dirección real de dependencias importadas).
                2. Evalúa la organización de capas y el nivel de desacoplamiento aparente según la distribución de componentes y paquetes.
                3. Emite 3 recomendaciones técnicas de alto impacto para mejorar la arquitectura, mantenibilidad o completitud de la solución.
                """);

        String userPrompt = userPromptBuilder.toString();
        String formattedContextPrompt = systemPrompt.trim() + "\n\n" + userPrompt.trim();

        return AnalysisContext.builder()
                .systemPrompt(systemPrompt.trim())
                .userPrompt(userPrompt.trim())
                .formattedContextPrompt(formattedContextPrompt)
                .build();
    }
}
