import json
import logging
import os
import subprocess
from typing import List, Tuple

logger = logging.getLogger(__name__)


def crop_video_ultra(input_path: str, output_path: str, positions: list, config, encoder="libx264") -> bool:
    mode        = config.CONVERSION_MODE["mode"]
    mode_config = config.CONVERSION_MODE["modes"][mode]
    logger.info("Modo de conversión: %s | encoder: %s", mode.upper(), encoder)

    if mode == "full":
        return _process_full(input_path, output_path, config, mode_config, encoder)
    return _process_smart_crop(input_path, output_path, positions, config, encoder)


def _process_full(input_path, output_path, config, mode_config, encoder) -> bool:
    w, h = mode_config["width"], mode_config["height"]

    if mode_config.get("blur_background", False):
        vf = (f"[0:v]split=2[bg][fg];"
              f"[bg]scale={w}:{h}:force_original_aspect_ratio=increase,crop={w}:{h},gblur=sigma=20[blurred];"
              f"[fg]scale={w}:{h}:force_original_aspect_ratio=decrease[scaled];"
              f"[blurred][scaled]overlay=(W-w)/2:(H-h)/2")
        logger.info("Usando fondo difuminado")
    else:
        bg = mode_config.get("background_color", "black")
        vf = (f"scale={w}:{h}:force_original_aspect_ratio=decrease,"
              f"pad={w}:{h}:(ow-iw)/2:(oh-ih)/2:color={bg}")
        logger.info("Usando letterbox | fondo=%s", bg)

    return _encode(input_path, output_path, vf, config, encoder)


def _process_smart_crop(input_path, output_path, positions, config, encoder) -> bool:
    cw, ch = config.CROP_SETTINGS["width"], config.CROP_SETTINGS["height"]

    if config.KEYFRAME_SETTINGS.get("optimize_keyframes", False) and positions:
        min_move = config.KEYFRAME_SETTINGS.get("min_movement_threshold", 5)
        optimized = [positions[0]]
        for p in positions[1:]:
            if abs(p[1] - optimized[-1][1]) > min_move:
                optimized.append(p)
        logger.info("Keyframes: %d → %d", len(positions), len(optimized))
        positions = optimized

    if not positions:
        crop_vf = f"crop={cw}:{ch}:(iw-{cw})/2:0"
    else:
        positions = sorted(positions, key=lambda p: p[0])
        expr      = _build_lerp(positions, config.STABILIZATION.get("use_easing", False))
        crop_vf   = f"crop={cw}:{ch}:x='{expr}':y=0"

    filters = [crop_vf]
    if config.ENCODING_SETTINGS.get("apply_unsharp", False):
        filters.append(f"unsharp={config.ENCODING_SETTINGS['unsharp_params']}")

    return _encode(input_path, output_path, ",".join(filters), config, encoder)


def _encode(input_path, output_path, vf, config, encoder) -> bool:
    preset   = config.ENCODING_SETTINGS["quality_preset"]
    settings = config.ENCODING_SETTINGS["presets"][preset]

    cmd = ["ffmpeg", "-y", "-i", input_path, "-vf", vf,
           "-c:v", encoder, "-preset", settings["preset"], "-crf", str(settings["crf"])]

    if encoder == "libx264":
        cmd.extend(["-profile:v", settings.get("profile", "high")])

    cmd.extend(["-pix_fmt", "yuv420p", "-movflags", "+faststart",
                "-c:a", "aac", "-b:a", "128k", output_path])

    logger.info("Encoding | preset=%s | ffmpeg_preset=%s | crf=%s",
                preset, settings["preset"], settings["crf"])

    try:
        subprocess.run(cmd, check=True, capture_output=True, text=True)
    except subprocess.CalledProcessError as e:
        logger.error("FFmpeg falló | stderr=%s", (e.stderr or "")[-1000:])
        return False

    if os.path.exists(output_path):
        logger.info("Video generado | size=%.2fMB", os.path.getsize(output_path) / (1024 * 1024))
        _log_video_info(output_path)
    return True


def _build_lerp(positions: List[Tuple[float, float]], use_easing: bool) -> str:
    if len(positions) == 1:
        return str(int(positions[0][1]))

    # FFmpeg tiene un límite práctico de anidación de expresiones ~30 niveles.
    # Si hay más keyframes, submuestreamos para quedarnos en ese límite.
    _MAX_KEYFRAMES = 28
    if len(positions) > _MAX_KEYFRAMES:
        step      = (len(positions) - 1) / (_MAX_KEYFRAMES - 1)
        indices   = [int(round(i * step)) for i in range(_MAX_KEYFRAMES)]
        indices[-1] = len(positions) - 1  # asegurar que incluye el último
        positions = [positions[i] for i in indices]
        logger.info("Keyframes reducidos a %d para expresión FFmpeg", len(positions))

    expr = ""
    for i in range(len(positions) - 1):
        t1, x1 = positions[i]
        t2, x2 = positions[i + 1]
        dur = t2 - t1
        if dur <= 0:
            continue
        interp = f"{int(x1)}+({int(x2)}-{int(x1)})*(t-{t1:.3f})/{dur:.3f}"
        expr += f"if(between(t,{t1:.3f},{t2:.3f}),{interp},"

    return expr + str(int(positions[-1][1])) + ")" * (len(positions) - 1)


def _log_video_info(path: str):
    try:
        result = subprocess.run(
            ["ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", path],
            capture_output=True, text=True, check=True,
        )
        info = json.loads(result.stdout)
        for s in info.get("streams", []):
            if s["codec_type"] == "video":
                logger.info("Resolución=%sx%s | codec=%s", s["width"], s["height"], s["codec_name"])
                if "r_frame_rate" in s:
                    num, den = s["r_frame_rate"].split("/")
                    logger.info("FPS=%.2f", float(num) / float(den))
        if "duration" in info.get("format", {}):
            logger.info("Duración=%.2fs", float(info["format"]["duration"]))
    except Exception as e:
        logger.warning("No se pudo obtener metadata: %s", e)
