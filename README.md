🎬 Elevideo

Elevideo es una aplicación web responsiva diseñada para convertir videos horizontales en formato vertical (9:16), optimizados para TikTok y YouTube Shorts, permitiendo a startups, pymes y creadores generar contenido de marketing de forma rápida y eficiente.

🚀 Objetivo del Proyecto

Reducir el tiempo y esfuerzo necesarios para reutilizar videos horizontales y transformarlos en shorts verticales listos para redes sociales, sin que este proceso se convierta en una carga operativa para el negocio.

🧩 Alcance del MVP (4 semanas)

El MVP de Elevideo incluye:

Subida de videos horizontales

Gestión y listado de videos

Procesamiento automático a formato vertical (9:16)

Editor básico (auto-center / crop)

Vista previa del resultado

Descarga del video final

UI responsiva (desktop y mobile)

🏗️ Arquitectura General

Monorepo con separación clara por responsabilidades:

Frontend: React + Vite + TypeScript

Backend: Spring Boot (Java)

Database: PostgreSQL

Procesamiento de video: Servicio backend (FFmpeg o mock en MVP)

Elevideo/
├── frontend/     # React + Vite
├── backend/      # Spring Boot
├── database/     # PostgreSQL (Docker)
├── docs/         # Arquitectura y contrato API
└── README.md

👥 Equipos Involucrados

Backend Team

API REST

Persistencia

Procesamiento de video

Frontend Team

UI/UX

Editor vertical

Integración con API

Product / PM

Scope

Priorización

QA y entrega MVP

📅 Cronograma

Duración total: 4 semanas

Week 1: Fundaciones, arquitectura y contratos

Week 2: Flujo core de video (upload → estado)

Week 3: Editor vertical y procesamiento

Week 4: UX, responsive, QA y entrega

El detalle de tareas se gestiona en GitHub Projects.

📌 Gestión del Proyecto

GitHub Projects (v2)

Metodología: Kanban + Sprints semanales

1 Issue = 1 tarea concreta

Pull Request obligatorio para main

Estados del board:

Backlog

Ready

In Progress

In Review

Blocked

Done

📄 Documentación

docs/architecture.md → Arquitectura técnica

docs/api-contract.md → Contrato API (request/response)

🎯 Criterio de Éxito del MVP

Un usuario puede subir un video horizontal

Convertirlo a formato vertical

Visualizar una preview

Descargar el resultado final

Todo el flujo funciona en menos de 3 pasos principales

🔮 Futuro (Post-MVP)

Autenticación de usuarios

Templates por red social

Subtítulos automáticos

Branding (logos, colores)

Publicación directa a redes sociales

Elevideo
Transforma tus videos. Eleva tu alcance.