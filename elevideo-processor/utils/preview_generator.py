import logging
import os
import subprocess
from pathlib import Path
from typing import Optional

import cv2
import numpy as np

logger = logging.getLogger(__name__)


class PreviewGenerator:

    def __init__(self, temp_dir: str = "/tmp/video_processing"):
        self.temp_dir = temp_dir
        os.makedirs(temp_dir, exist_ok=True)

    def generate_thumbnail(
        self,
        video_path: str,
        output_path: Optional[str] = None,
        timestamp_seconds: float = 1.0,
        width: int = 480,
    ) -> str:
        if not output_path:
            output_path = os.path.join(self.temp_dir, f"{Path(video_path).stem}_thumb.jpg")

        cap = cv2.VideoCapture(video_path)
        if not cap.isOpened():
            raise RuntimeError(f"No se pudo abrir el video: {video_path}")

        cap.set(cv2.CAP_PROP_POS_FRAMES, int(timestamp_seconds * cap.get(cv2.CAP_PROP_FPS)))
        ret, frame = cap.read()
        cap.release()

        if not ret:
            raise RuntimeError(f"No se pudo leer frame en {timestamp_seconds}s")

        h, w    = frame.shape[:2]
        new_h   = int(width / (w / h))
        resized = cv2.resize(frame, (width, new_h), interpolation=cv2.INTER_AREA)
        cv2.imwrite(output_path, resized, [cv2.IMWRITE_JPEG_QUALITY, 90])

        logger.info("Thumbnail generado | output=%s | size=%dx%d", output_path, width, new_h)
        return output_path

    def generate_preview_clip(
        self,
        video_path: str,
        output_path: Optional[str] = None,
        duration_seconds: int = 5,
        start_time: float = 0.0,
    ) -> str:
        if not output_path:
            output_path = os.path.join(self.temp_dir, f"{Path(video_path).stem}_preview.mp4")

        try:
            subprocess.run(
                [
                    "ffmpeg", "-y",
                    "-ss", str(start_time),
                    "-i", video_path,
                    "-t", str(duration_seconds),
                    "-c:v", "libx264", "-preset", "fast", "-crf", "23",
                    "-c:a", "aac", "-b:a", "128k",
                    "-movflags", "+faststart",
                    output_path,
                ],
                check=True, capture_output=True, text=True,
            )
        except subprocess.CalledProcessError as e:
            logger.error("FFmpeg falló generando preview: %s", (e.stderr or "")[-500:])
            raise RuntimeError("No se pudo generar el preview")

        logger.info("Preview generado | output=%s | size=%.2fMB",
                    output_path, os.path.getsize(output_path) / (1024 * 1024))
        return output_path

    def generate_comparison(
        self,
        original_path: str,
        processed_path: str,
        output_path: Optional[str] = None,
        timestamp_seconds: float = 1.0,
        width: int = 960,
    ) -> str:
        if not output_path:
            output_path = os.path.join(self.temp_dir, f"{Path(processed_path).stem}_comparison.jpg")

        half   = width // 2
        orig   = _resize_frame(_capture_frame(original_path,  timestamp_seconds), half)
        proc   = _resize_frame(_capture_frame(processed_path, timestamp_seconds), half)

        h      = min(orig.shape[0], proc.shape[0])
        orig   = cv2.resize(orig, (half, h))
        proc   = cv2.resize(proc, (half, h))

        def _label(text: str) -> np.ndarray:
            bar = np.zeros((40, half, 3), dtype=np.uint8)
            cv2.putText(bar, text, (10, 28), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
            return bar

        comparison = np.hstack([
            np.vstack([_label("ORIGINAL"),  orig]),
            np.vstack([_label("PROCESSED"), proc]),
        ])
        cv2.imwrite(output_path, comparison, [cv2.IMWRITE_JPEG_QUALITY, 90])

        logger.info("Comparación generada | output=%s", output_path)
        return output_path

    def cleanup(self, *file_paths: str) -> None:
        for path in file_paths:
            try:
                if path and os.path.exists(path):
                    os.remove(path)
            except Exception as e:
                logger.warning("Error eliminando preview %s: %s", path, e)


def _capture_frame(video_path: str, timestamp_seconds: float) -> np.ndarray:
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        raise RuntimeError(f"No se pudo abrir: {video_path}")
    cap.set(cv2.CAP_PROP_POS_FRAMES, int(timestamp_seconds * cap.get(cv2.CAP_PROP_FPS)))
    ret, frame = cap.read()
    cap.release()
    if not ret:
        raise RuntimeError(f"No se pudo capturar frame en {timestamp_seconds}s de {video_path}")
    return frame


def _resize_frame(frame: np.ndarray, width: int) -> np.ndarray:
    h, w  = frame.shape[:2]
    new_h = int(width / (w / h))
    return cv2.resize(frame, (width, new_h), interpolation=cv2.INTER_AREA)


def create_preview_generator(temp_dir: str = "/tmp/video_processing") -> PreviewGenerator:
    return PreviewGenerator(temp_dir)
