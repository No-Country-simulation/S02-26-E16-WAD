import logging

logger = logging.getLogger(__name__)


FACE_DETECTION: dict = {
    "model_selection":           1,
    "min_confidence":            0.60,
    "min_face_size":             30,
    "max_faces":                 1,
    "priority_mode":             "quality",
    "temporal_smoothing_window": 10,
    "redetect_interval_frames":  8,
    "use_fallback_detection":    True,
    "fallback_min_area":         2000,
    "tracking_max_lost_frames":  15,
}

STABILIZATION: dict = {
    "method": "hybrid",
    "kalman": {
        "process_variance":    0.008,
        "measurement_variance": 0.08,
        "estimation_error":    1.0,
    },
    "exponential_alpha":             0.88,
    "max_velocity_px_per_frame":     35,
    "max_acceleration_px_per_frame2": 8,
    "deadzone_pixels":               3,
    "use_prediction":                True,
    "prediction_frames":             10,
    "prediction_weight":             0.25,
    "use_easing":                    True,
    "easing_function":               "smooth",
    "adaptive_parameters":           True,
    "movement_analysis_window":      15,
}

CROP_SETTINGS: dict = {
    "width":                   1080,
    "height":                  1920,
    "use_rule_of_thirds":      True,
    "thirds_offset_factor":    0.15,
    "headroom_ratio":          0.18,
    "edge_padding":            15,
    "dynamic_zoom":            False,
    "zoom_range":              (650, 800),
    "adaptive_composition":    True,
    "composition_adjust_speed": 0.3,
}

KEYFRAME_SETTINGS: dict = {
    "optimize_keyframes":            True,
    "base_threshold_pixels":         12,
    "max_keyframes":                 80,
    "target_keyframes":              50,
    "tolerance":                     8,
    "force_keyframe_interval_seconds": 2.5,
    "min_keyframe_distance_seconds": 0.15,
    "quality_based_threshold":       True,
}

ENCODING_SETTINGS: dict = {
    "video_codec":    "libx264",
    "audio_codec":    "aac",
    "pix_fmt":        "yuv420p",
    "audio_sample_rate": 48000,
    "b_frames":       2,
    "gop_size":       60,
    "faststart":      True,
    "apply_unsharp":  True,
    "unsharp_params": "5:5:0.8:5:5:0.0",
    "color_primaries": "bt709",
    "color_trc":      "bt709",
    "colorspace":     "bt709",
    "quality_preset": "high",
    "presets": {
        "ultra_fast": {"preset": "ultrafast", "crf": "28", "profile": "baseline", "bitrate_audio": "128k"},
        "fast":       {"preset": "fast",      "crf": "24", "profile": "main",     "bitrate_audio": "160k"},
        "balanced":   {"preset": "medium",    "crf": "21", "profile": "high",     "level": "4.1", "bitrate_audio": "192k"},
        "high":       {"preset": "slow",      "crf": "18", "profile": "high",     "level": "4.2", "bitrate_audio": "224k", "tune": "film"},
        "ultra":      {"preset": "veryslow",  "crf": "16", "profile": "high",     "level": "5.0", "bitrate_audio": "256k", "audio_quality": 0, "tune": "film"},
        "web":        {"preset": "medium",    "crf": "22", "profile": "high",     "level": "4.1", "maxrate": "5M", "bufsize": "10M", "bitrate_audio": "160k"},
    },
}

PERFORMANCE_SETTINGS: dict = {
    "sample_rate":    3,
    "verbose":        True,
    "use_multipass":  True,
}

QUALITY_METRICS: dict = {
    "calculate_metrics":    True,
    "track_quality_issues": True,
    "min_acceptable_quality": 0.70,
}

CONVERSION_MODE: dict = {
    "mode": "smart_crop",
    "modes": {
        "full": {
            "description":      "Mantiene todo el contenido con letterbox",
            "width":            1080,
            "height":           1920,
            "blur_background":  False,
            "background_color": "black",
        },
        "smart_crop": {
            "description": "Recorte inteligente siguiendo rostros",
            "width":       1080,
            "height":      1920,
        },
    },
}

SCENE_ANALYSIS: dict = {
    "enabled":                 False,
    "scene_change_threshold":  0.3,
}


def set_conversion_mode(mode: str) -> None:
    CONVERSION_MODE["mode"] = mode


def log_current_config() -> None:
    logger.info(
        "Config activa | mode=%s | preset=%s | sample_rate=1/%s | multipass=%s",
        CONVERSION_MODE["mode"],
        ENCODING_SETTINGS["quality_preset"],
        PERFORMANCE_SETTINGS["sample_rate"],
        PERFORMANCE_SETTINGS["use_multipass"],
    )


def apply_preset(preset_name: str) -> None:
    presets = {
        "ultra_quality": {
            "performance": {"sample_rate": 2, "use_multipass": True},
            "stabilization": {"exponential_alpha": 0.92, "max_velocity_px_per_frame": 25, "deadzone_pixels": 4},
            "keyframes": {"max_keyframes": 100, "tolerance": 5},
            "encoding": {"quality_preset": "ultra"},
        },
        "professional": {
            "performance": {"sample_rate": 3, "use_multipass": True},
            "stabilization": {"exponential_alpha": 0.88, "max_velocity_px_per_frame": 35},
            "keyframes": {"max_keyframes": 80},
            "encoding": {"quality_preset": "high"},
        },
        "balanced": {
            "performance": {"sample_rate": 4, "use_multipass": False},
            "stabilization": {"exponential_alpha": 0.85, "max_velocity_px_per_frame": 40},
            "keyframes": {"max_keyframes": 60},
            "encoding": {"quality_preset": "balanced"},
        },
        "fast": {
            "performance": {"sample_rate": 6, "use_multipass": False},
            "stabilization": {"exponential_alpha": 0.80, "max_velocity_px_per_frame": 50, "deadzone_pixels": 2},
            "keyframes": {"max_keyframes": 40},
            "encoding": {"quality_preset": "fast", "apply_unsharp": False},
        },
    }

    platform_aliases = {
        "tiktok":         ("professional", {"crop": {"headroom_ratio": 0.16}, "encoding": {"quality_preset": "web"}}),
        "instagram":      ("professional", {"crop": {"headroom_ratio": 0.18}}),
        "youtube_shorts": ("ultra_quality", {"encoding": {"quality_preset": "high"}}),
    }

    base_name, overrides = platform_aliases.get(preset_name, (preset_name, {}))
    config = presets.get(base_name)

    if config is None:
        logger.warning("Preset desconocido: %s", preset_name)
        return

    PERFORMANCE_SETTINGS.update(config.get("performance", {}))
    STABILIZATION.update(config.get("stabilization", {}))
    KEYFRAME_SETTINGS.update(config.get("keyframes", {}))
    ENCODING_SETTINGS.update(config.get("encoding", {}))

    if "crop" in overrides:
        CROP_SETTINGS.update(overrides["crop"])
    if "encoding" in overrides:
        ENCODING_SETTINGS.update(overrides["encoding"])

    logger.info(
        "Preset aplicado: %s | encoding=%s | sample_rate=1/%s | multipass=%s",
        preset_name,
        ENCODING_SETTINGS["quality_preset"],
        PERFORMANCE_SETTINGS["sample_rate"],
        PERFORMANCE_SETTINGS["use_multipass"],
    )
