package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.pipeline.rules.ComponentDetectionRules;
import com.codeinsight.api.application.pipeline.rules.ComponentDetectionRules.ComponentRule;
import com.codeinsight.api.application.pipeline.rules.ComponentDetectionRules.PathConventionRule;
import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.ComponentType;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.ScannedFileMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Etapa 4: Clasifica los componentes de código según estereotipos de arquitectura.
 */
@Component
public class ComponentDetectorStage {

  private static final Logger log = LoggerFactory.getLogger(
    ComponentDetectorStage.class
  );

  /**
   * Recorre los archivos escaneados del proyecto e identifica los componentes de
   * código fuente.
   *
   * @param scannedFiles Mapa de archivos escaneados.
   * @return {@link ComponentAnalysisResult} con la lista de componentes
   *         detectados y sus conteos.
   */
  public ComponentAnalysisResult detect(ScannedFileMap scannedFiles) {
    if (scannedFiles == null || scannedFiles.getRootPath() == null) {
      throw new InvalidRepositoryException(
        "ScannedFileMap and root path must not be null"
      );
    }

    List<DetectedComponent> detectedComponents = new ArrayList<>();
    Map<String, Integer> componentCounts = new HashMap<>();

    Path rootPath = scannedFiles.getRootPath();
    List<String> relativePaths = scannedFiles.getRelativeFilePaths();

    if (relativePaths != null) {
      for (String relativePath : relativePaths) {
        if (isSourceCodeFile(relativePath)) {
          Path absolutePath = rootPath.resolve(relativePath);
          if (Files.exists(absolutePath) && Files.isRegularFile(absolutePath)) {
            try {
              String content = Files.readString(absolutePath);
              ComponentType type = detectComponentType(relativePath, content);
              if (type != null) {
                String className = extractClassName(relativePath);
                DetectedComponent component = new DetectedComponent(
                  className,
                  type,
                  relativePath
                );
                detectedComponents.add(component);

                String category = type.name();
                componentCounts.put(
                  category,
                  componentCounts.getOrDefault(category, 0) + 1
                );
              }
            } catch (IOException e) {
              log.warn(
                "Could not read file for component detection {}: {}",
                relativePath,
                e.getMessage()
              );
            }
          }
        }
      }
    }

    return ComponentAnalysisResult.builder()
      .totalComponents(detectedComponents.size())
      .componentCounts(componentCounts)
      .components(detectedComponents)
      .build();
  }

  /**
   * Determina si la ruta corresponde a un archivo de código fuente analizable,
   * filtrando carpetas y archivos de test.
   * <p>
   * Las extensiones válidas y los patrones de exclusión se obtienen de
   * {@link ComponentDetectionRules}.
   */
  private boolean isSourceCodeFile(String relativePath) {
    String lower = relativePath.toLowerCase();

    for (String segment : ComponentDetectionRules.TEST_PATH_SEGMENTS) {
      if (lower.contains(segment)) return false;
    }
    for (String suffix : ComponentDetectionRules.TEST_FILE_SUFFIXES) {
      if (lower.endsWith(suffix.toLowerCase())) return false;
    }

    return ComponentDetectionRules.SOURCE_EXTENSIONS.stream().anyMatch(
      lower::endsWith
    );
  }

  /**
   * Analiza el texto del archivo y su ruta para inferir el tipo de componente.
   * Primero aplica las reglas de contenido del lenguaje correspondiente;
   * si ninguna coincide, aplica las convenciones de ruta como fallback.
   */
  private ComponentType detectComponentType(
    String relativePath,
    String content
  ) {
    String lowerPath = relativePath.toLowerCase();
    String cleanContent = stripComments(content).toLowerCase();

    // Buscar la lista de reglas correspondiente a la extensión del archivo
    for (Map.Entry<
      String,
      List<ComponentRule>
    > entry : ComponentDetectionRules.RULES_BY_EXTENSION.entrySet()) {
      if (lowerPath.endsWith(entry.getKey())) {
        ComponentType type = matchRules(entry.getValue(), cleanContent);
        if (type != null) return type;
        break;
      }
    }

    return matchPathConventions(lowerPath);
  }

  private ComponentType matchRules(
    List<ComponentRule> rules,
    String cleanContent
  ) {
    for (ComponentRule rule : rules) {
      if (rule.matcher().test(cleanContent)) {
        return rule.type();
      }
    }
    return null;
  }

  /**
   * Fallback: infiere el tipo de componente a partir de convenciones de directorio
   * y sufijos de nombre de archivo definidos en {@link ComponentDetectionRules#PATH_CONVENTION_RULES}.
   */
  private ComponentType matchPathConventions(String lowerPath) {
    for (PathConventionRule rule : ComponentDetectionRules.PATH_CONVENTION_RULES) {
      boolean matchesDir = rule
        .directorySegments()
        .stream()
        .anyMatch(lowerPath::contains);
      boolean matchesSuffix = rule
        .fileSuffixes()
        .stream()
        .anyMatch(lowerPath::endsWith);
      if (matchesDir || matchesSuffix) {
        return rule.type();
      }
    }
    return null;
  }

  /**
   * Elimina los comentarios de bloque y de línea para evitar falsos positivos.
   */
  private String stripComments(String content) {
    if (content == null) {
      return "";
    }
    String noBlock = content.replaceAll("(?s)/\\*.*?\\*/", "");
    return noBlock.replaceAll("//.*", "");
  }

  private String extractClassName(String relativePath) {
    Path path = Path.of(relativePath);
    String fileName = path.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      return fileName.substring(0, dotIndex);
    }
    return fileName;
  }
}
