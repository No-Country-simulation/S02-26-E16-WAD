# Video Processor API

API para convertir videos horizontales a formato vertical 9:16 para TikTok, Instagram Reels y YouTube Shorts.

Detecta rostros automáticamente, aplica smart crop con estabilización cinematográfica y sube el resultado a Cloudinary.

---

## Requisitos

- Python 3.11+
- FFmpeg instalado y en el PATH
- Cuenta de Cloudinary (plan gratuito suficiente para desarrollo)

---

## Instalación

```bash
git clone <repo>
cd video-processor-api

python -m venv .venv
source .venv/bin/activate      # Linux/macOS
# .venv\Scripts\activate       # Windows

pip install -r requirements.txt
```

Copiar y completar las variables de entorno:

```bash
cp .env.example .env
```

---

## Variables de entorno

| Variable | Requerida | Descripción |
|---|---|---|
| `CLOUDINARY_CLOUD_NAME` | ✅ | Nombre del cloud en Cloudinary |
| `CLOUDINARY_API_KEY` | ✅ | API key de Cloudinary |
| `CLOUDINARY_API_SECRET` | ✅ | API secret de Cloudinary |
| `SERVICE_JWT_SECRET` | ✅ | Secret HS256 compartido con Spring Boot |
| `SERVICE_API_KEY` | ✅ | API key interna para webhooks |
| `SPRING_BOOT_WEBHOOK_URL` | ⚠️ | URL para notificaciones finales de jobs |
| `SPRING_BOOT_PROGRESS_WEBHOOK_URL` | ⚠️ | URL para notificaciones de progreso en tiempo real |
| `CLOUDINARY_TEMP_DIR` | — | Directorio temporal (default: `/tmp/video_processing`) |
| `HOST` | — | Host del servidor (default: `0.0.0.0`) |
| `PORT` | — | Puerto (default: `8000`) |
| `RELOAD` | — | Hot reload para desarrollo (default: `true`) |
| `LOG_LEVEL` | — | Nivel de logging (default: `INFO`) |

Las variables marcadas con ⚠️ son opcionales pero desactivan esa funcionalidad si no se configuran.

---

## Arrancar el servidor

```bash
python main.py
```

O directamente con uvicorn:

```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Documentación interactiva disponible en `http://localhost:8000/docs`.

---

## Endpoints

### `POST /api/video/process`

Inicia el procesamiento de un video. Devuelve un `job_id` para consultar el estado.

**Modos de procesamiento:**

- `vertical` — Convierte el video completo a 9:16 con smart crop.
- `short_auto` — Selecciona automáticamente el mejor segmento y lo convierte.
- `short_manual` — Usa el segmento indicado por el usuario.

**Ejemplo — vertical:**
```json
{
  "processing_mode": "vertical",
  "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
  "platform": "tiktok",
  "background_mode": "smart_crop",
  "quality": "high"
}
```

**Ejemplo — short automático:**
```json
{
  "processing_mode": "short_auto",
  "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
  "platform": "instagram",
  "background_mode": "blurred",
  "quality": "normal",
  "short_auto_duration": 30
}
```

**Ejemplo — short manual:**
```json
{
  "processing_mode": "short_manual",
  "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
  "platform": "youtube_shorts",
  "background_mode": "smart_crop",
  "quality": "high",
  "short_options": {
    "start_time": 45.0,
    "duration": 20
  }
}
```

---

### `GET /api/video/status/{job_id}`

Devuelve el estado actual del job, incluyendo fase, progreso (0–100) y ETA estimada.

---

### `GET /api/video/download/{job_id}`

Devuelve la URL de Cloudinary del video procesado (solo disponible si el job está completado).

---

### `POST /api/video/jobs/{job_id}/cancel`

Cancela un job en proceso. Si ya completó el procesamiento pero no subió el resultado, no se sube.

---

### `GET /api/video/jobs`

Lista todos los jobs del token autenticado.

---

### `DELETE /api/video/jobs/{job_id}`

Elimina un job completado o fallido del registro.

---

## Arquitectura

```
main.py
├── routers/
│   └── video.py              # Endpoints, jobs_db, autenticación
├── services/
│   ├── video_service.py      # Orquestación del flujo completo
│   ├── webhook_service.py    # Notificaciones a Spring Boot
│   ├── strategies.py         # Patrón Strategy por modo de procesamiento
│   ├── segment_selector.py   # Selección inteligente de segmento (short_auto)
│   └── segment_cutter.py     # Extracción de segmento con FFmpeg
├── processing/
│   ├── video_processor_enhanced.py   # Pipeline principal de procesamiento
│   ├── face_detector_enhanced.py     # Detección híbrida de rostros (MediaPipe + fallback)
│   ├── stabilization_enhanced.py     # Estabilización cinematográfica adaptativa
│   └── ffmpeg_ultra.py               # Encoding y filtros FFmpeg
├── storage/
│   └── cloudinary_service.py  # Upload, download y compresión automática
├── models/
│   └── schemas.py             # Modelos Pydantic con discriminated unions
├── core/
│   ├── config.py              # Configuración global mutable por job
│   ├── auth.py                # Validación JWT HS256
│   ├── middleware.py          # Request ID, logging, CORS
│   ├── error_handler.py       # Manejo centralizado de errores y retry
│   └── exceptions.py         # Excepciones tipadas del dominio
└── utils/
    ├── validators.py          # Validaciones de URL, tamaño y duración
    ├── progress_tracker.py    # Tracking de fases con throttled webhooks
    ├── cancellation_manager.py # Cancelación thread-safe de jobs
    ├── optimization.py        # Hardware acceleration, cache, métricas
    └── preview_generator.py   # Thumbnails y preview clips
```

---

## Parámetros avanzados

El campo `advanced_options` en el request permite ajustar el comportamiento del procesamiento:

| Campo | Tipo | Descripción |
|---|---|---|
| `headroom_ratio` | float (0.05–0.40) | Espacio sobre la cabeza en el encuadre |
| `smoothing_strength` | float (0.50–0.99) | Suavizado del movimiento de cámara |
| `max_camera_speed` | int (5–100) | Velocidad máxima de desplazamiento en px/frame |
| `apply_sharpening` | bool | Aplicar filtro de nitidez al video final |
| `use_rule_of_thirds` | bool | Componer usando la regla de los tercios |
| `edge_padding` | int (0–50) | Margen mínimo a los bordes en píxeles |

---

## Notas de producción

- La variable `RELOAD=false` debe estar configurada en producción.
- El directorio temporal (`CLOUDINARY_TEMP_DIR`) requiere espacio suficiente: se recomienda al menos 3× el tamaño máximo de video esperado.
- Si FFmpeg detecta una GPU NVIDIA o Intel QSV, el encoding se acelera automáticamente con `h264_nvenc` o `h264_qsv`.
- Los videos que excedan 95 MB se comprimen automáticamente antes de subirse a Cloudinary.
