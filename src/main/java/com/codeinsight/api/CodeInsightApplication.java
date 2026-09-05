package com.codeinsight.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación Spring Boot para Code Insight API.
 * 
 * Inicia el servidor backend y el motor de análisis de ingeniería inversa de repositorios.
 */
@SpringBootApplication
public class CodeInsightApplication {

    /**
     * Método principal que arranca la aplicación Spring Boot.
     *
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        SpringApplication.run(CodeInsightApplication.class, args);
    }
}
