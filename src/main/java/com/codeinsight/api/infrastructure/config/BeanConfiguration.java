package com.codeinsight.api.infrastructure.config;

import com.codeinsight.api.application.pipeline.stage.AiSynthesisStage;
import com.codeinsight.api.application.pipeline.stage.ArchitectureEvidenceDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ComponentDetectorStage;
import com.codeinsight.api.application.pipeline.stage.ContextBuilderStage;
import com.codeinsight.api.application.pipeline.stage.FileScannerStage;
import com.codeinsight.api.application.pipeline.stage.RepositoryLoaderStage;
import com.codeinsight.api.application.pipeline.stage.TechnologyDetectorStage;
import com.codeinsight.api.application.port.in.AnalyzeRepositoryUseCase;
import com.codeinsight.api.application.service.AnalyzeRepositoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Infraestructura de Beans de Spring.
 *
 * Declara la instanciación e inyección de dependencias para los casos de uso de la capa de aplicación,
 * manteniendo el dominio agnóstico de frameworks.
 */
@Configuration
public class BeanConfiguration {

  /**
   * Registra el servicio {@link AnalyzeRepositoryService} como implementación del puerto de entrada
   * {@link AnalyzeRepositoryUseCase} inyectando las etapas del pipeline.
   */
  @Bean
  public AnalyzeRepositoryUseCase analyzeRepositoryUseCase(
    RepositoryLoaderStage repositoryLoader,
    FileScannerStage fileScanner,
    TechnologyDetectorStage technologyDetector,
    ComponentDetectorStage componentDetector,
    ArchitectureEvidenceDetectorStage architectureEvidenceDetector,
    ContextBuilderStage contextBuilder,
    AiSynthesisStage aiSynthesis
  ) {
    return new AnalyzeRepositoryService(
      repositoryLoader,
      fileScanner,
      technologyDetector,
      componentDetector,
      architectureEvidenceDetector,
      contextBuilder,
      aiSynthesis
    );
  }
}
