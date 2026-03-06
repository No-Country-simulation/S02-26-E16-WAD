"""
Valida el JWT de servicio emitido por Spring Boot (HS256).

Flujo: Frontend → Spring Boot → [JWT de servicio] → este microservicio.

El token debe contener:
  sub         — user_id (UUID)
  aud         — "python-service"
  token_type  — "DELEGATED_SERVICE"
  scope       — "PYTHON_SERVICE"
  iat / exp   — emitido / expiración

Variables de entorno requeridas:
  SERVICE_JWT_SECRET   — secret HS256 en Base64 (mismo que jwt.secret en Spring)
  SERVICE_JWT_MAX_AGE  — vida máxima en segundos (default: 300)
"""

import base64
import logging
import os
from datetime import datetime, timezone
from typing import Optional

import jwt
from dotenv import load_dotenv
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

load_dotenv()

logger = logging.getLogger(__name__)

_EXPECTED_AUDIENCE   = "python-service"
_EXPECTED_TOKEN_TYPE = "DELEGATED_SERVICE"
_EXPECTED_SCOPE      = "PYTHON_SERVICE"
_ALGORITHM           = "HS256"
_REQUIRED_CLAIMS     = ["sub", "exp", "iat", "aud", "token_type", "scope"]

try:
    _secret_b64 = os.environ["SERVICE_JWT_SECRET"]
    # Spring Boot decodifica el secret desde Base64; replicamos ese comportamiento aquí.
    _JWT_SECRET: bytes = base64.b64decode(_secret_b64)
    _MAX_AGE_SECONDS: int = int(os.getenv("SERVICE_JWT_MAX_AGE", "300"))
except KeyError:
    logger.error("SERVICE_JWT_SECRET no configurada — todos los requests serán rechazados")
    _JWT_SECRET      = b""
    _MAX_AGE_SECONDS = 300

_bearer = HTTPBearer(auto_error=False)


class TokenData:
    """Datos del usuario extraídos del JWT una vez validado."""

    __slots__ = ("user_id",)

    def __init__(self, user_id: str):
        self.user_id = user_id

    def __repr__(self) -> str:
        return f"TokenData(user_id={self.user_id!r})"


def _unauthorized(detail: str) -> HTTPException:
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=detail,
        headers={"WWW-Authenticate": "Bearer"},
    )


async def require_service_token(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(_bearer),
) -> TokenData:
    """Dependencia FastAPI que valida el JWT de servicio en cada request."""
    if not _JWT_SECRET:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Servicio no disponible.",
        )

    if credentials is None:
        raise _unauthorized("Autenticación requerida.")

    try:
        payload = jwt.decode(
            credentials.credentials,
            _JWT_SECRET,
            algorithms=[_ALGORITHM],
            audience=_EXPECTED_AUDIENCE,
            options={"verify_exp": True, "verify_iat": True, "require": _REQUIRED_CLAIMS},
        )
    except jwt.ExpiredSignatureError:
        raise _unauthorized("Token expirado.")
    except jwt.InvalidAudienceError:
        raise _unauthorized("Token no autorizado.")
    except jwt.MissingRequiredClaimError:
        raise _unauthorized("Token inválido.")
    except jwt.InvalidTokenError:
        raise _unauthorized("Token inválido.")

    iat = payload.get("iat")
    if iat is not None:
        age = (datetime.now(timezone.utc) - datetime.fromtimestamp(iat, tz=timezone.utc)).total_seconds()
        if age > _MAX_AGE_SECONDS:
            raise _unauthorized("Token expirado.")

    if payload.get("token_type") != _EXPECTED_TOKEN_TYPE:
        raise _unauthorized("Token no autorizado.")

    if payload.get("scope") != _EXPECTED_SCOPE:
        raise _unauthorized("Token no autorizado.")

    return TokenData(user_id=str(payload["sub"]))


def verify_job_ownership(job: dict, token: TokenData, job_id: str) -> None:
    """
    Verifica que el job pertenece al usuario del token.
    Lanza 404 si el job no existe, 403 si pertenece a otro usuario.
    """
    if job is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Job {job_id} no encontrado.")

    if job.get("user_id") != token.user_id:
        logger.warning("Acceso denegado | job_id=%s | owner=%s | requester=%s", job_id, job.get("user_id"), token.user_id)
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No tienes permiso para acceder a este job.")
