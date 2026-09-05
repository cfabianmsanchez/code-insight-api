package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.model.AnalysisContext;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ArchitectureEvidenceResult.EngineeringEvidence;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.ScannedFileMap;
import com.codeinsight.api.domain.model.TechnologyStack;
import org.springframework.stereotype.Component;

/**
 * Etapa 6 del Pipeline: Context Builder.
 *
 * Consolida todas las evidencias determinísticas recopiladas (Etapas 2 a 5) en un prompt
 * estructurado en formato Markdown optimizado para ser consumido por el motor de síntesis IA (Etapa 7).
 * Incluye directivas anti-alucinación, sección de evidencias de ingeniería (DI/tests),
 * relaciones inbound/outbound y reglas estrictas para las recomendaciones finales.
 */
@Component
public class ContextBuilderStage {

    /**
     * Construye los prompts del sistema y usuario reuniendo las métricas y evidencias factuales.
     *
     * @param request              Solicitud original de análisis.
     * @param scannedFiles         Mapa de archivos escaneados.
     * @param techStack            Stack tecnológico detectado.
     * @param componentAnalysis    Análisis de componentes.
     * @param architectureEvidence Evidencias estructurales y de ingeniería de la arquitectura.
     * @return {@link AnalysisContext} con systemPrompt, userPrompt y formattedContextPrompt.
     */
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
                - PRINCIPIO CRÍTICO: La ausencia de una evidencia NO implica la ausencia de una práctica en el proyecto real.
                  Solo puedes afirmar que algo no existe si la evidencia lo demuestra explícitamente.
                """;

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("# Radiografía de Ingeniería Inversa del Repositorio\n\n");

        // 1. Ficha Técnica y Métricas Generales
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
            userPromptBuilder.append("- **Rutas Estructurales Distintas**: ")
                    .append(architectureEvidence.getTotalStructuralPaths()).append("\n");
            userPromptBuilder.append("- **Profundidad Máxima de Rutas**: ")
                    .append(architectureEvidence.getMaxPathDepth()).append(" segmentos\n");
            userPromptBuilder.append("- **Palabras Clave de Arquitectura Encontradas**: ")
                    .append(architectureEvidence.getDetectedKeywords()).append("\n");
            userPromptBuilder.append("- **Distribución de Componentes por Paquete/Capa**: ")
                    .append(architectureEvidence.getPackageComponentDistribution()).append("\n");
            userPromptBuilder.append("- **Notas Factuales de Evidencia**:\n");
            if (architectureEvidence.getEvidenceNotes() != null) {
                for (String note : architectureEvidence.getEvidenceNotes()) {
                    userPromptBuilder.append("  - ").append(note).append("\n");
                }
            }

            // 4a. Relaciones inbound (implementaciones de puertos de entrada)
            if (!architectureEvidence.getInboundPortImplementations().isEmpty()) {
                userPromptBuilder.append("\n### 4a. Relaciones Inbound (Puerto de Entrada → Implementación)\n");
                userPromptBuilder.append("Las siguientes interfaces de entrada son implementadas por clases de la capa application:\n");
                architectureEvidence.getInboundPortImplementations().forEach((port, impl) ->
                        userPromptBuilder.append("  - `").append(impl).append("` implementa puerto inbound `").append(port).append("`\n")
                );
            }

            // 4b. Relaciones outbound (adaptadores que implementan puertos de salida)
            if (!architectureEvidence.getOutboundAdapterImplementations().isEmpty()) {
                userPromptBuilder.append("\n### 4b. Relaciones Outbound (Adaptador → Puerto de Salida)\n");
                userPromptBuilder.append("Los siguientes adaptadores de infraestructura implementan puertos de salida:\n");
                architectureEvidence.getOutboundAdapterImplementations().forEach((adapter, port) ->
                        userPromptBuilder.append("  - `").append(adapter).append("` implementa puerto outbound `").append(port).append("`\n")
                );
            }

            // 4c. Evidencias de ingeniería detectadas
            EngineeringEvidence eng = architectureEvidence.getEngineeringEvidence();
            if (eng != null) {
                userPromptBuilder.append("\n### 4c. Evidencias de Buenas Prácticas de Ingeniería\n");
                userPromptBuilder.append("IMPORTANTE: Estas evidencias confirman prácticas presentes. ")
                        .append("La ausencia de una evidencia aquí NO implica que la práctica no exista.\n");
                userPromptBuilder.append("- **Inyección de Dependencias Spring (@Configuration)**: ")
                        .append(eng.springConfigurationDetected() ? "DETECTADA" : "No observada").append("\n");
                userPromptBuilder.append("- **Definiciones @Bean encontradas**: ")
                        .append(eng.beanDefinitions()).append("\n");
                userPromptBuilder.append("- **Inyección por Constructor**: ")
                        .append(eng.constructorInjectionDetected() ? "DETECTADA" : "No observada").append("\n");
                userPromptBuilder.append("- **Archivos de Test (src/test/)**: ")
                        .append(eng.testFilesDetected()).append("\n");
            }
        }
        userPromptBuilder.append("\n");

        // 5. Directivas de Análisis para la IA (prompt endurecido)
        userPromptBuilder.append("""
                ## 5. Directivas para la Síntesis Arquitectónica (Ollama)
                Con base EXCLUSIVAMENTE en la radiografía factual anterior, redacta el análisis en Markdown respondiendo a:

                **1. Clasificación Arquitectónica:**
                   Determina el estilo o patrón arquitectónico más probable. Incluye:
                   - Estilo principal inferido
                   - Nivel de confianza estimado (de 0.0 a 1.0)
                   - Evidencias CONCRETAS de rutas y componentes que lo sustentan (cita textualmente)
                   - Como máximo 2 estilos arquitectónicos alternativos, SOLO si existe evidencia concreta que los sustente.
                     Para cada alternativa, cita esa evidencia. Si no hay evidencia suficiente, responde "No concluyente".
                   - Aspectos no verificables con la evidencia disponible

                **2. Organización de Capas:**
                   Evalúa el desacoplamiento aparente según la distribución de componentes, paquetes y relaciones detectadas.

                **3. Recomendaciones Técnicas:**
                   Emite EXACTAMENTE 3 recomendaciones técnicas de alto impacto.
                   Reglas obligatorias para cada recomendación:
                   - Debe estar respaldada por una evidencia CONCRETA de las secciones anteriores.
                   - NO recomiendes implementar tecnologías, prácticas o patrones cuya presencia ya esté confirmada en las evidencias (sección 4c).
                   - La ausencia de una evidencia NO significa ausencia de la práctica; no especules sobre lo que no está en el reporte.
                   - Si no puedes formular 3 recomendaciones basadas en evidencia concreta, indica "No concluyente" en lugar de inventar.
                   - Devuelve exactamente 3 recomendaciones, no más, no menos.
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
