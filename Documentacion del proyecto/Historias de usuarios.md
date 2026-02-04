# MVP – WVP (Wide Video to Vertical Platform)

## 📌 Contexto / Problema

Hoy, para tener visibilidad, autoridad y generar networking en redes sociales, es clave publicar tanto **videos horizontales** como **shorts verticales**.

El problema es que:

- Adaptar videos horizontales a vertical suele cortar contenido importante.
- La generación de shorts requiere edición manual.
- Para startups, pymes y emprendedores, **no es core del negocio** invertir tiempo en edición de video.

Este MVP busca **automatizar la reutilización de contenido**, permitiendo:

- Mantener la información clave al pasar de horizontal a vertical.
- Generar shorts automáticamente.
- Reducir drásticamente el tiempo y esfuerzo de publicación.

---

## 🎯 Alcance del MVP

### El MVP NO:

- No es un editor de video profesional.
- No compite con herramientas como Premiere o CapCut.
- No requiere edición manual avanzada.

### El MVP SÍ:

- Convierte videos horizontales (16:9) a vertical (9:16).
- Preserva elementos importantes del contenido.
- Genera shorts automáticamente listos para redes sociales.

---

## 🧱 Historias de Usuario

### HU-01 – Subir video horizontal

**Como** creador de contenido / emprendedor  
**Quiero** subir un video horizontal (16:9)  
**Para** generar automáticamente versiones verticales reutilizables.

**Criterios de aceptación**

- Acepta formatos comunes (mp4, mov).
- Resolución mínima soportada: 720p.
- Feedback visual del estado de carga.
- Duración máxima definida para el MVP (ej: 60–90 min).

---

### HU-02 – Reencuadre automático inteligente

**Como** usuario  
**Quiero** que el sistema adapte el video horizontal a vertical (9:16)  
**Para** no perder personas, textos o elementos importantes.

**Criterios de aceptación**

- Mantiene visible:
  - Caras
  - Hablante principal
  - Zonas relevantes de acción o movimiento
- No requiere edición manual.
- Permite previsualizar el resultado antes de exportar.

---

### HU-03 – Generación automática de shorts

**Como** emprendedor o marketer  
**Quiero** que el sistema detecte y genere shorts automáticamente  
**Para** ahorrar tiempo de edición y publicar con mayor frecuencia.

**Criterios de aceptación**

- Duración configurable (ej: 15s, 30s, 60s).
- Genera múltiples shorts desde un solo video.
- Formato vertical (9:16).
- Nombres automáticos (`short_01`, `short_02`, etc.).

---

### HU-04 – Selección de shorts generados

**Como** usuario  
**Quiero** ver un listado de los shorts generados y elegir cuáles exportar  
**Para** evitar descargar contenido irrelevante.

**Criterios de aceptación**

- Preview individual de cada short.
- Opción de seleccionar / deseleccionar.
- Información visible:
  - Duración
  - Timestamp de origen en el video original.

---

### HU-05 – Exportación lista para redes sociales

**Como** creador de contenido  
**Quiero** descargar los shorts ya optimizados para redes  
**Para** publicarlos directamente sin reprocesar.

**Criterios de aceptación**

- Formato 9:16.
- Resolución estándar: 1080x1920.
- Compatible con:
  - Instagram Reels
  - TikTok
  - YouTube Shorts

---

### HU-06 – Preservación de calidad visual

**Como** usuario  
**Quiero** que los videos generados mantengan buena calidad visual  
**Para** cuidar la imagen profesional de mi marca.

**Criterios de aceptación**

- Bitrate adecuado.
- Sin deformaciones ni estiramientos.
- Sin cortes abruptos o artefactos visibles.

---

### HU-07 – Flujo simple y rápido

**Como** usuario no técnico  
**Quiero** un flujo simple en pocos pasos  
**Para** generar shorts sin conocimientos de edición.

**Criterios de aceptación**

- Flujo claro:
  1. Subir video
  2. Procesar
  3. Ver shorts
  4. Descargar
- Sin configuraciones avanzadas obligatorias.
- Tiempo de procesamiento comunicado al usuario.

---

## ⚙️ Historias Técnicas (No Funcionales)

### HU-T01 – Procesamiento asincrónico

**Como** sistema  
**Debo** procesar los videos en background  
**Para** no bloquear la experiencia del usuario.

---

### HU-T02 – Escalabilidad básica

**Como** sistema  
**Debo** soportar múltiples procesamientos simultáneos  
**Para** permitir el uso concurrente por varios usuarios.

---

## 📊 Métricas mínimas del MVP

- % de videos procesados exitosamente.
- Tiempo promedio de procesamiento.
- Cantidad de shorts generados por video.
- Tasa de descarga de shorts.

---
