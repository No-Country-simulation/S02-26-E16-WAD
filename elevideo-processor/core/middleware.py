import logging
import os
import secrets

from dotenv import load_dotenv
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse

load_dotenv()

logger = logging.getLogger(__name__)

_SERVICE_KEY_HEADER = "X-Service-Key"
_EXCLUDED_PATHS     = {"/", "/health"}
_SERVICE_API_KEY    = os.getenv("SERVICE_API_KEY", "").strip()

if not _SERVICE_API_KEY:
    logger.warning("SERVICE_API_KEY no configurada — verificación de X-Service-Key DESACTIVADA")
else:
    logger.info("Service key activa | header=%s", _SERVICE_KEY_HEADER)


class ServiceKeyMiddleware(BaseHTTPMiddleware):
    """
    Primera capa de defensa: verifica X-Service-Key antes de que el JWT sea evaluado.
    Sin SERVICE_API_KEY configurada, deja pasar todo (útil en desarrollo local).
    Usa secrets.compare_digest para evitar timing attacks.
    """

    async def dispatch(self, request: Request, call_next):
        if request.url.path in _EXCLUDED_PATHS or not _SERVICE_API_KEY:
            return await call_next(request)

        incoming = request.headers.get(_SERVICE_KEY_HEADER, "")

        if not incoming or not secrets.compare_digest(incoming, _SERVICE_API_KEY):
            logger.warning(
                "Request rechazado: %s inválido o ausente | path=%s | method=%s",
                _SERVICE_KEY_HEADER, request.url.path, request.method,
            )
            return JSONResponse(status_code=401, content={"detail": "Autenticación requerida."})

        return await call_next(request)


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    """Añade headers de seguridad estándar a todas las respuestas."""

    async def dispatch(self, request: Request, call_next):
        response = await call_next(request)

        response.headers["X-Content-Type-Options"]    = "nosniff"
        response.headers["X-Frame-Options"]           = "DENY"
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
        response.headers["Referrer-Policy"]           = "no-referrer"
        response.headers["Permissions-Policy"]        = "geolocation=(), microphone=(), camera=()"
        response.headers.pop("server", None)

        return response
