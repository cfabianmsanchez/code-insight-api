package com.codeinsight.api.domain.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Modelo de Dominio: Mapa de Archivos Escaneados (Etapa 2).
 *
 * Contiene el resultado del escaneo del directorio temporal efímero:
 * totales de archivos/carpetas, distribución por extensión, rutas relativas y manifiestos del proyecto.
 */
public class ScannedFileMap {

  /** Ruta raíz del directorio temporal analizado. */
  private Path rootPath;
  /** Conteo de archivos regulares encontrados. */
  private int totalFiles;
  /** Conteo de directorios escaneados. */
  private int totalDirectories;
  /** Mapa de frecuencias de extensiones (ejemplo: .java -> 35). */
  private Map<String, Integer> extensionCounts;
  /** Lista de rutas relativas a la raíz del repositorio. */
  private List<String> relativeFilePaths;
  /** Lista de rutas a archivos manifiesto (pom.xml, package.json, etc.). */
  private List<Path> manifestFiles;

  public ScannedFileMap() {}

  public ScannedFileMap(
    Path rootPath,
    int totalFiles,
    int totalDirectories,
    Map<String, Integer> extensionCounts,
    List<String> relativeFilePaths,
    List<Path> manifestFiles
  ) {
    this.rootPath = rootPath;
    this.totalFiles = totalFiles;
    this.totalDirectories = totalDirectories;
    this.extensionCounts = extensionCounts;
    this.relativeFilePaths = relativeFilePaths;
    this.manifestFiles = manifestFiles;
  }

  public Path getRootPath() {
    return rootPath;
  }

  public int getTotalFiles() {
    return totalFiles;
  }

  public int getTotalDirectories() {
    return totalDirectories;
  }

  public Map<String, Integer> getExtensionCounts() {
    return extensionCounts;
  }

  public List<String> getRelativeFilePaths() {
    return relativeFilePaths;
  }

  public List<Path> getManifestFiles() {
    return manifestFiles;
  }

  /**
   * Resuelve las rutas relativas contra {@link #rootPath} y devuelve una lista
   * de rutas absolutas listas para lectura de contenido.
   * Útil para etapas que necesitan leer el contenido real de los archivos.
   *
   * @return Lista de {@link Path} absolutos de todos los archivos escaneados,
   *         o lista vacía si no hay rutas relativas o rootPath es nulo.
   */
  public List<Path> getAllFilePaths() {
    if (rootPath == null || relativeFilePaths == null) {
      return java.util.Collections.emptyList();
    }
    return relativeFilePaths
      .stream()
      .map(rel -> rootPath.resolve(rel))
      .toList();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Path rootPath;
    private int totalFiles;
    private int totalDirectories;
    private Map<String, Integer> extensionCounts;
    private List<String> relativeFilePaths;
    private List<Path> manifestFiles;

    public Builder rootPath(Path rootPath) {
      this.rootPath = rootPath;
      return this;
    }

    public Builder totalFiles(int totalFiles) {
      this.totalFiles = totalFiles;
      return this;
    }

    public Builder totalDirectories(int totalDirectories) {
      this.totalDirectories = totalDirectories;
      return this;
    }

    public Builder extensionCounts(Map<String, Integer> extensionCounts) {
      this.extensionCounts = extensionCounts;
      return this;
    }

    public Builder relativeFilePaths(List<String> relativeFilePaths) {
      this.relativeFilePaths = relativeFilePaths;
      return this;
    }

    public Builder manifestFiles(List<Path> manifestFiles) {
      this.manifestFiles = manifestFiles;
      return this;
    }

    public ScannedFileMap build() {
      return new ScannedFileMap(
        rootPath,
        totalFiles,
        totalDirectories,
        extensionCounts,
        relativeFilePaths,
        manifestFiles
      );
    }
  }
}
