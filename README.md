# Code Insight API (`code-insight-api`) 🚀

Backend desarrollado en **Java 17+** y **Spring Boot 3** que implementa **Arquitectura Hexagonal (Puertos y Adaptadores)** y un **Pipeline de Ingeniería Inversa de 7 Etapas** para el análisis estructural, de componentes, inferencia de relaciones y síntesis arquitectónica asistida por IA local a partir de repositorios GitHub o archivos ZIP.

---

## 🏛️ Arquitectura Hexagonal y Estructura del Proyecto

El proyecto está diseñado bajo una Arquitectura Hexagonal para desacoplar el dominio de negocio de los marcos de trabajo (frameworks) e infraestructura externa:

```text
com.codeinsight.api
├── CodeInsightApplication.java
│
├── domain                                      # Dominio Puro (Java Estándar, sin Spring)
│   ├── exception                               # Excepciones personalizadas del dominio
│   └── model                                   # Modelos de dominio e inferencia
│       ├── AnalysisContext.java                # Prompts formateados
│       ├── ArchitectureEvidenceResult.java     # Evidencias estructurales e inyección
│       ├── ComponentAnalysisResult.java
│       ├── ComponentType.java
│       ├── DetectedComponent.java
│       ├── FetchCodeRequest.java
│       ├── FrontendFramework.java              # ANGULAR, REACT, VUE, NEXT_JS, IONIC, SVELTE, NONE
│       ├── ProjectKind.java                    # BACKEND, FRONTEND, FULLSTACK, MOBILE, LIBRARY, UNKNOWN
│       ├── RepositoryAnalysisResult.java       # Resultado consolidado + aiModelUsed
│       ├── ScannedFileMap.java
│       ├── SourceType.java
│       └── TechnologyStack.java
│
├── application                                 # Casos de Uso, Pipeline y Puertos
│   ├── ai
│   │   └── PromptProvider.java                 # Puerto de plantillas de prompts versionadas
│   ├── model
│   │   └── TempCodeDirectory.java              # Wrapper efímero AutoCloseable
│   ├── pipeline/stage                          # Etapas del Pipeline de Análisis
│   │   ├── RepositoryLoaderStage.java          # Etapa 1: Carga efímera (Git / ZIP)
│   │   ├── FileScannerStage.java               # Etapa 2: Escaneo de archivos y métricas
│   │   ├── TechnologyDetectorStage.java        # Etapa 3: Detección de stack y manifiestos
│   │   ├── ComponentDetectorStage.java         # Etapa 4: Identificación de componentes
│   │   ├── ArchitectureEvidenceDetectorStage.java # Etapa 5: Evidencias Frontend/Backend y Puertos
│   │   ├── ContextBuilderStage.java            # Etapa 6: Ensamblado de contexto factual
│   │   └── AiSynthesisStage.java               # Etapa 7: Síntesis de arquitectura con IA
│   ├── port
│   │   ├── in
│   │   │   └── AnalyzeRepositoryUseCase.java   # Input Port
│   │   └── out
│   │       ├── AiModelManagementPort.java      # Output Port para gestión de modelos de IA
│   │       ├── ArchitectureSynthesisPort.java  # Output Port para síntesis IA (LLM)
│   │       └── CodeFetcherPort.java            # Output Port para cargadores de código
│   └── service
│       └── AnalyzeRepositoryService.java       # Orquestador determinístico del pipeline
│
└── infrastructure                              # Adaptadores e Infraestructura (Spring Boot)
    ├── adapter
    │   ├── in/rest                             # Driving Adapter (REST Controllers & DTOs)
    │   │   ├── AnalyzeRepositoryController.java
    │   │   ├── SystemConfigController.java     # Endpoint para selección dinámica de modelos Ollama
    │   │   ├── dto/                            # Response DTOs
    │   │   ├── mapper/                         # Mapeador dominio ↔ DTO REST
    │   │   └── exception/                      # GlobalExceptionHandler
    │   └── out
    │       ├── ai                              # Driven Adapter para Ollama (RestClient)
    │       │   ├── dto/                        # DTOs de comunicación HTTP
    │       │   └── OllamaAdapter.java          # Cliente REST hacia Ollama API
    │       ├── fetcher                         # Driven Adapters (Git & ZipFile Extractors)
    │       │   ├── GitRepositoryFetcherAdapter.java
    │       │   └── ZipExtractorFetcherAdapter.java
    │       └── prompt                          # Driven Adapter de Plantillas
    │           └── ResourcePromptProvider.java # Carga de plantillas Markdown desde classpath
    └── config
        └── BeanConfiguration.java              # Configuración de Beans de Spring
```

