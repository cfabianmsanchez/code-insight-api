## 5. Directivas para la Síntesis Arquitectónica (Ollama)
Con base EXCLUSIVAMENTE en la radiografía factual anterior, redacta el análisis en Markdown con las siguientes secciones.
REGLA DE CONCISIÓN: El usuario ya dispone de pestañas con la lista de componentes, stack y métricas. NO vuelvas a enumerar exhaustivamente todos los archivos ni a repetir las métricas. Limítate a INTERPRETAR los hechos y sintetizar conclusiones.

**1. Resumen Funcional (OBLIGATORIO):**
   Explica en 1 o 2 párrafos concisos cuál es el propósito funcional del repositorio.
   Reglas:
   - Basa la conclusión únicamente en nombres de componentes, tecnologías, endpoints, metadata del manifiesto y evidencias disponibles.
   - No incluyas clasificaciones arquitectónicas ni recomendaciones dentro de esta sección 1 (esas van en las secciones 2, 3 y 4).
   - Si la evidencia es insuficiente para determinar el propósito, indícalo explícitamente.

**2. Clasificación Arquitectónica, Patrones Complementarios y Prácticas:**
   Evalúa el repositorio y estructura la respuesta en 3 subpartes claras:
   
   **a. Estilo Arquitectónico Principal:**
   - Estilo principal inferido (ej. Feature-based Architecture para frontend, Hexagonal Architecture para backend).
   - Nivel de confianza estimado (de 0.0 a 0.95; NUNCA asignes 1.0).
   - Evidencias CONCRETAS de rutas, directorios y componentes que lo sustentan (cita textualmente).
   
   **b. Patrones de Diseño Complementarios:**
   - Identifica patrones secundarios o complementarios detectados (ej. **Facade Pattern**, **Ports & Adapters**, **Repository Pattern**), citando la evidencia específica (clases, interfaces o servicios).
   - NOTA: No trates patrones complementarios (como Facade) como competidores del estilo principal (como Feature-based), sino como patrones de diseño aplicados dentro del mismo.
   
   **c. Prácticas de Framework, Estado y Routing:**
   - Identifica mecanismos y prácticas observadas (ej. **Angular Signals / Reactivity**, **Standalone Components**, **Lazy Loading / Routing**, **Spring Dependency Injection**), citando archivos concretos (ej. `app.routes.ts`, `*.facade.ts`).

**3. Organización de Capas y Estructura por Módulos/Features:**
   Evalúa la jerarquía estructural y el desacoplamiento por módulos/features y capas (ej. App Shell, Shared UI, Features, Data Access, Facade, Models).
   REGLA DE CAPAS: NO confundas componentes UI individuales (como `header` o `loader`) con capas completas de arquitectura; clasifícalos adecuadamente dentro de la capa Shared UI o UI.

**4. Recomendaciones Técnicas (entre 0 y 3 recomendaciones):**
   Emite entre 0 y 3 recomendaciones técnicas de alto impacto (máximo 3, solo si están verdaderamente sustentadas).
   Reglas obligatorias para cada recomendación:
   - DEBE estar respaldada por una evidencia FACTUAL CONCRETA de las secciones anteriores (ej. "0 archivos de prueba implementados a pesar de contar con script 'ng test' en package.json").
   - REGLA DE VALIDACIÓN: Si la evidencia de una recomendación es "No concluyente", esa recomendación es INVÁLIDA y NO debes emitirla.
   - Cita únicamente nombres de archivos reales que figuren en las evidencias. NO inventes archivos inexistentes como `app.module.ts`.
   - NO recomiendes implementar tecnologías o patrones cuya presencia ya esté confirmada en las evidencias (sección 4c/4d).
   - Devuelve las recomendaciones que realmente tengan fundamento (pueden ser 0, 1, 2 o 3). Es preferible devolver 1 o 2 sólidas antes que forzar 3 con datos falsos.
