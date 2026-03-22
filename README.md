<div align="center">

<img src="https://github.com/user-attachments/assets/4602601c-e3ed-4c0d-b847-24fa5a05d5d6" alt="Elevideo Banner" width="30%" />

**Plataforma inteligente de procesamiento de video**  
Convierte videos horizontales a formato vertical 9:16 con detección de rostros, reencuadre inteligente y múltiples modos de procesamiento para TikTok, Instagram Reels y YouTube Shorts.

[![Frontend](https://img.shields.io/badge/Frontend-Vercel-black?style=for-the-badge&logo=vercel)](https://elevideo.vercel.app)
[![Backend](https://img.shields.io/badge/Backend%20API-Render-46E3B7?style=for-the-badge&logo=render)](https://elevideo-ec.onrender.com/swagger-ui/index.html)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-6DB33F?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-JS-61DAFB?style=for-the-badge&logo=react)](https://react.dev/)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=for-the-badge&logo=python)](https://www.python.org/)

</div>

---

## 📋 Tabla de contenidos

- [¿Qué es Elevideo?](#-qué-es-elevideo)
- [Arquitectura del sistema](#-arquitectura-del-sistema)
- [Demos y deploys](#-demos-y-deploys)
- [Estructura del Repositorio](#-estructura-del-repositorio)
- [Stack tecnológico](#-stack-tecnológico)
- [El equipo](#-el-equipo)

---

## 🎬 ¿Qué es Elevideo?

Elevideo es una plataforma web que permite a creadores de contenido transformar sus videos horizontales en clips verticales listos para publicar en redes sociales, sin necesidad de editar manualmente.

El sistema detecta automáticamente los rostros en el video, calcula el encuadre óptimo en cada fotograma y aplica estabilización cinematográfica para generar un resultado fluido y profesional.

### Funcionalidades principales

- 📁 **Proyectos** — Organiza tus videos en proyectos independientes
- ⬆️ **Subida de videos** — Upload directo a Cloudinary con metadatos automáticos
- 🎯 **Tres modos de procesamiento:**
    - `vertical` — Convierte el video completo a 9:16
    - `short_auto` — Selecciona automáticamente el mejor segmento
    - `short_manual` — Tú decides el segmento de inicio y duración
- 📊 **Seguimiento en tiempo real** — Consulta el progreso de cada job mediante polling
- 🎨 **Fondos personalizables** — Smart crop, fondo difuminado o fondo negro
- 📥 **Galería de resultados** — Accede a todos tus videos procesados con sus URLs de descarga
- 🔔 **Notificaciones por email** — Recibe un aviso cuando tu video está listo

---

## 🏗 Arquitectura del sistema

Elevideo está compuesto por tres servicios independientes que se comunican entre sí:

```
┌─────────────────────────────────────────────────────────┐
│                     Usuario / Browser                   │
└───────────────────────────┬─────────────────────────────┘
                            │ HTTPS
                            ▼
┌─────────────────────────────────────────────────────────┐
│              Frontend  (React + Tailwind CSS)           │
│                   elevideo.vercel.app                   │
└───────────────────────────┬─────────────────────────────┘
                            │ REST API + JWT
                            ▼
┌─────────────────────────────────────────────────────────┐
│           Backend Spring Boot  (Monolito Modular)       │
│             elevideo-ec.onrender.com                    │
│                                                         │
│   auth │ user │ project │ video │ processing │ notif.   │
└──────────────┬───────────────────────▲──────────────────┘
               │ HTTP + JWT delegado   │ Webhooks
               │                       │ (X-Service-Key)
               ▼                       │
┌──────────────────────────┐           │
│   Microservicio Python   │───────────┘
│   FastAPI + MediaPipe    │
│     (solo local)         │
└──────────────────────────┘
```

### Flujo de procesamiento

1. El usuario sube un video desde el frontend → se almacena en **Cloudinary**
2. Crea un job de procesamiento → **Spring Boot** registra el job y llama al microservicio Python
3. **Python** descarga el video, aplica detección de rostros con MediaPipe, estabilización cinematográfica y encoding con FFmpeg
4. El resultado se sube a Cloudinary y Python notifica a Spring Boot vía **webhook**
5. Spring Boot actualiza el job y guarda la rendition con las URLs de descarga
6. El usuario recibe un notificacion en su navegagador y puede ver el resultado en su galería de videos procesados

---

## 🌐 Demos y deploys

| Servicio | URL | Estado |
|---|---|---|
| 🎨 Frontend | [elevideo.vercel.app](https://elevideo.vercel.app) | ✅ Desplegado |
| ⚙️ Backend API (Swagger) | [elevideo-ec.onrender.com/swagger-ui/index.html](https://elevideo-ec.onrender.com/swagger-ui/index.html) | ✅ Desplegado |
| 🐍 Microservicio Python | — | ⚠️ Solo local |

> **¿Por qué el microservicio Python no está desplegado?**  
> El procesamiento de video con MediaPipe, OpenCV y FFmpeg es una tarea intensiva en CPU y memoria RAM. Los planes gratuitos de plataformas como Render o Railway no ofrecen los recursos suficientes para ejecutarlo de forma estable. Para usarlo es necesario correrlo localmente o en un servidor propio con recursos adecuados.

---

## 📦 Estructura del repositorio

```
elevideo/
├── backend/          ← API REST Spring Boot (monolito modular)
├── frontend/         ← Interfaz de usuario React.js
└── video-processor/  ← Microservicio de procesamiento Python FastAPI
```

Cada servicio tiene su propio README con instrucciones detalladas de instalación y configuración:

- 📖 [README — Backend](./backend/README.md)
- 📖 [README — Frontend](./Frontend/readme.md)
- 📖 [README — Microservicio Python](./elevideo-processor/README.md)

---

## 🛠 Stack tecnológico

### Backend — Spring Boot
| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.5.11 | Framework REST |
| Spring Modulith | 2.0.3 | Arquitectura de monolito modular |
| Spring Security + JJWT | 0.12.5 | Autenticación JWT |
| PostgreSQL | — | Base de datos relacional |
| Spring Data JPA | — | Persistencia |
| MapStruct | 1.6.3 | Mapeo de objetos |
| Cloudinary SDK | 1.39.0 | Almacenamiento de video |
| Thymeleaf + Resend | — | Emails transaccionales |
| SpringDoc OpenAPI | 2.8.15 | Documentación Swagger |

### Frontend — React
| Tecnología | Uso |
|---|---|
| React.js (JS ES6+) | Interfaz de usuario reactiva |
| Tailwind CSS | Estilos utilitarios |
| CRACO | Configuración personalizada sin eject |
| PostCSS | Procesamiento de CSS |

### Microservicio Python — FastAPI
| Tecnología | Uso |
|---|---|
| Python 3.11+ | Lenguaje principal |
| FastAPI | Framework REST asíncrono |
| MediaPipe | Detección de rostros |
| OpenCV | Procesamiento de fotogramas |
| FFmpeg | Encoding y filtros de video |
| NumPy | Cálculos de estabilización |
| Cloudinary SDK | Subida del video procesado |

---

## 👥 El Equipo

Este proyecto fue desarrollado por un equipo multidisciplinar en el marco de una simulación laboral de [No Country](https://www.nocountry.tech/).

| Rol | Nombre | LinkedIn | GitHub |
| --- | ------ | -------- | ------ |
| 📋 **Project Manager** | David H. Caycedo Blum | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/davidcoachdev) | [![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/davidcoachdev) |
| 🎨 **Frontend Developer** | Jhorman Nieto | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/jhormandev) | [![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/jhorman9) |
| 🔧 **Backend Developer** | Edgar Ulises Camberos Arreola | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/edgar-camberos-8a66052bb) | [![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/EdgarCamberos1894) |
| 🔧 **Backend Developer** | Eduin Pino | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/eduin-pino-249b45255) | [![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/Ed-Pino) |
| 🔧 **Backend Developer** | Lisandro Sánchez Morales | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=flat&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/polyglis-san-49914b343) | [![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/polyglisdev) |
---

<div align="center">
  <sub>Desarrollado con ❤️ en <a href="https://www.nocountry.tech/">No Country</a></sub>
</div>
