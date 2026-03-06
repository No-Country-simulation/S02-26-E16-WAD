import logging
import os
import subprocess
import time
from pathlib import Path
from typing import List, Tuple

import cv2
import numpy as np

logger = logging.getLogger(__name__)

_VERTICAL_AR_LOW  = 0.53
_VERTICAL_AR_HIGH = 0.59
_TARGET_AR        = 9 / 16
_FACE_RELIABILITY_THRESHOLD = 0.0


def process_video_enhanced(
    input_path: str,
    config,
    detector,
    stabilizer,
    use_multipass: bool = True,
    encoder: str = "libx264",
) -> Tuple[str, dict]:
    cap = cv2.VideoCapture(input_path)
    if not cap.isOpened():
        raise ValueError(f"No se pudo abrir el video: {input_path}")

    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    fps          = cap.get(cv2.CAP_PROP_FPS)
    width        = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height       = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    cap.release()

    logger.info("Procesando | %dx%d | %d frames @ %.2ffps | mode=%s | encoder=%s",
                width, height, total_frames, fps, config.CONVERSION_MODE["mode"], encoder)

    ar         = width / height
    crop_w     = config.CROP_SETTINGS["width"]
    crop_h     = config.CROP_SETTINGS["height"]

    # Ya vertical con dimensiones exactas
    if _VERTICAL_AR_LOW <= ar <= _VERTICAL_AR_HIGH:
        if width == crop_w and height == crop_h:
            logger.info("Video ya es vertical con dimensiones exactas — sin procesamiento")
            return input_path, _base_metrics(total_frames, reason="already_vertical_exact")
        logger.info("Video vertical, dimensiones distintas — re-escalando")
        return _rescale_vertical(input_path, config, encoder)

    # Más estrecho que el crop deseado
    if width <= crop_w:
        logger.warning("Video más estrecho que crop (%dx%d <= %d) — modo full", width, height, crop_w)
        return _process_full(input_path, config, encoder)

    # Video horizontal — modo full o smart crop
    if config.CONVERSION_MODE["mode"] == "full":
        return _process_full(input_path, config, encoder)

    return _process_smart_crop(input_path, config, detector, stabilizer,
                                use_multipass, encoder, total_frames, fps, width, crop_w, crop_h)


def _process_smart_crop(
    input_path, config, detector, stabilizer, use_multipass,
    encoder, total_frames, fps, frame_width, crop_w, crop_h,
) -> Tuple[str, dict]:
    from processing.ffmpeg_ultra import crop_video_ultra

    if use_multipass:
        from processing.stabilization_enhanced import MultiPassStabilizer
        multipass = MultiPassStabilizer(config)

    sample_rate     = config.PERFORMANCE_SETTINGS["sample_rate"]
    positions       = []
    quality_metrics = []
    frame_number    = 0
    frames_processed = 0
    t0              = time.time()

    cap = cv2.VideoCapture(input_path)
    while True:
        ret, frame = cap.read()
        if not ret:
            break

        if frame_number % sample_rate == 0:
            ts   = frame_number / fps if fps > 0 else 0
            faces = detector.detect(frame)
            face  = detector.get_primary_face(faces)

            if face:
                crop_x, _ = _optimal_composition(face, (frame_width, cap.get(cv2.CAP_PROP_FRAME_HEIGHT)),
                                                  (crop_w, crop_h), config)
                q = face.get("quality")
                if use_multipass:
                    multipass.add_position(ts, crop_x, q)
                else:
                    sx = stabilizer.stabilize(crop_x, q) if hasattr(stabilizer, "stabilize") else crop_x
                    positions.append((ts, sx))
                conf_val     = q.confidence  if q else face.get("confidence", 0.5)
                stab_val     = q.stability   if q else 0.5
                reliable_val = q.is_reliable if q else (conf_val >= 0.65)
                quality_metrics.append({
                    "timestamp":   ts,
                    "confidence":  conf_val,
                    "stability":   stab_val,
                    "is_reliable": reliable_val,
                })
            else:
                if use_multipass:
                    last = multipass._buffer[-1]["position"] if multipass._buffer else (frame_width - crop_w) // 2
                    multipass.add_position(ts, last, None)
                else:
                    fallback = positions[-1][1] if positions else (frame_width - crop_w) // 2
                    sx = stabilizer.stabilize(None) if hasattr(stabilizer, "stabilize") else fallback
                    positions.append((ts, sx if sx is not None else fallback))

            frames_processed += 1

        frame_number += 1
    cap.release()

    if use_multipass:
        positions = multipass.process()

    # Fallback si muy pocos rostros detectados
    # reliability = fracción de frames analizados donde se detectó al menos una cara
    total_analyzed = frames_processed
    faces_detected = len(quality_metrics)
    reliability    = faces_detected / total_analyzed if total_analyzed > 0 else 0.0

    logger.info("Detección | frames_analizados=%d | con_cara=%d | reliability=%.1f%%",
                total_analyzed, faces_detected, reliability * 100)

    if reliability < _FACE_RELIABILITY_THRESHOLD:
        logger.warning("Sin detecciones de cara — cambiando a modo full")
        return _process_full(input_path, config, encoder)

    output_path = _output_path(input_path, config.CONVERSION_MODE["mode"])
    success     = crop_video_ultra(input_path, output_path, positions, config, encoder=encoder)
    if not success:
        raise RuntimeError("Error en el encoding del video")

    metrics = {
        "total_frames":    total_frames,
        "frames_processed": frames_processed,
        "keyframes":        len(positions),
        "analysis_time":    time.time() - t0,
        "overall_quality":  1.0,
        "reliability_rate": reliability,
    }
    if quality_metrics:
        avg_c = np.mean([m["confidence"] for m in quality_metrics])
        avg_s = np.mean([m["stability"]  for m in quality_metrics])
        metrics["overall_quality"] = avg_c * 0.4 + avg_s * 0.3 + reliability * 0.3

    logger.info("Smart crop completado | quality=%.1f%% | time=%.2fs",
                metrics["overall_quality"] * 100, metrics["analysis_time"])
    return output_path, metrics


