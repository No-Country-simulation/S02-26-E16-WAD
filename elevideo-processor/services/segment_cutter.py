import logging
import os
import subprocess
import time
from pathlib import Path
from typing import Optional

logger = logging.getLogger(__name__)


class SegmentCutter:

    @staticmethod
    def cut_segment(
        input_path: str,
        start_time: float,
        duration: int,
        job_id: str,
        temp_dir: Optional[str] = None,
    ) -> str:
        if temp_dir is None:
            temp_dir = str(Path(input_path).parent)
        os.makedirs(temp_dir, exist_ok=True)

        output_path = os.path.join(temp_dir, f"{job_id}_segment_{int(start_time)}s_{duration}s.mp4")

        logger.info("Cortando segmento | job_id=%s | start=%.2fs | duration=%ds",
                    job_id, start_time, duration)

        t0 = time.time()

        # -ss post-input garantiza duración exacta (seek frame a frame desde start_time).
        # fast/crf18 preserva calidad suficiente para que MediaPipe detecte rostros.
        cmd = [
            "ffmpeg", "-y",
            "-i", input_path,
            "-ss", str(start_time),
            "-t", str(duration),
            "-c:v", "libx264", "-preset", "fast", "-crf", "18",
            "-c:a", "aac", "-b:a", "128k",
            "-movflags", "+faststart",
            output_path,
        ]

        try:
            subprocess.run(cmd, check=True, capture_output=True, text=True)
        except subprocess.CalledProcessError as e:
            logger.error("FFmpeg falló al cortar segmento | job_id=%s | stderr=%s",
                         job_id, (e.stderr or "")[-500:])
            raise RuntimeError(
                f"Error al cortar segmento (start={start_time}s, duration={duration}s)."
            )

        if not os.path.exists(output_path):
            raise RuntimeError(f"FFmpeg terminó sin crear el archivo: {output_path}")

        logger.info("Segmento cortado | job_id=%s | start=%.2fs | duration=%ds | size=%.2fMB | time=%.2fs",
                    job_id, start_time, duration,
                    os.path.getsize(output_path) / (1024 * 1024),
                    time.time() - t0)
        return output_path
