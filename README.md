# Code Insight API (`code-insight-api`)

Backend desarrollado con **Spring Boot 3 (Java 17)** que utiliza **Arquitectura Hexagonal (Ports & Adapters)** y el **Patrón de Diseño Strategy** para ofrecer métricas e inspección de código a través de una API REST lista para integrarse con un Frontend.

---

## 🏛️ Arquitectura del Proyecto

El proyecto está diseñado bajo los principios de la Arquitectura Hexagonal para desacoplar el dominio del negocio de la infraestructura y frameworks externos.

```
com.codeinsight.api
├── CodeInsightApplication.java
│
├── domain                              # Dominio puro (Java estándar, sin Spring)
│   ├── model                           # Entidades y objetos de valor (AnalysisReport, CodeAnalysisRequest)
│   ├── exception                       # Excepciones del dominio
│   └── strategy                        # PATRÓN STRATEGY
│       ├── CodeAnalysisStrategy.java   # Interfaz de la Estrategia
│       ├── JavaCodeAnalysisStrategy.java
│       ├── PythonCodeAnalysisStrategy.java
│       └── CodeAnalysisStrategyFactory.java # Context/Factory para selección dinámica
│
├── application                         # Casos de uso y Puertos
│   ├── port
│   │   ├── in                          # Input Ports (Casos de uso para adaptadores primarios como REST)
│   │   │   └── AnalyzeCodeUseCase.java
│   │   └── out                         # Output Ports (Interfaces para persistencia, external APIs)
│   │       └── SaveAnalysisReportPort.java
│   └── service                         # Implementación de Casos de Uso
│       └── AnalyzeCodeService.java
│
└── infrastructure                      # Adaptadores e Infraestructura (Spring Boot)
    ├── adapter
    │   ├── in/rest                     # Adaptador REST (Driving Adapter)
    │   │   ├── CodeAnalysisController.java
    │   │   ├── dto/                    # AnalysisRequestDto, AnalysisResponseDto
    │   │   ├── mapper/                 # AnalysisRestMapper
    │   │   └── exception/              # GlobalExceptionHandler
    │   └── out/persistence             # Adaptador de salida (Driven Adapter)
    │       └── InMemoryReportPersistenceAdapter.java
    └── config                          # Configuración de Beans de Spring y Swagger
        └── BeanConfiguration.java
```

---

## 💡 Patrón Strategy Aplicado

El **Patrón Strategy** permite extender dinámicamente nuevos lenguajes o motores de análisis sin alterar la lógica existente (Principio Open/Closed):

1. **`CodeAnalysisStrategy`**: Define el contrato genérico para cualquier algoritmo de análisis.
2. **`JavaCodeAnalysisStrategy` / `PythonCodeAnalysisStrategy`**: Implementaciones concretas para cada lenguaje.
3. **`CodeAnalysisStrategyFactory`**: Resuelve dinámicamente la estrategia adecuada según la propiedad `type` del request (`JAVA`, `PYTHON`, etc.).

---

## 🚀 Cómo Ejecutar la Aplicación

### Requisitos Previos
- Java 17+
- Apache Maven 3.8+

### Compilación y Tests
```bash
mvn clean test
```

### Ejecutar Servidor Local
```bash
mvn spring-boot:run
```
El servidor iniciará en: `http://localhost:8080`

---

## 📡 API REST - Integración con Frontend

### 1. Analizar Código (`POST /api/v1/analysis`)

**Request Payload:**
```json
{
  "projectKey": "frontend-dashboard",
  "sourceCode": "public class UserNotificationService {\n  public void sendEmail() {\n    System.out.println(\"Sending email...\");\n  }\n}",
  "type": "JAVA",
  "metadata": {
    "author": "dev-user"
  }
}
```

**Response Payload (`201 Created`):**
```json
{
  "id": "c1f70d2c-88e4-4d8a-9876-efb0451a998e",
  "projectKey": "frontend-dashboard",
  "analysisType": "JAVA",
  "linesOfCode": 5,
  "cyclomaticComplexity": 2,
  "estimatedBugs": 0,
  "securityVulnerabilities": 1,
  "summary": "Java Code Analysis completed successfully.",
  "recommendations": [
    "Consider using SLF4J logger instead of System.out.println",
    "Ensure classes have proper unit test coverage",
    "Keep method length under 30 lines of code"
  ],
  "metrics": {
    "classesDetected": 1,
    "methodsDetected": 1,
    "languageVersion": "Java 17+"
  },
  "timestamp": "2026-09-04T03:25:00"
}
```

---

## 📄 Documentación Swagger / OpenAPI

Puedes probar los endpoints de forma interactiva en la interfaz Swagger UI:
👉 `http://localhost:8080/swagger-ui.html`
