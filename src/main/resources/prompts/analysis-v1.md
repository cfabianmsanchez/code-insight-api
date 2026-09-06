## 5. Directivas para la Síntesis Arquitectónica Híbrida (Ollama)
Utiliza la radiografía factual provista como tu ANCLA INQUEBRANTABLE DE VERDAD (Ground Truth) y redacta el análisis en Markdown con las siguientes secciones.
REGLA DE CONCISIÓN E INTERPRETACIÓN: El usuario ya dispone de pestañas con métricas y componentes. NO te limites a enumerar datos. Interpreta los hechos, evalúa la cohesión arquitectónica y sintetiza conclusiones de valor estratégico.

**1. Resumen Funcional (OBLIGATORIO):**
   Explica en 1 o 2 párrafos concisos cuál es el propósito funcional del repositorio.
   Reglas:
   - Deduce el dominio de negocio analizando la combinación de componentes, modelos, endpoints, librerías y metadatos del manifiesto.
   - No incluyas clasificaciones arquitectónicas ni recomendaciones dentro de esta sección 1 (esas van en las secciones 2, 3 y 4).
   - Si la evidencia es insuficiente para determinar el propósito, indícalo explícitamente.

**2. Clasificación Arquitectónica, Patrones Complementarios y Prácticas:**
   Evalúa el repositorio y estructura la respuesta en 3 subpartes claras:
   
   **a. Estilo Arquitectónico Principal:**
   - Estilo principal inferido (ej. Feature-based Architecture para frontend, Hexagonal Architecture, Layered, Clean Architecture para backend).
   - Nivel de confianza estimado (de 0.0 a 0.95; NUNCA asignes 1.0).
   - Evidencias CONCRETAS de rutas, directorios y componentes que lo sustentan (cita textualmente los archivos reales).
   
   **b. Patrones de Diseño Complementarios:**
   - Identifica e interpreta patrones secundarios o complementarios detectados o deducidos (ej. **Facade Pattern**, **Ports & Adapters**, **Repository Pattern**, **Factory/Strategy**, **CQRS / Event-Driven**), citando la evidencia específica.
   - NOTA: No trates patrones complementarios como competidores del estilo principal, sino como patrones aplicados dentro del mismo.
   
   **c. Prácticas de Framework, Estado y Routing:**
   - Identifica mecanismos y buenas prácticas observadas (ej. **Angular Signals / Reactivity**, **Standalone Components**, **Lazy Loading / Routing**, **Spring Dependency Injection**), citando archivos concretos.

**3. Organización de Capas y Estructura por Módulos/Features:**
   Evalúa la jerarquía estructural y el nivel de desacoplamiento entre módulos/features y capas (ej. Domain, Application, Infrastructure, Shared UI, Features, Data Access).
   Sintetiza la cohesión de las capas y destaca fortalezas o debilidades en la organización física de los archivos.

**4. Recomendaciones Técnicas (entre 0 y 3 recomendaciones):**
   Emite entre 0 y 3 recomendaciones técnicas de alto impacto verdaderamente sustentadas en la evidencia.
   Reglas obligatorias para cada recomendación:
   - DEBE estar respaldada por una evidencia FACTUAL CONCRETA observada (ej. "0 archivos de prueba implementados a pesar de contar con script 'ng test' en package.json").
   - REGLA DE VALIDACIÓN: Si la evidencia de una recomendación es "No concluyente", esa recomendación es INVÁLIDA y NO debes emitirla.
   - Cita únicamente nombres de archivos reales que figuren en las evidencias. NO inventes archivos inexistentes.
   - NO recomiendes implementar tecnologías o patrones cuya presencia ya esté confirmada en las evidencias.
   - Devuelve únicamente recomendaciones con verdadero fundamento real (pueden ser 0, 1, 2 o 3). Es preferible devolver 1 o 2 sólidas antes que forzar 3 con datos falsos.
