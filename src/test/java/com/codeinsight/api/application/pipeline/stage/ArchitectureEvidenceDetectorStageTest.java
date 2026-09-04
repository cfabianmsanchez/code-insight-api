package com.codeinsight.api.application.pipeline.stage;

import com.codeinsight.api.domain.model.ArchitectureEvidenceResult;
import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.ComponentType;
import com.codeinsight.api.domain.model.DetectedComponent;
import com.codeinsight.api.domain.model.ScannedFileMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureEvidenceDetectorStageTest {

    private final ArchitectureEvidenceDetectorStage stage = new ArchitectureEvidenceDetectorStage();

    @Test
    void detect_shouldGatherFactualArchitecturalEvidencesCorrectly(@TempDir Path tempDir) {
        List<String> files = List.of(
                "src/main/java/com/demo/domain/model/User.java",
                "src/main/java/com/demo/application/pipeline/stage/ScannerStage.java",
                "src/main/java/com/demo/infrastructure/adapter/in/rest/UserController.java"
        );

        ScannedFileMap scannedFiles = ScannedFileMap.builder()
                .rootPath(tempDir)
                .relativeFilePaths(files)
                .build();

        ComponentAnalysisResult componentAnalysis = ComponentAnalysisResult.builder()
                .totalComponents(1)
                .components(List.of(
                        new DetectedComponent("UserController", ComponentType.CONTROLLER, "src/main/java/com/demo/infrastructure/adapter/in/rest/UserController.java")
                ))
                .build();

        ArchitectureEvidenceResult result = stage.detect(scannedFiles, componentAnalysis);

        assertNotNull(result);
        assertEquals(3, result.getTotalStructuralPaths());
        assertTrue(result.getMaxPathDepth() > 0);
        assertTrue(result.getDetectedKeywords().contains("domain"));
        assertTrue(result.getDetectedKeywords().contains("application"));
        assertTrue(result.getDetectedKeywords().contains("infrastructure"));
        assertTrue(result.getDetectedKeywords().contains("adapter"));
        assertEquals(1, result.getPackageComponentDistribution().get("infrastructure"));
        assertTrue(result.getEvidenceNotes().stream().anyMatch(note -> note.contains("Palabras clave de arquitectura encontradas")));
    }
}