### Plantillas de Prompts Versionadas (`resources/prompts/`)
```text
src/main/resources/
└── prompts/
    ├── system-v1.md           # Reglas estrictas anti-alucinación
    └── analysis-v1.md         # Directivas de síntesis (Resumen Funcional, Arquitectura, Recomendaciones)
```

---

## ⚙️ Pipeline de Análisis de 7 Etapas

El servicio `AnalyzeRepositoryService` ejecuta secuencialmente un pipeline determinístico y asistido por IA:

1. **`RepositoryLoaderStage`**: Adquiere el código fuente utilizando el adaptador adecuado (`GitRepositoryFetcherAdapter` o `ZipExtractorFetcherAdapter`) y crea una carpeta efímera (`TempCodeDirectory`) que garantiza su autodestrucción en disco.
2. **`FileScannerStage`**: Recorre efímeramente el árbol de directorios omitiendo carpetas de compilación o ruido (`.git`, `node_modules`, `target`, `.idea`, etc.) y compila métricas de archivos y manifiestos.
3. **`TechnologyDetectorStage`**: Examina manifiestos (`pom.xml`, `build.gradle`, `package.json`, `requirements.txt`) para identificar el lenguaje principal, framework, gestor de dependencias y bibliotecas clave.
4. **`ComponentDetectorStage`**: Analiza clases y anotaciones Java/Spring (`@RestController`, `@Service`, `@Repository`, `@Component`, `@Configuration`) para catalogar componentes por estereotipo.
5. **`ArchitectureEvidenceDetectorStage`**: 
   - Analiza la distribución de clases en paquetes y palabras clave de arquitectura (`domain`, `application`, `infrastructure`).
   - Identifica relaciones semánticas **Inbound** (Input Port → Implementación de Aplicación) y **Outbound** (Adaptador de Infraestructura → Output Port).
   - Detecta evidencias de ingeniería multiplataforma (archivos de test Java/JS/TS/Python, scripts de test en `package.json`, inyección de dependencias Spring).
6. **`ContextBuilderStage`**: Ensambla el contexto factual combinando las evidencias determinísticas recopiladas con las plantillas versionadas de prompts cargadas a través de `PromptProvider`.
7. **`AiSynthesisStage`**: Invoca el puerto `ArchitectureSynthesisPort` para comunicarse vía HTTP REST con el modelo Ollama local, sintetizar la evaluación arquitectónica en Markdown y extraer automáticamente la sección `0. Resumen Funcional` (`functionalSummary`). Incluye un modo de respaldo (fallback) resiliente.

---

## 📄 Gestión de Prompts Desacoplados (`PromptProvider`)

Los prompts no están incrustados en código Java, sino organizados como artefactos de configuración versionados en Markdown:

* **Configuración en `application.yml`**:
  ```yaml
  ai:
    prompts:
      architecture-version: v1
  ```
* **Ventajas**:
  - Permite hacer evolucionar y versionar las directivas del modelo sin modificar el pipeline de Java.
  - Carga al iniciar la aplicación mediante `@PostConstruct` en `ResourcePromptProvider`.
  - Permite realizar pruebas unitarias y snapshot testing de los prompts sin levantar el servidor Ollama.

---

## 🦙 Configuración e Instalación de Ollama (IA Local)

Para habilitar la **Etapa 7 (Síntesis de Arquitectura con IA)**, el proyecto se conecta mediante HTTP REST con un servidor local de **Ollama**.

### 1. Instalación de Ollama

Descarga e instala Ollama según tu sistema operativo:

