package com.codeinsight.api.application.pipeline.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeinsight.api.domain.model.ComponentAnalysisResult;
import com.codeinsight.api.domain.model.ComponentType;
import com.codeinsight.api.domain.model.ScannedFileMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComponentDetectorStageTest {

  private final ComponentDetectorStage stage = new ComponentDetectorStage();

  @Test
  void detect_shouldIdentifySpringComponentsCorrectly(@TempDir Path tempDir)
    throws IOException {
    Path controllerPath = tempDir.resolve("UserController.java");
    Files.writeString(
      controllerPath,
      """
      package com.example.demo;
      import org.springframework.web.bind.annotation.RestController;

      @RestController
      public class UserController {}
      """
    );

    Path servicePath = tempDir.resolve("UserService.java");
    Files.writeString(
      servicePath,
      """
      package com.example.demo;
      import org.springframework.stereotype.Service;

      @Service
      public class UserService {}
      """
    );

    Path repoPath = tempDir.resolve("UserRepository.java");
    Files.writeString(
      repoPath,
      """
      package com.example.demo;
      import org.springframework.data.jpa.repository.JpaRepository;

      public interface UserRepository extends JpaRepository<Object, Long> {}
      """
    );

    ScannedFileMap scannedFiles = ScannedFileMap.builder()
      .rootPath(tempDir)
      .relativeFilePaths(
        List.of(
          "UserController.java",
          "UserService.java",
          "UserRepository.java"
        )
      )
      .build();

    ComponentAnalysisResult result = stage.detect(scannedFiles);

    assertNotNull(result);
    assertEquals(3, result.getTotalComponents());
    assertEquals(
      1,
      result.getComponentCounts().get(ComponentType.CONTROLLER.name())
    );
    assertEquals(
      1,
      result.getComponentCounts().get(ComponentType.SERVICE.name())
    );
    assertEquals(
      1,
      result.getComponentCounts().get(ComponentType.REPOSITORY.name())
    );
    assertTrue(
      result
        .getComponents()
        .stream()
        .anyMatch(
          c ->
            c.getName().equals("UserController") &&
            c.getType() == ComponentType.CONTROLLER
        )
    );
  }

  @Test
  void detect_shouldNotMisclassifyControllerAdviceAsController(
    @TempDir Path tempDir
  ) throws IOException {
    Path advicePath = tempDir.resolve("GlobalExceptionHandler.java");
    Files.writeString(
      advicePath,
      """
      package com.example.demo;
      import org.springframework.web.bind.annotation.RestControllerAdvice;

      @RestControllerAdvice
      public class GlobalExceptionHandler {}
      """
    );

    Path componentPath = tempDir.resolve("JwtParser.java");
    Files.writeString(
      componentPath,
      """
      package com.example.demo;
      import org.springframework.stereotype.Component;

      @Component
      public class JwtParser {}
      """
    );

    ScannedFileMap scannedFiles = ScannedFileMap.builder()
      .rootPath(tempDir)
      .relativeFilePaths(
        List.of("GlobalExceptionHandler.java", "JwtParser.java")
      )
      .build();

    ComponentAnalysisResult result = stage.detect(scannedFiles);

    assertNotNull(result);
    assertEquals(1, result.getTotalComponents());
    assertEquals(
      1,
      result.getComponentCounts().get(ComponentType.COMPONENT.name())
    );
    assertTrue(
      result
        .getComponents()
        .stream()
        .noneMatch(c -> c.getType() == ComponentType.CONTROLLER)
    );
    assertTrue(
      result
        .getComponents()
        .stream()
        .anyMatch(
          c ->
            c.getName().equals("JwtParser") &&
            c.getType() == ComponentType.COMPONENT
        )
    );
  }

  @Test
  void detect_shouldIdentifyMultiLanguageComponentsCorrectly(
    @TempDir Path tempDir
  ) throws IOException {
    Path pyController = tempDir.resolve("routes.py");
    Files.writeString(
      pyController,
      """
      from fastapi import APIRouter
      router = APIRouter()
      @router.get("/users")
      def get_users(): pass
      """
    );

    Path reactComponent = tempDir.resolve("UserCard.tsx");
    Files.writeString(
      reactComponent,
      """
      import React, { useState } from 'react';
      export const UserCard = () => { return <div>User</div>; };
      """
    );

    Path goHandler = tempDir.resolve("user_handler.go");
    Files.writeString(
      goHandler,
      """
      package handler
      import "github.com/gin-gonic/gin"
      func SetupRoutes(r *gin.Engine) {
          r.GET("/ping", func(c *gin.Context) {})
      }
      """
    );

    ScannedFileMap scannedFiles = ScannedFileMap.builder()
      .rootPath(tempDir)
      .relativeFilePaths(
        List.of("routes.py", "UserCard.tsx", "user_handler.go")
      )
      .build();

    ComponentAnalysisResult result = stage.detect(scannedFiles);

    assertNotNull(result);
    assertEquals(3, result.getTotalComponents());
    assertEquals(
      2,
      result.getComponentCounts().get(ComponentType.CONTROLLER.name())
    );
    assertEquals(
      1,
      result.getComponentCounts().get(ComponentType.FRONTEND_COMPONENT.name())
    );
  }
}
