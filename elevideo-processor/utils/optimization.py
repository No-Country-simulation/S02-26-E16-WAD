import hashlib
import json
import logging
import os
import subprocess
from typing import Any, Dict, Optional

logger = logging.getLogger(__name__)


class ConfigurationCache:

    def __init__(self, cache_dir: str = "/tmp/video_processing/cache"):
        self.cache_dir = cache_dir
        os.makedirs(cache_dir, exist_ok=True)

    def _key(self, **params) -> str:
        return hashlib.md5(json.dumps(params, sort_keys=True).encode()).hexdigest()

    def get(self, **params) -> Optional[Dict[str, Any]]:
        path = os.path.join(self.cache_dir, f"{self._key(**params)}.json")
        try:
            with open(path) as f:
                return json.load(f)
        except Exception:
            return None

    def set(self, data: Dict[str, Any], **params) -> None:
        path = os.path.join(self.cache_dir, f"{self._key(**params)}.json")
        try:
            with open(path, "w") as f:
                json.dump(data, f)
        except Exception as e:
            logger.warning("Error guardando cache: %s", e)

    def clear(self) -> None:
        for f in os.listdir(self.cache_dir):
            if f.endswith(".json"):
                try:
                    os.remove(os.path.join(self.cache_dir, f))
                except Exception:
                    pass


class FrameSamplingOptimizer:

    def __init__(self, threshold: float = 30.0):
        self.threshold       = threshold
        self.last_frame      = None
        self.frames_skipped  = 0
        self.frames_processed = 0

    def should_process_frame(self, frame) -> bool:
        import cv2
        import numpy as np

        if self.last_frame is None:
            self.last_frame = frame.copy()
            self.frames_processed += 1
            return True

        diff = cv2.absdiff(
            cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY),
            cv2.cvtColor(self.last_frame, cv2.COLOR_BGR2GRAY),
        )
        if np.mean(diff) > self.threshold:
            self.last_frame = frame.copy()
            self.frames_processed += 1
            return True

        self.frames_skipped += 1
        return False

    def get_stats(self) -> Dict[str, Any]:
        total     = self.frames_processed + self.frames_skipped
        skip_rate = self.frames_skipped / total * 100 if total > 0 else 0
        return {"frames_processed": self.frames_processed,
                "frames_skipped":   self.frames_skipped,
                "skip_rate_percent": skip_rate}

    def reset(self) -> None:
        self.last_frame       = None
        self.frames_skipped   = 0
        self.frames_processed = 0


class HardwareAccelerationDetector:

    @staticmethod
    def detect_gpu_support() -> Dict[str, Any]:
        result = {"nvidia_nvenc": False, "intel_qsv": False, "recommended_encoder": "libx264"}

        try:
            if subprocess.run(["nvidia-smi"], capture_output=True, timeout=2).returncode == 0:
                result["nvidia_nvenc"]        = True
                result["recommended_encoder"] = "h264_nvenc"
                logger.info("NVIDIA GPU detectada — usando h264_nvenc")
                return result
        except (FileNotFoundError, subprocess.TimeoutExpired):
            pass

        try:
            vainfo = subprocess.run(["vainfo"], capture_output=True, text=True, timeout=2)
            if "VAProfileH264" in vainfo.stdout:
                result["intel_qsv"]           = True
                result["recommended_encoder"] = "h264_qsv"
                logger.info("Intel QSV detectado — usando h264_qsv")
                return result
        except (FileNotFoundError, subprocess.TimeoutExpired):
            pass

        logger.info("Sin aceleración por hardware — usando libx264")
        return result

    @staticmethod
    def get_optimized_ffmpeg_encoder() -> str:
        return HardwareAccelerationDetector.detect_gpu_support()["recommended_encoder"]


class BatchProcessor:

    def __init__(self, batch_size: int = 10):
        self.batch_size = batch_size
        self._batch: list = []

    def add(self, item: Any) -> None:
        self._batch.append(item)

    def should_process(self) -> bool:
        return len(self._batch) >= self.batch_size

    def get_batch(self) -> list:
        batch, self._batch = self._batch, []
        return batch

    def flush(self) -> list:
        return self.get_batch()


class PerformanceMonitor:

    def __init__(self):
        self.metrics: Dict[str, Any] = {
            "total_processing_time": 0.0,
            "analysis_time":         0.0,
            "encoding_time":         0.0,
            "upload_time":           0.0,
            "download_time":         0.0,
            "frames_analyzed":       0,
            "frames_skipped":        0,
            "cache_hits":            0,
            "cache_misses":          0,
            "hw_acceleration_used":  False,
        }

    def record_metric(self, name: str, value: Any) -> None:
        if name in self.metrics and isinstance(self.metrics[name], (int, float)) and isinstance(value, (int, float)):
            self.metrics[name] += value
        else:
            self.metrics[name] = value

    def get_summary(self) -> Dict[str, Any]:
        summary = dict(self.metrics)
        total_frames = summary["frames_analyzed"] + summary["frames_skipped"]
        if total_frames > 0:
            summary["optimization_rate"] = summary["frames_skipped"] / total_frames * 100
        total_cache = summary["cache_hits"] + summary["cache_misses"]
        if total_cache > 0:
            summary["cache_hit_rate"] = summary["cache_hits"] / total_cache * 100
        return summary

    def log_summary(self) -> None:
        s = self.get_summary()
        logger.info("Performance | total=%.2fs | analysis=%.2fs | encoding=%.2fs | upload=%.2fs | hw=%s",
                    s["total_processing_time"], s["analysis_time"],
                    s["encoding_time"], s["upload_time"], s["hw_acceleration_used"])
        if "optimization_rate" in s:
            logger.info("Frames skipped: %d (%.1f%%)", s["frames_skipped"], s["optimization_rate"])
        if "cache_hit_rate" in s:
            logger.info("Cache hit rate: %.1f%%", s["cache_hit_rate"])


_config_cache:        Optional[ConfigurationCache] = None
_performance_monitor: Optional[PerformanceMonitor] = None


def get_config_cache() -> ConfigurationCache:
    global _config_cache
    if _config_cache is None:
        _config_cache = ConfigurationCache()
    return _config_cache


def get_performance_monitor() -> PerformanceMonitor:
    global _performance_monitor
    if _performance_monitor is None:
        _performance_monitor = PerformanceMonitor()
    return _performance_monitor
