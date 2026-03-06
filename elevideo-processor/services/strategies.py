import os
import logging
from abc import ABC, abstractmethod
from typing import Tuple

from models.schemas import (
    VideoProcessRequest,
    ShortAutoRequest,
    ShortManualRequest,
    ProcessingMode,
)
from utils.progress_tracker import ProgressTracker, ProcessingPhase

logger = logging.getLogger(__name__)


class ProcessingStrategy(ABC):

    @abstractmethod
    def process(
        self,
        local_input_path: str,
        request: VideoProcessRequest,
        config,
        detector,
        stabilizer,
        encoder: str,
        job_id: str,
        tracker: ProgressTracker,
    ) -> Tuple[str, dict]:
        """Ejecuta el procesamiento y retorna (output_path, metrics)."""

    @property
    @abstractmethod
    def mode(self) -> ProcessingMode:
        pass


class VerticalStrategy(ProcessingStrategy):

    @property
    def mode(self) -> ProcessingMode:
        return ProcessingMode.vertical

    def process(self, local_input_path, request, config, detector, stabilizer, encoder, job_id, tracker):
        from processing.video_processor_enhanced import process_video_enhanced

        tracker.update_phase(ProcessingPhase.DETECTING_FACES)

        use_multipass = request.quality.value in ("normal", "high")
        output_path, metrics = process_video_enhanced(
            local_input_path, config, detector, stabilizer,
            use_multipass=use_multipass, encoder=encoder,
        )

        metrics["segment_start"]    = None
        metrics["segment_duration"] = None

        logger.info("VerticalStrategy completada | job_id=%s | quality=%.2f%%",
                    job_id, metrics.get("overall_quality", 0) * 100)
        return output_path, metrics


class ShortAutoStrategy(ProcessingStrategy):

    @property
    def mode(self) -> ProcessingMode:
        return ProcessingMode.short_auto

    def process(self, local_input_path, request, config, detector, stabilizer, encoder, job_id, tracker):
        from processing.video_processor_enhanced import process_video_enhanced
        from services.segment_selector import SegmentSelector
        from services.segment_cutter import SegmentCutter
        from utils.validators import ShortOptionsValidator

        assert isinstance(request, ShortAutoRequest)

        tracker.update_phase(ProcessingPhase.SELECTING_SEGMENT)
        video_duration = SegmentSelector.get_video_duration(local_input_path)

        ShortOptionsValidator.validate_short_auto(
            target_duration=request.short_auto_duration,
            video_duration=video_duration,
        )

        start_time, actual_duration, selection_strategy = SegmentSelector.select_best_segment(
            video_path=local_input_path,
            total_duration=video_duration,
            target_duration=request.short_auto_duration,
            detector=detector,
            config=config,
        )
        logger.info("Segmento seleccionado | job_id=%s | start=%.2fs | duration=%ds | strategy=%s",
                    job_id, start_time, actual_duration, selection_strategy)

        tracker.update_phase(ProcessingPhase.CUTTING_SEGMENT)
        intermediate_path = SegmentCutter.cut_segment(
            input_path=local_input_path,
            start_time=start_time,
            duration=actual_duration,
            job_id=job_id,
        )

        tracker.update_phase(ProcessingPhase.DETECTING_FACES)
        use_multipass = request.quality.value in ("normal", "high")
        output_path, metrics = process_video_enhanced(
            intermediate_path, config, detector, stabilizer,
            use_multipass=use_multipass, encoder=encoder,
        )

        _remove_intermediate(intermediate_path, job_id)

        metrics.update({
            "segment_start":      start_time,
            "segment_duration":   actual_duration,
            "selection_strategy": selection_strategy,
            "original_duration":  video_duration,
        })

        logger.info("ShortAutoStrategy completada | job_id=%s | start=%.2fs | quality=%.2f%%",
                    job_id, start_time, metrics.get("overall_quality", 0) * 100)
        return output_path, metrics


class ShortManualStrategy(ProcessingStrategy):

    @property
    def mode(self) -> ProcessingMode:
        return ProcessingMode.short_manual

    def process(self, local_input_path, request, config, detector, stabilizer, encoder, job_id, tracker):
        from processing.video_processor_enhanced import process_video_enhanced
        from services.segment_selector import SegmentSelector
        from services.segment_cutter import SegmentCutter
        from utils.validators import ShortOptionsValidator

        assert isinstance(request, ShortManualRequest)

        start_time = request.short_options.start_time
        duration   = request.short_options.duration

        tracker.update_phase(ProcessingPhase.SELECTING_SEGMENT)
        video_duration = SegmentSelector.get_video_duration(local_input_path)

        # start_time negativo es error duro; exceso de duración se ajusta con warning
        ShortOptionsValidator.validate_short_manual(
            start_time=start_time,
            duration=duration,
            video_duration=None,
        )

        available = video_duration - start_time
        if duration > available:
            logger.warning(
                "Duración ajustada | job_id=%s | pedido=%ds | disponible=%.1fs | video=%.1fs",
                job_id, duration, available, video_duration,
            )
            duration = max(int(available), 1)

        tracker.update_phase(ProcessingPhase.CUTTING_SEGMENT)
        intermediate_path = SegmentCutter.cut_segment(
            input_path=local_input_path,
            start_time=start_time,
            duration=duration,
            job_id=job_id,
        )

        tracker.update_phase(ProcessingPhase.DETECTING_FACES)
        use_multipass = request.quality.value in ("normal", "high")
        output_path, metrics = process_video_enhanced(
            intermediate_path, config, detector, stabilizer,
            use_multipass=use_multipass, encoder=encoder,
        )

        _remove_intermediate(intermediate_path, job_id)

        metrics.update({
            "segment_start":      start_time,
            "segment_duration":   duration,
            "selection_strategy": "manual",
            "original_duration":  video_duration,
        })

        logger.info("ShortManualStrategy completada | job_id=%s | start=%.2fs | quality=%.2f%%",
                    job_id, start_time, metrics.get("overall_quality", 0) * 100)
        return output_path, metrics


_STRATEGY_MAP = {
    ProcessingMode.vertical:     VerticalStrategy,
    ProcessingMode.short_auto:   ShortAutoStrategy,
    ProcessingMode.short_manual: ShortManualStrategy,
}


def get_strategy(processing_mode: ProcessingMode) -> ProcessingStrategy:
    strategy_class = _STRATEGY_MAP.get(processing_mode)
    if strategy_class is None:
        raise ValueError(f"Sin estrategia para el modo '{processing_mode}'. Disponibles: {list(_STRATEGY_MAP)}")
    return strategy_class()


def _remove_intermediate(path: str, job_id: str) -> None:
    try:
        if path and os.path.exists(path):
            os.remove(path)
            logger.info("Intermedio eliminado | job_id=%s | path=%s", job_id, path)
    except Exception as e:
        logger.warning("No se pudo eliminar intermedio | job_id=%s | %s", job_id, e)
