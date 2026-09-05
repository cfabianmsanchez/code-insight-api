package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.ai.PromptProvider;
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
 * Utiliza {@link PromptProvider} para cargar las plantillas de prompts versionadas fuera del código Java.
 */
@Component
public class ContextBuilderStage {

    private final PromptProvider promptProvider;

    public ContextBuilderStage(PromptProvider promptProvider) {
        this.promptProvider = promptProvider;
    }

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

        String systemPrompt = promptProvider.systemPrompt();

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("# Radiografía de Ingeniería Inversa del Repositorio\n\n");

        // 0. Metadata del Proyecto (extraída de manifiestos: package.json, etc.)
        EngineeringEvidence engMeta = (architectureEvidence != null) ? architectureEvidence.getEngineeringEvidence() : null;
        if (engMeta != null && (engMeta.projectName() != null || engMeta.projectDescription() != null || engMeta.mainEntry() != null)) {
            userPromptBuilder.append("## 0. Metadata del Proyecto (extraída del manifiesto)\n");
            if (engMeta.projectName() != null)
                userPromptBuilder.append("- **Nombre del Proyecto**: ").append(engMeta.projectName()).append("\n");
            if (engMeta.projectDescription() != null)
                userPromptBuilder.append("- **Descripción Declarada**: ").append(engMeta.projectDescription()).append("\n");
            if (engMeta.mainEntry() != null)
                userPromptBuilder.append("- **Punto de Entrada Principal**: ").append(engMeta.mainEntry()).append("\n");
            if (engMeta.testScript() != null)
                userPromptBuilder.append("- **Script de Test**: `").append(engMeta.testScript()).append("`\n");
            userPromptBuilder.append("\n");
        }

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

            // 4c. Evidencias de ingeniería (stack-aware)
            EngineeringEvidence eng = architectureEvidence.getEngineeringEvidence();
            if (eng != null) {
                userPromptBuilder.append("\n### 4c. Evidencias de Buenas Prácticas de Ingeniería\n");
                userPromptBuilder.append("IMPORTANTE: Estas evidencias confirman prácticas presentes. ")
                        .append("La ausencia de una evidencia aquí NO implica que la práctica no exista.\n");

                // Genérico: tests multiplataforma
                userPromptBuilder.append("- **Archivos de Test Detectados (multiplataforma)**: ")
                        .append(eng.testFilesDetected()).append("\n");
                if (!eng.testDirectories().isEmpty()) {
                    userPromptBuilder.append("- **Directorios de Test Encontrados**: ")
                            .append(eng.testDirectories()).append("\n");
                }
                if (eng.testScriptDetected()) {
                    userPromptBuilder.append("- **Script de Test (package.json)**: `")
                            .append(eng.testScript()).append("`\n");
                }

                // Spring/Java específico — solo mostrar si hay evidencia relevante
                if (eng.springConfigurationDetected() || eng.beanDefinitions() > 0 || eng.constructorInjectionDetected()) {
                    userPromptBuilder.append("- **Inyección de Dependencias Spring (@Configuration)**: ")
                            .append(eng.springConfigurationDetected() ? "DETECTADA" : "No observada").append("\n");
                    userPromptBuilder.append("- **Definiciones @Bean**: ").append(eng.beanDefinitions()).append("\n");
                    userPromptBuilder.append("- **Inyección por Constructor**: ")
                            .append(eng.constructorInjectionDetected() ? "DETECTADA" : "No observada").append("\n");
                }
            }
        }
        userPromptBuilder.append("\n");

        // 5. Directivas de Análisis para la IA (cargadas desde PromptProvider)
        userPromptBuilder.append(promptProvider.analysisDirectives());

        String userPrompt = userPromptBuilder.toString();
        String formattedContextPrompt = systemPrompt.trim() + "\n\n" + userPrompt.trim();

        return AnalysisContext.builder()
                .systemPrompt(systemPrompt.trim())
                .userPrompt(userPrompt.trim())
                .formattedContextPrompt(formattedContextPrompt)
                .build();
    }
}
