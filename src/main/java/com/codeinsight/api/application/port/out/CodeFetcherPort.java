package com.codeinsight.api.application.port.out;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.domain.model.FetchCodeRequest;

/**
 * Puerto de Salida (Outbound Port): Cargador de Código Fuente.
 *
 * Define la interfaz que deben implementar los adaptadores de infraestructura
 * encargados de descargar o descomprimir el código fuente en un directorio temporal.
 */
public interface CodeFetcherPort {
  /**
   * Evalúa si este cargador es capaz de procesar el tipo de fuente solicitado.
   *
   * @param request Datos de la solicitud de adquisición de código.
   * @return {@code true} si la fuente es soportada; {@code false} en caso contrario.
   */
  boolean supports(FetchCodeRequest request);

  /**
   * Adquiere el código fuente (clonando un repositorio Git o descomprimiendo un archivo ZIP)
   * y lo almacena en una estructura de directorio temporal manejada.
   *
   * @param request Parámetros de adquisición de código.
   * @return {@link TempCodeDirectory} que contiene la carpeta efímera generada.
   */
  TempCodeDirectory fetchCode(FetchCodeRequest request);
}
