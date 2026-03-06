import logging
import os

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from routers import video
from services.video_service import create_video_service
from storage.cloudinary_service import create_cloudinary_service

logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO").upper(),
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
)
logger = logging.getLogger("video-processor-api")

app = FastAPI(
    title="Video Processor API",
    description="Convierte videos horizontales a verticales para redes sociales.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def startup():
    logger.info("Iniciando Video Processor API")
    cloudinary = create_cloudinary_service()
    svc        = create_video_service(cloudinary)
    video.set_services(cloudinary, svc)
    logger.info("Servicios inicializados | cloud=%s | temp=%s",
                os.getenv("CLOUDINARY_CLOUD_NAME"), cloudinary.temp_dir)


app.include_router(video.router)


@app.get("/")
async def root():
    return {
        "status":    "ok",
        "version":   "1.0.0",
        "docs":      "/docs",
        "endpoints": {
            "process":  "/api/video/process",
            "status":   "/api/video/status/{job_id}",
            "download": "/api/video/download/{job_id}",
        },
    }


@app.get("/health")
async def health():
    return {"status": "healthy", "cloudinary_configured": bool(os.getenv("CLOUDINARY_CLOUD_NAME"))}


@app.exception_handler(RequestValidationError)
async def validation_error_handler(request, exc):
    logger.error("422 | %s", exc.errors())
    return JSONResponse(status_code=422, content={"detail": exc.errors()})


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=os.getenv("HOST", "0.0.0.0"),
        port=int(os.getenv("PORT", 8000)),
        reload=os.getenv("RELOAD", "true").lower() == "true",
    )
