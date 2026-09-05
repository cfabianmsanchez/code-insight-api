## 5. Directivas para la Síntesis Arquitectónica (Ollama)
Con base EXCLUSIVAMENTE en la radiografía factual anterior, redacta el análisis en Markdown con las siguientes secciones:

**0. Resumen Funcional (OBLIGATORIO):**
   Explica en máximo 3 párrafos cuál parece ser el propósito funcional del repositorio.
   Reglas:
   - Basa la conclusión únicamente en nombres de componentes, tecnologías, endpoints,
     metadata del manifiesto (sección 0) y evidencias disponibles.
   - No inventes funcionalidades no sustentadas por la evidencia.
   - Si la evidencia es insuficiente para determinar el propósito, indícalo explícitamente.

**1. Clasificación Arquitectónica:**
   Determina el estilo o patrón arquitectónico más probable. Incluye:
   - Estilo principal inferido
   - Nivel de confianza estimado (de 0.0 a 1.0)
   - Evidencias CONCRETAS de rutas y componentes que lo sustentan (cita textualmente)
   - Como máximo 2 estilos arquitectónicos alternativos, SOLO si existe evidencia concreta que los sustente.
     Para cada alternativa, cita esa evidencia. Si no hay evidencia suficiente, responde "No concluyente".
   - Aspectos no verificables con la evidencia disponible

**2. Organización de Capas:**
   Evalúa el desacoplamiento aparente según la distribución de componentes, paquetes y relaciones detectadas.

**3. Recomendaciones Técnicas:**
   Emite EXACTAMENTE 3 recomendaciones técnicas de alto impacto.
   Reglas obligatorias para cada recomendación:
   - Debe estar respaldada por una evidencia CONCRETA de las secciones anteriores.
   - NO recomiendes implementar tecnologías, prácticas o patrones cuya presencia ya esté confirmada en las evidencias (sección 4c).
   - La ausencia de una evidencia NO significa ausencia de la práctica; no especules sobre lo que no está en el reporte.
   - Si no puedes formular 3 recomendaciones basadas en evidencia concreta, indica "No concluyente" en lugar de inventar.
   - Devuelve exactamente 3 recomendaciones, no más, no menos.
