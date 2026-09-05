package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.application.model.TempCodeDirectory;
import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.exception.InvalidRepositoryException;
import com.codeinsight.api.domain.exception.UnsupportedSourceTypeException;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Etapa 1 del Pipeline: Repository Loader.
 * 
 * Encargado de seleccionar el adaptador de adquisición correspondiente y cargar el código fuente
 * en una carpeta temporal efímera ({@link TempCodeDirectory}).
 */
@Component
public class RepositoryLoaderStage {

    /** Lista de cargadores de código inyectados por Spring. */
    private final List<CodeFetcherPort> fetchers;

    /**
     * Crea una instancia de la Etapa 1 inyectando la lista de cargadores disponibles.
     */
    public RepositoryLoaderStage(List<CodeFetcherPort> fetchers) {
        this.fetchers = fetchers;
    }

    /**
     * Ejecuta la adquisición de código delegando en el cargador que soporte la solicitud.
     *
     * @param request Solicitud con la fuente de código a cargar.
     * @return {@link TempCodeDirectory} con la ruta física y el tipo de fuente.
     */
    public TempCodeDirectory loadRepository(FetchCodeRequest request) {
        if (request == null) {
            throw new InvalidRepositoryException("FetchCodeRequest cannot be null");
        }

        CodeFetcherPort fetcher = fetchers.stream()
                .filter(f -> f.supports(request))
                .findFirst()
                .orElseThrow(() -> new UnsupportedSourceTypeException(
                        "No supported fetcher found for source type: " + request.getSourceType()));

        return fetcher.fetchCode(request);
    }
}