* **macOS**:
  ```bash
  brew install ollama
  ```
  *(O descarga el instalador desde [ollama.com](https://ollama.com))*

* **Linux**:
  ```bash
  curl -fsSL https://ollama.com/install.sh | sh
  ```

* **Windows**:
  Descarga el ejecutable desde [ollama.com/download/windows](https://ollama.com/download/windows).

---

### 2. Iniciar el Servidor de Ollama

Ejecuta el servidor en una terminal independiente para que escuche en el puerto predeterminado `http://localhost:11434`:

```bash
ollama serve
```

---

### 3. Descargar el Modelo Recomendado

En otra terminal, descarga el modelo especializado en código recomendado para el proyecto:

```bash
ollama pull qwen2.5-coder
```

---

### 4. Probar la Conexión con Ollama (Opcional)

Verifica que Ollama responde correctamente ejecutando este comando `curl`:

```bash
curl http://localhost:11434/api/generate -d '{
  "model": "qwen2.5-coder",
  "prompt": "¿Qué es la Arquitectura Hexagonal?",
  "stream": false
}'
```

---

### 5. Personalizar Configuración de la API (Opcional)

Por defecto, la API se conecta a `http://localhost:11434` usando el modelo `qwen2.5-coder`. Puedes modificar esta configuración vía variables de entorno al iniciar la aplicación:

```bash
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=qwen2.5-coder

mvn spring-boot:run
```

---

### 🛡️ Modo de Respaldo (Fallback Resiliente)

Si Ollama **no está instalado o no se encuentra en ejecución**, la API de `code-insight-api` **no fallará ni responderá con errores HTTP 500**. El pipeline completará exitosamente las etapas determinísticas 1 a 6 y devolverá en la propiedad `aiSynthesis` un reporte de instrucciones informando cómo activar el servicio local.

---

## 🚀 Cómo Ejecutar la Aplicación

### Requisitos Previos
- Java 17+ (probado en Java 17 y 26)
- Apache Maven 3.8+
- (Opcional para IA) Ollama iniciado en `http://localhost:11434` (`ollama serve`) con el modelo `qwen2.5-coder`.

### Compilación y Suite de Pruebas
```bash
mvn clean test
```

### Ejecutar Servidor Local
```bash
mvn spring-boot:run
```
El servidor iniciará en: `http://localhost:8080`

---

## 📡 API REST

### 1. Analizar Repositorio de GitHub (`POST /api/v1/analyses/github`)

**Request Payload:**
```json
{
  "projectKey": "code-insight-api",
  "repoUrl": "https://github.com/cfabianmsanchez/code-insight-api"
}
```

**Response Payload (`200 OK`):**
```json
{
  "projectKey": "code-insight-api",
  "sourceType": "GITHUB_REPO",
  "totalFiles": 42,
  "totalDirectories": 16,
  "functionalSummary": "El proyecto code-insight-api es una API REST en Java con Spring Boot diseñada para realizar análisis de ingeniería inversa de repositorios de software. Recibe código fuente vía Git o ZIP, ejecuta un pipeline de 7 etapas para detectar tecnologías y componentes, e integra Ollama para generar informes de síntesis de arquitectura.",
  "technologyStack": {
    "mainLanguage": "Java",
    "mainFramework": "Spring Boot",
    "buildTool": "Maven",
    "databasesDetected": [],
    "keyLibraries": [
      "OpenAPI / Swagger"
    ]
  },
  "componentAnalysis": {
    "totalComponents": 11,
    "componentCounts": {
      "CONTROLLER": 1,
      "SERVICE": 1,
      "COMPONENT": 9
    }
  },
  "architectureEvidence": {
    "totalStructuralPaths": 16,
    "maxPathDepth": 8,
    "detectedKeywords": [
      "application",
      "domain",
      "infrastructure",
      "adapter",
      "port"
    ],
    "inboundPortImplementations": {
      "AnalyzeRepositoryUseCase": "AnalyzeRepositoryService"
    },
    "outboundAdapterImplementations": {
      "OllamaAdapter": "ArchitectureSynthesisPort",
      "GitRepositoryFetcherAdapter": "CodeFetcherPort",
      "ZipExtractorFetcherAdapter": "CodeFetcherPort"
    }
  },
  "aiSynthesis": "## 0. Resumen Funcional\n...\n## 1. Clasificación Arquitectónica\n...\n## 2. Organización de Capas\n...\n## 3. Recomendaciones Técnicas\n...",
  "timestamp": "2026-09-05T13:30:00"
}
```

### 2. Analizar Archivo ZIP (`POST /api/v1/analyses/zip`)

**Form Data:**
- `file`: Archivo `.zip` con el proyecto de código fuente.
- `projectKey` (opcional): Identificador del proyecto.

---

## 📄 Documentación Swagger / OpenAPI

Puedes probar los endpoints de forma interactiva en la interfaz Swagger UI:
👉 `http://localhost:8080/swagger-ui.html`
