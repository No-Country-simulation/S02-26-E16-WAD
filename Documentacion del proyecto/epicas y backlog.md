# WVP – Épicas y Backlog Técnico (MVP)

---

## 🧭 ÉPICAS DEL MVP

### ÉPICA 1 – Ingesta de Video Horizontal

**Objetivo:**  
Permitir que los usuarios suban videos horizontales de forma simple, segura y confiable para su posterior procesamiento.

**Historias incluidas**
- HU-01 – Subir video horizontal
- HU-T01 – Procesamiento asincrónico (parcial)

---

### ÉPICA 2 – Adaptación Automática Horizontal → Vertical

**Objetivo:**  
Transformar videos 16:9 en formato vertical 9:16 sin perder información visual relevante.

**Historias incluidas**
- HU-02 – Reencuadre automático inteligente
- HU-06 – Preservación de calidad visual

---

### ÉPICA 3 – Generación Automática de Shorts

**Objetivo:**  
Detectar segmentos relevantes del video original y generar múltiples shorts verticales automáticamente.

**Historias incluidas**
- HU-03 – Generación automática de shorts
- HU-04 – Selección de shorts generados

---

### ÉPICA 4 – Exportación y Distribución

**Objetivo:**  
Permitir la descarga de shorts listos para publicar en redes sociales sin reprocesamiento adicional.

**Historias incluidas**
- HU-05 – Exportación lista para redes sociales

---

### ÉPICA 5 – Experiencia de Usuario y Flujo Simple

**Objetivo:**  
Ofrecer una experiencia clara, rápida y sin fricción para usuarios no técnicos.

**Historias incluidas**
- HU-07 – Flujo simple y rápido

---

### ÉPICA 6 – Plataforma y Escalabilidad Básica

**Objetivo:**  
Garantizar que el sistema pueda escalar de manera mínima y procesar múltiples videos en paralelo.

**Historias incluidas**
- HU-T01 – Procesamiento asincrónico
- HU-T02 – Escalabilidad básica

---

---

## 🛠️ BACKLOG TÉCNICO (MVP)

### 📦 Backend / Infraestructura

- Definir límites de tamaño y duración de video.
- Implementar endpoint de upload con soporte multipart.
- Almacenamiento de videos originales (ej: object storage).
- Almacenamiento de videos procesados (shorts).
- Gestión de estados del procesamiento:
  - Uploaded
  - Processing
  - Completed
  - Failed
- Procesamiento asincrónico con workers.
- Sistema de colas para tareas de video.
- Manejo de errores y reintentos.
- Limpieza automática de archivos temporales.

---

### 🎞️ Procesamiento de Video

- Pipeline de conversión 16:9 → 9:16.
- Detección de área relevante (centro, movimiento, caras).
- Recorte dinámico por frame o segmento.
- Normalización de resolución (1080x1920).
- Control de bitrate y calidad de salida.
- Generación automática de múltiples clips.
- Configuración de duración de shorts (15s / 30s / 60s).
- Naming automático de shorts.

---

### 🧠 Lógica de Dominio

- Modelo de dominio:
  - Video
  - Short
  - ProcessingJob
- Reglas de negocio:
  - Un video puede generar N shorts.
  - Un short pertenece a un solo video.
  - No exportar shorts fallidos.
- Validaciones:
  - Formato
  - Resolución mínima
  - Duración máxima

---

### 🖥️ Frontend / UX

- Pantalla de upload de video.
- Indicador de progreso de carga.
- Vista de estado de procesamiento.
- Preview de shorts generados.
- Selector de shorts para exportar.
- Descarga individual o múltiple de shorts.
- Mensajes claros de error y estado.
- Flujo UX en 4 pasos:
  1. Upload
  2. Processing
  3. Preview
  4. Download

---

### 🔐 Seguridad y Control Básico

- Validación de archivos en backend.
- Protección contra uploads maliciosos.
- Límites de uso por usuario (rate / volumen).
- Manejo básico de permisos (videos propios).

---

### 📊 Observabilidad y Métricas

- Logs de procesamiento de video.
- Métricas:
  - Tiempo promedio de procesamiento.
  - Shorts generados por video.
  - Fallos por tipo.
- Alertas básicas por fallos críticos.

---

### 🧪 Testing y Calidad

- Tests unitarios de reglas de negocio.
- Tests de pipeline de video.
- Tests de fallos de procesamiento.
- Validación de outputs (resolución, formato).
- Smoke tests del flujo completo.

---

## 🧠 Notas de Arquitectura (MVP)

- Procesamiento desacoplado del frontend.
- Workers de video escalables horizontalmente.
- Storage desacoplado del procesamiento.
- Diseño preparado para:
  - IA avanzada en V2
  - Detección semántica de momentos
  - Subtítulos automáticos

---
