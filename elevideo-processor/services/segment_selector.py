import json
import logging
import os
import re
import subprocess
import tempfile
from typing import List, Optional, Tuple

from models.schemas import SHORT_MIN_DURATION_SECONDS

logger = logging.getLogger(__name__)

_SAMPLE_STEP            = 1.0
_FACE_SAMPLE_EVERY      = 1.5
_SCENE_CHANGE_THRESHOLD = 0.35
_SCENE_CUT_PENALTY      = 0.25

_WEIGHT_FACES  = 0.40
_WEIGHT_AUDIO  = 0.25
_WEIGHT_MOTION = 0.25
_WEIGHT_SCENE  = 0.10


class SegmentSelector:

    @staticmethod
    def select_best_segment(
        video_path: str,
        total_duration: float,
        target_duration: int,
        detector=None,
        config=None,
    ) -> Tuple[float, int, str]:
        if total_duration <= target_duration:
            actual = max(SHORT_MIN_DURATION_SECONDS, int(total_duration))
            logger.info("Video corto (%.2fs <= %ds): usando video completo", total_duration, target_duration)
            return 0.0, actual, "full_video"

        try:
            return SegmentSelector._analyze_and_select(video_path, total_duration, target_duration, detector, config)
        except Exception as e:
            logger.warning("Análisis falló (%s). Usando segmento central como fallback.", e)
            start, duration = SegmentSelector._central_segment(total_duration, target_duration)
            return start, duration, "central_fallback"

    @staticmethod
    def get_video_duration(video_path: str) -> float:
        cmd = ["ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", video_path]
        try:
            result = subprocess.run(cmd, check=True, capture_output=True, text=True)
            return float(json.loads(result.stdout)["format"]["duration"])
        except subprocess.CalledProcessError as e:
            raise RuntimeError(f"No se pudo obtener la duración: {video_path}") from e
        except (KeyError, ValueError, json.JSONDecodeError) as e:
            raise RuntimeError(f"Metadata inválida o incompleta: {video_path}") from e

    @staticmethod
    def _analyze_and_select(
        video_path: str,
        total_duration: float,
        target_duration: int,
        detector=None,
        config=None,
    ) -> Tuple[float, int, str]:
        candidates    = SegmentSelector._generate_candidates(total_duration, target_duration)
        audio_scores  = SegmentSelector._analyze_audio(video_path)
        scene_cuts    = SegmentSelector._detect_scene_cuts(video_path)
        motion_scores = SegmentSelector._analyze_motion(video_path)

        face_scores: Optional[dict] = None
        if detector is not None and config is not None:
            try:
                face_scores = SegmentSelector._analyze_faces(video_path, total_duration, detector, config)
            except Exception as e:
                logger.warning("Análisis de caras falló (%s). Continuando sin este criterio.", e)

        best_start, best_score, _ = SegmentSelector._score_candidates(
            candidates, target_duration, audio_scores, motion_scores, scene_cuts, face_scores,
        )

        has_faces  = face_scores is not None
        has_motion = len(motion_scores) > 0

        if has_faces and has_motion:    strategy = "smart_auto"
        elif has_faces:                 strategy = "smart_auto_no_motion"
        elif has_motion:                strategy = "smart_auto_no_faces"
        else:                           strategy = "smart_auto_audio_only"

        logger.info("Segmento seleccionado | start=%.2fs | score=%.3f | strategy=%s",
                    best_start, best_score, strategy)
        return best_start, target_duration, strategy

    @staticmethod
    def _generate_candidates(total_duration: float, target_duration: int) -> List[float]:
        max_start  = total_duration - target_duration
        candidates = []
        current    = 0.0
        while current <= max_start:
            candidates.append(round(current, 3))
            current += _SAMPLE_STEP
        if not candidates or candidates[-1] < max_start - 0.1:
            candidates.append(round(max_start, 3))
        return candidates

    @staticmethod
    def _analyze_audio(video_path: str) -> dict:
        cmd = [
            "ffmpeg", "-i", video_path, "-vn",
            "-af", "asetnsamples=n=1024,astats=metadata=1:reset=1,ametadata=print:key=lavfi.astats.Overall.RMS_level:file=-",
            "-f", "null", "-",
        ]
        try:
            result  = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
            pattern = re.compile(r"pts_time:(\d+\.?\d*)\s+lavfi\.astats\.Overall\.RMS_level=(-?\d+\.?\d*)")
            rms_by_time = {}
            for m in pattern.finditer(result.stderr + result.stdout):
                t, rms_db = float(m.group(1)), float(m.group(2))
                rms_by_time[t] = 10 ** (rms_db / 20) if rms_db > -100 else 0.0

            if not rms_by_time:
                return {}

            window_scores: dict = {}
            for t in [i * _SAMPLE_STEP for i in range(int(max(rms_by_time) / _SAMPLE_STEP) + 2)]:
                vals = [v for ts, v in rms_by_time.items() if t <= ts < t + _SAMPLE_STEP]
                if vals:
                    window_scores[t] = sum(vals) / len(vals)

            max_val = max(window_scores.values(), default=0)
            return {k: v / max_val for k, v in window_scores.items()} if max_val > 0 else window_scores
        except Exception:
            return {}

    @staticmethod
    def _detect_scene_cuts(video_path: str) -> List[float]:
        cmd = ["ffmpeg", "-i", video_path, "-vf", f"scdet=threshold={_SCENE_CHANGE_THRESHOLD}", "-f", "null", "-"]
        try:
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=120)
            return [float(m.group(1)) for m in re.finditer(r"lavfi\.scd\.time=(\d+\.?\d*)", result.stderr + result.stdout)]
        except Exception:
            return []

    @staticmethod
    def _analyze_motion(video_path: str) -> dict:
        cmd = [
            "ffmpeg", "-i", video_path,
            "-vf", "mestimate=method=ds:search_param=7,metadata=print:key=lavfi.me.sad.avg:file=-",
            "-r", "1", "-f", "null", "-",
        ]
        try:
            result  = subprocess.run(cmd, capture_output=True, text=True, timeout=180)
            pattern = re.compile(r"pts_time:(\d+\.?\d*)\s+lavfi\.me\.sad\.avg=(\d+\.?\d*)")
            raw     = {float(m.group(1)): float(m.group(2)) for m in pattern.finditer(result.stderr + result.stdout)}

            if not raw:
                return {}

            max_sad    = max(raw.values())
            normalized = {t: v / max_sad for t, v in raw.items()} if max_sad > 0 else dict(raw)

            times   = sorted(normalized)
            smoothed = {}
            for i, t in enumerate(times):
                neighbors    = [normalized[times[j]] for j in range(max(0, i - 1), min(len(times), i + 2))]
                smoothed[t]  = sum(neighbors) / len(neighbors)
            return smoothed
        except Exception:
            return {}

    @staticmethod
    def _analyze_faces(video_path: str, total_duration: float, detector, config) -> dict:
        sample_times = [round(t, 2) for t in _frange(0, total_duration, _FACE_SAMPLE_EVERY)]
        face_scores: dict = {}

        with tempfile.TemporaryDirectory(prefix="face_analysis_") as tmp_dir:
            for t in sample_times:
                frame_path = os.path.join(tmp_dir, f"frame_{t:.2f}.jpg")
                result = subprocess.run(
                    ["ffmpeg", "-ss", str(t), "-i", video_path, "-vframes", "1", "-q:v", "3", "-f", "image2", frame_path, "-y"],
                    capture_output=True, timeout=10,
                )
                if result.returncode != 0 or not os.path.exists(frame_path):
                    face_scores[t] = 0.0
                    continue
                try:
                    import cv2
                    frame = cv2.imread(frame_path)
                    if frame is None:
                        face_scores[t] = 0.0
                        continue
                    faces = detector.detect_faces(frame)
                    if   len(faces) == 0: face_scores[t] = 0.0
                    elif len(faces) == 1: face_scores[t] = 1.0
                    else:                 face_scores[t] = 0.85
                except Exception:
                    face_scores[t] = 0.0

        times    = sorted(face_scores)
        smoothed = {}
        for i, t in enumerate(times):
            neighbors   = [face_scores[times[j]] for j in range(max(0, i - 1), min(len(times), i + 2))]
            smoothed[t] = sum(neighbors) / len(neighbors)
        return smoothed

    @staticmethod
    def _score_candidates(
        candidates: List[float],
        target_duration: int,
        audio_scores: dict,
        motion_scores: dict,
        scene_cuts: List[float],
        face_scores: Optional[dict],
    ) -> Tuple[float, float, list]:
        has_faces  = bool(face_scores)
        has_audio  = bool(audio_scores)
        has_motion = bool(motion_scores)

        available = []
        if has_faces:  available.append(("faces",  _WEIGHT_FACES))
        if has_audio:  available.append(("audio",  _WEIGHT_AUDIO))
        if has_motion: available.append(("motion", _WEIGHT_MOTION))

        total_w   = sum(w for _, w in available) or 1.0
        eff_w     = {name: w / total_w for name, w in available}

        best_start = candidates[0]
        best_score = -1.0
        detail     = []

        for start in candidates:
            face_s   = _window_avg(face_scores,   start, target_duration) if has_faces  else None
            audio_s  = _window_avg(audio_scores,  start, target_duration) if has_audio  else None
            motion_s = _window_avg(motion_scores, start, target_duration) if has_motion else None

            n_cuts = _count_cuts(scene_cuts, start, target_duration)

            if available:
                base = 0.0
                if has_faces  and face_s  is not None: base += eff_w["faces"]  * face_s
                if has_audio  and audio_s  is not None: base += eff_w["audio"]  * audio_s
                if has_motion and motion_s is not None: base += eff_w["motion"] * motion_s
            else:
                base = 0.5

            score = max(0.0, base - _SCENE_CUT_PENALTY * n_cuts)
            detail.append({"start": start, "face": face_s, "audio": audio_s, "motion": motion_s, "cuts": n_cuts, "score": score})

            if score > best_score:
                best_score = score
                best_start = start

        for rank, entry in enumerate(sorted(detail, key=lambda x: x["score"], reverse=True)[:3], 1):
            logger.debug("Top %d | start=%.2fs | face=%.2f | audio=%.2f | motion=%.2f | cuts=%d | score=%.3f",
                         rank, entry["start"], entry["face"] or 0, entry["audio"] or 0,
                         entry["motion"] or 0, entry["cuts"], entry["score"])

        return best_start, best_score, detail

    @staticmethod
    def _central_segment(total_duration: float, target_duration: int) -> Tuple[float, int]:
        center     = total_duration / 2.0
        start_time = max(0.0, center - target_duration / 2.0)
        if start_time + target_duration > total_duration:
            start_time = total_duration - target_duration
        return round(start_time, 3), target_duration


def _frange(start: float, stop: float, step: float) -> List[float]:
    values, current = [], start
    while current <= stop:
        values.append(current)
        current += step
    return values


def _window_avg(scores: dict, start: float, duration: int) -> float:
    if not scores:
        return 0.5
    end      = start + duration
    relevant = [v for t, v in scores.items() if start <= t < end]
    if not relevant:
        closest = min(scores, key=lambda t: abs(t - start))
        return scores[closest]
    return sum(relevant) / len(relevant)


def _count_cuts(scene_cuts: List[float], start: float, duration: int) -> int:
    margin = 0.5
    end    = start + duration
    return sum(1 for cut in scene_cuts if (start + margin) < cut < (end - margin))
