# Code Insight API (`code-insight-api`)

Backend desarrollado en **Java 17** y **Spring Boot 3** que implementa **Arquitectura Hexagonal (Puertos y Adaptadores)** y un **Pipeline de Ingeniería Inversa de 7 Etapas** para el análisis estructural, de componentes y síntesis arquitectónica asistida por IA a partir de repositorios GitHub o archivos ZIP.

---

## 🏛️ Arquitectura Hexagonal y Estructura del Proyecto

El proyecto está diseñado bajo una estricta Arquitectura Hexagonal para desacoplar el dominio del negocio de los marcos de trabajo (frameworks) e infraestructura externa:

```text
com.codeinsight.api
├── CodeInsightApplication.java
│
├── domain                                      # Dominio Puro (Java Estándar, sin Spring)
│   ├── exception                               # Excepciones personalizadas del dominio
│   │   ├── DomainException.java
│   │   ├── InvalidRepositoryException.java
│   │   ├── RepositoryFetchException.java
│   │   ├── RepositoryScanningException.java
│   │   └── UnsupportedSourceTypeException.java
│   └── model                                   # Modelos del dominio y registros de datos
│       ├── AnalysisContext.java
│       ├── ArchitectureEvidenceResult.java
│       ├── ComponentAnalysisResult.java
│       ├── ComponentType.java
│       ├── DetectedComponent.java
│       ├── FetchCodeRequest.java
│       ├── RepositoryAnalysisResult.java
│       ├── ScannedFileMap.java
│       ├── SourceType.java
│       └── TechnologyStack.java
│
├── application                                 # Casos de Uso, Pipeline y Puertos
│   ├── model
│   │   └── TempCodeDirectory.java              # Wrapper efímero AutoCloseable de workspace
│   ├── pipeline/stage                          # Etapas del Pipeline de Análisis
│   │   ├── RepositoryLoaderStage.java          # Etapa 1: Carga efímera
│   │   ├── FileScannerStage.java               # Etapa 2: Escaneo de archivos y métricas
│   │   ├── TechnologyDetectorStage.java        # Etapa 3: Detección de stack y manifiestos
│   │   ├── ComponentDetectorStage.java         # Etapa 4: Identificación de componentes
│   │   ├── ArchitectureEvidenceDetectorStage.java # Etapa 5: Recopilación de evidencias
│   │   └── ContextBuilderStage.java            # Etapa 6: Ensamblado del contexto LLM
│   │   └── OllamaAnalysisStage.java            # Etapa 7: Síntesis de arquitectura con IA
│   ├── port
│   │   ├── in
│   │   │   └── AnalyzeRepositoryUseCase.java   # Input Port
│   │   └── out
│   │       ├── ArchitectureSynthesisPort.java  # Output Port para síntesis IA (LLM)
│   │       └── CodeFetcherPort.java            # Output Port para cargadores de código
│   └── service
│       └── AnalyzeRepositoryService.java       # Orquestador del pipeline
│
└── infrastructure                              # Adaptadores e Infraestructura (Spring Boot)
    ├── adapter
    │   ├── in/rest                             # Driving Adapter (REST Controllers & DTOs)
    │   │   ├── AnalyzeRepositoryController.java
    │   │   ├── dto/
    │   │   ├── mapper/
    │   │   └── exception/                      # GlobalExceptionHandler (RFC 7807)
    │   └── out
    │       ├── ai                              # Driven Adapter para Ollama (RestClient)
    │       │   ├── dto/                        # OllamaChatRequest, OllamaChatResponse, OllamaMessage
    │       │   └── OllamaAdapter.java
    │       └── fetcher                         # Driven Adapters (Git & ZIP Fetchers)
    │           ├── GitRepositoryFetcherAdapter.java
    │           └── ZipExtractorFetcherAdapter.java
    └── config
        └── BeanConfiguration.java              # Configuración de Beans de Spring
```

---

## ⚙️ Pipeline de Análisis de 7 Etapas

El servicio `AnalyzeRepositoryService` ejecuta secuencialmente un pipeline determinístico y asistido por IA:

1. **`RepositoryLoaderStage`**: Adquiere el código fuente utilizando el adaptador adecuado (`GitRepositoryFetcherAdapter` o `ZipExtractorFetcherAdapter`) y crea una carpeta efímera (`TempCodeDirectory`) que garantiza su autodestrucción en disco.
2. **`FileScannerStage`**: Recorre efímeramente el árbol de directorios omitiendo carpetas de compilación o ruido (`.git`, `node_modules`, `target`, etc.) y compila métricas de archivos y manifiestos.
3. **`TechnologyDetectorStage`**: Examina manifiestos (`pom.xml`, `build.gradle`, `package.json`, `requirements.txt`) para identificar el lenguaje principal, framework, gestor de dependencias y bibliotecas clave.
4. **`ComponentDetectorStage`**: Analiza clases y anotaciones Java/Spring (`@RestController`, `@Service`, `@Repository`, `@Component`, `@Configuration`) para catalogar los componentes por estereotipo.
5. **`ArchitectureEvidenceDetectorStage`**: Analiza la profundidad de paquetes, distribución de clases y señales léxicas estructurales (como `domain`, `application`, `infrastructure`, `port`, `adapter`) para calcular el nivel de desacoplamiento aparente.
6. **`ContextBuilderStage`**: Construye el prompt estructurado en Markdown con las evidencias determinísticas (Ficha Técnica, Stack, Componentes, Evidencias Estructurales y Directivas de Síntesis) listo para ser consumido por la IA.
7. **`OllamaAnalysisStage`**: Invoca el puerto `ArchitectureSynthesisPort` para comunicarse vía HTTP REST con el modelo Ollama local (`qwen2.5-coder`) y sintetizar la evaluación arquitectónica en Markdown. Incluye un modo de respaldo (fallback) en caso de que Ollama no se encuentre en ejecución.

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

> 💡 **Modelos alternativos soportados:**
> También puedes utilizar otros modelos de IA locales como `llama3.2`, `codellama` o `mistral`:
> ```bash
> ollama pull llama3.2
> ```

---

### 4. Probar la Conexión con Ollama (Opcional)

Puedes verificar que Ollama responde correctamente ejecutando este comando `curl`:

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
- Java 17+
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
  "totalFiles": 40,
  "totalDirectories": 16,
  "technologyStack": {
    "mainLanguage": "Java",
    "mainFramework": "Spring Boot",
    "buildTool": "Maven",
    "databasesDetected": [],
    "keyLibraries": [
      "OpenAPI / Swagger",
      "Lombok"
    ]
  },
  "componentAnalysis": {
    "totalComponents": 10,
    "componentCounts": {
      "CONTROLLER": 1,
      "SERVICE": 1,
      "COMPONENT": 8
    }
  },
  "aiSynthesis": "## Radiografía de Ingeniería Inversa\n...",
  "timestamp": "2026-09-05T11:22:00"
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