def _process_full(input_path: str, config, encoder: str) -> Tuple[str, dict]:
    from processing.ffmpeg_ultra import crop_video_ultra

    cap    = cv2.VideoCapture(input_path)
    width  = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    cap.release()

    crop_w = config.CROP_SETTINGS["width"]
    crop_h = config.CROP_SETTINGS["height"]

    if width == crop_w and height == crop_h:
        return input_path, _base_metrics(reason="already_exact")

    ar = width / height
    if _VERTICAL_AR_LOW <= ar <= _VERTICAL_AR_HIGH:
        return _rescale_vertical(input_path, config, encoder)

    # Forzar modo full temporalmente para el crop_video_ultra
    original_mode = config.CONVERSION_MODE.get("mode")
    config.CONVERSION_MODE["mode"] = "full"
    try:
        output = _output_path(input_path, "full")
        ok     = crop_video_ultra(input_path, output, [], config, encoder=encoder)
        if not ok:
            raise RuntimeError("Error en el encoding del video en modo full")
        return output, {**_base_metrics(reason="full_mode"), "mode": "full"}
    finally:
        config.CONVERSION_MODE["mode"] = original_mode


def _rescale_vertical(input_path: str, config, encoder: str) -> Tuple[str, dict]:
    crop_w = config.CROP_SETTINGS["width"]
    crop_h = config.CROP_SETTINGS["height"]
    preset = config.ENCODING_SETTINGS["quality_preset"]
    s      = config.ENCODING_SETTINGS["presets"][preset]

    output = _output_path(input_path, "rescaled")
    cmd    = ["ffmpeg", "-y", "-i", input_path,
              "-vf", f"scale={crop_w}:{crop_h}:force_original_aspect_ratio=decrease,"
                     f"pad={crop_w}:{crop_h}:(ow-iw)/2:(oh-ih)/2:color=black",
              "-c:v", encoder, "-preset", s["preset"], "-crf", str(s["crf"])]

    if encoder == "libx264":
        cmd.extend(["-profile:v", s.get("profile", "high")])

    cmd.extend(["-pix_fmt", "yuv420p", "-movflags", "+faststart", "-c:a", "aac", "-b:a", "128k", output])

    try:
        subprocess.run(cmd, check=True, capture_output=True, text=True)
    except subprocess.CalledProcessError as e:
        logger.error("Re-scale falló | stderr=%s", (e.stderr or "")[-500:])
        raise RuntimeError("Error al re-escalar el video")

    logger.info("Video re-escalado | output=%s", output)
    return output, {**_base_metrics(reason="vertical_rescale"), "mode": "vertical_rescale"}


def _optimal_composition(face: dict, frame_size, crop_size, config) -> Tuple[int, int]:
    frame_w, _     = frame_size
    crop_w, crop_h = crop_size
    _, __, w, h    = face["bbox"]
    cx, _          = face["center"]
    cs             = config.CROP_SETTINGS

    if cs.get("use_rule_of_thirds", False):
        ratio  = cx / frame_w
        offset = cs.get("thirds_offset_factor", 0.15)
        if ratio < 0.35:   target = crop_w * (0.33 - offset)
        elif ratio > 0.65: target = crop_w * (0.67 + offset)
        else:              target = crop_w * 0.5
    else:
        target = crop_w * 0.5

    headroom     = cs.get("headroom_ratio", 0.18) * crop_h
    face_h_ratio = h / frame_size[1] if frame_size[1] else 0
    if face_h_ratio > 0.4:    headroom *= 0.7
    elif face_h_ratio < 0.15: headroom *= 1.3  # noqa: F841 — reservado para composición futura

    padding = cs.get("edge_padding", 15)
    crop_x  = int(np.clip(cx - target, padding, frame_w - crop_w - padding))
    return crop_x, 0


def _output_path(input_path: str, suffix: str) -> str:
    stem      = Path(input_path).stem
    ts        = time.strftime("%Y%m%d_%H%M%S")
    temp_dir  = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "temp")
    os.makedirs(temp_dir, exist_ok=True)
    return os.path.join(temp_dir, f"{stem}_vertical_{suffix}_{ts}.mp4")


def _base_metrics(total_frames: int = 0, reason: str = "") -> dict:
    return {
        "total_frames":    total_frames,
        "frames_processed": 0,
        "keyframes":        0,
        "analysis_time":    0,
        "overall_quality":  1.0,
        "skipped_reason":   reason,
    }
