package com.codeinsight.api.domain.pipeline.stage;

import com.codeinsight.api.application.port.out.CodeFetcherPort;
import com.codeinsight.api.domain.model.FetchCodeRequest;
import com.codeinsight.api.domain.model.TempCodeDirectory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Etapa 1 del Pipeline: Repository Loader.
 * Encargado de adquirir y cargar el código fuente desde una URL de GitHub o un archivo .ZIP
 * dentro de un directorio temporal efímero (TempCodeDirectory).
 */
@Component
public class RepositoryLoaderStage {

    private final List<CodeFetcherPort> fetchers;

    public RepositoryLoaderStage(List<CodeFetcherPort> fetchers) {
        this.fetchers = fetchers;
    }

    /**
     * Ejecuta la Etapa 1: Carga efímera del repositorio.
     *
     * @param request Solicitud de adquisición con origen GITHUB_REPO o ZIP_FILE.
     * @return TempCodeDirectory listo para las etapas de escaneo y detección.
     */
    public TempCodeDirectory loadRepository(FetchCodeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("FetchCodeRequest cannot be null");
        }

        CodeFetcherPort fetcher = fetchers.stream()
                .filter(f -> f.supports(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No supported fetcher found for source type: " + request.getSourceType()));

        return fetcher.fetchCode(request);
    }
}
