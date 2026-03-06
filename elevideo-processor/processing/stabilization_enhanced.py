import numpy as np
from collections import deque
from enum import Enum
from typing import List, Optional, Tuple


class MovementType(Enum):
    STATIC   = "static"
    SMOOTH   = "smooth"
    MODERATE = "moderate"
    RAPID    = "rapid"
    ERRATIC  = "erratic"


class AdaptiveKalmanFilter:

    def __init__(self, process_variance=0.01, measurement_variance=0.1, estimation_error=1.0):
        self.base_pv          = process_variance
        self.base_mv          = measurement_variance
        self.estimation_error = estimation_error
        self.process_variance = process_variance
        self.measurement_variance = measurement_variance
        self.posterior        = None

    def update(self, measurement, movement_type=MovementType.SMOOTH) -> float:
        self._adapt(movement_type)
        if self.posterior is None:
            self.posterior = measurement
            return measurement

        prior_error  = self.estimation_error + self.process_variance
        gain         = prior_error / (prior_error + self.measurement_variance)
        self.posterior      = self.posterior + gain * (measurement - self.posterior)
        self.estimation_error = (1 - gain) * prior_error
        return self.posterior

    def _adapt(self, mt: MovementType):
        if   mt == MovementType.STATIC:   self.process_variance, self.measurement_variance = self.base_pv * 0.3, self.base_mv * 2.0
        elif mt == MovementType.SMOOTH:   self.process_variance, self.measurement_variance = self.base_pv,       self.base_mv
        elif mt == MovementType.MODERATE: self.process_variance, self.measurement_variance = self.base_pv * 1.5, self.base_mv * 0.8
        elif mt == MovementType.RAPID:    self.process_variance, self.measurement_variance = self.base_pv * 3.0, self.base_mv * 0.4
        elif mt == MovementType.ERRATIC:  self.process_variance, self.measurement_variance = self.base_pv * 0.5, self.base_mv * 3.0


class MovementAnalyzer:

    def __init__(self, window_size=15):
        self.position_history = deque(maxlen=window_size)
        self.velocity_history = deque(maxlen=window_size)

    def analyze(self, position) -> MovementType:
        self.position_history.append(position)
        if len(self.position_history) < 3:
            return MovementType.SMOOTH

        velocities = [abs(self.position_history[i] - self.position_history[i - 1])
                      for i in range(1, len(self.position_history))]
        self.velocity_history.append(velocities[-1])

        if len(velocities) >= 5:
            recent   = list(self.velocity_history)[-5:]
            avg, std = np.mean(recent), np.std(recent)
            mx       = np.max(recent)
            if avg < 2:                          return MovementType.STATIC
            if avg < 8  and std < 4:             return MovementType.SMOOTH
            if avg < 20 and std < 10:            return MovementType.MODERATE
            if mx  > 40 and std < 15:            return MovementType.RAPID
            return MovementType.ERRATIC

        return MovementType.SMOOTH

    def get_smoothness_score(self) -> float:
        if len(self.velocity_history) < 3:
            return 1.0
        v    = list(self.velocity_history)
        mean = np.mean(v)
        return 1.0 / (1.0 + np.std(v) / mean) if mean > 0 else 1.0


class AdaptiveStabilizer:

    def __init__(self, config):
        self.config    = config
        self.method    = config.STABILIZATION["method"]

        if self.method in ("kalman", "hybrid"):
            kp = config.STABILIZATION["kalman"]
            self.kalman_x = AdaptiveKalmanFilter(**kp)
            self.kalman_y = AdaptiveKalmanFilter(**kp)

        self.movement_analyzer = MovementAnalyzer(window_size=15)

        self.base_alpha    = config.STABILIZATION["exponential_alpha"]
        self.current_alpha = self.base_alpha
        self.smooth_pos    = None

        self.use_prediction    = config.STABILIZATION["use_prediction"]
        self.prediction_weight = config.STABILIZATION["prediction_weight"]
        self.position_history  = deque(maxlen=30)

        self.base_max_velocity = config.STABILIZATION["max_velocity_px_per_frame"]
        self.max_velocity      = self.base_max_velocity
        self.max_acceleration  = config.STABILIZATION["max_acceleration_px_per_frame2"]

        self.base_deadzone    = config.STABILIZATION["deadzone_pixels"]
        self.current_deadzone = self.base_deadzone

        self.use_easing      = config.STABILIZATION["use_easing"]
        self.easing_function = config.STABILIZATION["easing_function"]

        self.prev_position        = None
        self.prev_velocity        = 0
        self.lost_tracking_frames = 0
        self.recovery_mode        = False
        self.stability_scores     = deque(maxlen=30)

    def stabilize(self, raw_position, tracking_quality=None):
        if raw_position is None:
            self.lost_tracking_frames += 1
            if self.prev_position is not None and self.lost_tracking_frames < 10:
                return self._handle_lost_tracking()
            return self.prev_position

        if self.lost_tracking_frames > 0:
            self.recovery_mode = True
        self.lost_tracking_frames = 0

        mt = self.movement_analyzer.analyze(raw_position)
        self._adapt(mt, tracking_quality)

        if   self.method == "kalman":      s = self.kalman_x.update(raw_position, mt)
        elif self.method == "exponential": s = self._exp_smooth(raw_position)
        elif self.method == "hybrid":      s = self._hybrid(raw_position, mt)
        else:                              s = raw_position

        s = self._deadzone(s, mt)
        s = self._velocity_limit(s, mt)
        if self.use_prediction and not self.recovery_mode:
            s = self._predict(s)
        if self.use_easing and self.prev_position is not None:
            s = self._easing(self.prev_position, s, recovery=self.recovery_mode)

        self.position_history.append(s)
        self.prev_position = s
        self.stability_scores.append(self.movement_analyzer.get_smoothness_score())

        if self.recovery_mode and len(self.position_history) > 5:
            self.recovery_mode = False

        return s

    def get_stability_score(self) -> float:
        return float(np.mean(list(self.stability_scores))) if self.stability_scores else 1.0

    def reset(self):
        if hasattr(self, "kalman_x"):
            self.kalman_x.posterior = self.kalman_y.posterior = None
        self.smooth_pos           = None
        self.prev_position        = None
        self.prev_velocity        = 0
        self.lost_tracking_frames = 0
        self.recovery_mode        = False
        self.position_history.clear()
        self.stability_scores.clear()
        self.movement_analyzer = MovementAnalyzer()

    def _adapt(self, mt: MovementType, quality=None):
        alpha_map = {
            MovementType.STATIC:   min(0.98, self.base_alpha + 0.10),
            MovementType.SMOOTH:   self.base_alpha,
            MovementType.MODERATE: max(0.75, self.base_alpha - 0.10),
            MovementType.RAPID:    max(0.65, self.base_alpha - 0.20),
            MovementType.ERRATIC:  min(0.95, self.base_alpha + 0.15),
        }
        self.current_alpha = alpha_map.get(mt, self.base_alpha)
        self.max_velocity  = self.base_max_velocity * (1.8 if mt == MovementType.RAPID
                                                        else 1.3 if mt == MovementType.MODERATE else 1.0)
        self.current_deadzone = (self.base_deadzone * 2.0
                                 if quality and not quality.is_reliable else self.base_deadzone)

    def _handle_lost_tracking(self):
        if len(self.position_history) < 3:
            return self.prev_position
        recent = list(self.position_history)[-5:]
        v      = np.median([recent[i] - recent[i - 1] for i in range(1, len(recent))])
        return self.prev_position + v * (0.7 ** self.lost_tracking_frames)

    def _exp_smooth(self, position) -> float:
        if self.smooth_pos is None:
            self.smooth_pos = position
            return position
        self.smooth_pos = self.smooth_pos * self.current_alpha + position * (1 - self.current_alpha)
        return self.smooth_pos

    def _hybrid(self, position, mt: MovementType) -> float:
        k = self.kalman_x.update(position, mt)
        if self.smooth_pos is None:
            self.smooth_pos = k
            return k
        self.smooth_pos = self.smooth_pos * self.current_alpha + k * (1 - self.current_alpha)
        return self.smooth_pos

    def _deadzone(self, position, mt: MovementType) -> float:
        if self.prev_position is None: return position
        dz = self.current_deadzone * (1.5 if mt == MovementType.ERRATIC else 1.0)
        return self.prev_position if abs(position - self.prev_position) < dz else position

    def _velocity_limit(self, position, mt: MovementType) -> float:
        if self.prev_position is None: return position
        v   = np.clip(position - self.prev_position, -self.max_velocity, self.max_velocity)
        max_acc = self.max_acceleration * (0.5 if mt == MovementType.ERRATIC else 1.0)
        a   = np.clip(v - self.prev_velocity, -max_acc, max_acc)
        v   = self.prev_velocity + a
        self.prev_velocity = v
        return self.prev_position + v

    def _predict(self, position) -> float:
        if len(self.position_history) < 5: return position
        recent = list(self.position_history)[-10:]
        vels   = [recent[i] - recent[i - 1] for i in range(1, len(recent))]
        if len(vels) >= 3:
            signs = [np.sign(v) for v in vels[-5:]]
            if all(s == signs[0] for s in signs) and signs[0] != 0:
                predicted = position + np.median(vels[-3:]) * 0.3
                w = self.prediction_weight * 0.7
                return position * (1 - w) + predicted * w
        return position

    def _easing(self, start, end, recovery=False) -> float:
        t = 0.3 if recovery else 0.5
        t = t * t * (3 - 2 * t)
        if not recovery and self.easing_function == "cubic":
            return start + (end - start) * 0.6
        if not recovery and self.easing_function == "linear":
            return end
        return start + (end - start) * t


class MultiPassStabilizer:

    def __init__(self, config):
        self.config  = config
        self._buffer = []

    def add_position(self, timestamp: float, position, tracking_quality=None):
        self._buffer.append({"timestamp": timestamp, "position": position, "quality": tracking_quality})

    def process(self) -> List[Tuple[float, float]]:
        if not self._buffer:
            return []
        p1 = self._pass1_filter()
        p2 = self._pass2_smooth(p1)
        return self._pass3_refine(p2)

    def _pass1_filter(self) -> List[Tuple[float, float]]:
        positions  = [p["position"] for p in self._buffer]
        timestamps = [p["timestamp"] for p in self._buffer]
        qualities  = [p.get("quality") for p in self._buffer]
        filtered   = self._remove_outliers(positions, qualities)
        kalman     = AdaptiveKalmanFilter()
        return list(zip(timestamps, [kalman.update(p) for p in filtered]))

    def _pass2_smooth(self, positions: List[Tuple[float, float]]) -> List[Tuple[float, float]]:
        ts, vals = zip(*positions)
        return list(zip(ts, self._gaussian_smooth(np.array(vals), window=9, sigma=3.0)))

    def _pass3_refine(self, positions: List[Tuple[float, float]]) -> List[Tuple[float, float]]:
        ts, vals   = zip(*positions)
        max_v      = self.config.STABILIZATION["max_velocity_px_per_frame"] * 0.7
        refined    = [vals[0]]
        for v in vals[1:]:
            diff = np.clip(v - refined[-1], -max_v, max_v)
            refined.append(refined[-1] + diff)
        return list(zip(ts, refined))

    def _remove_outliers(self, positions, qualities, threshold=3.0) -> list:
        arr  = np.array(positions)
        mean, std = np.mean(arr), np.std(arr)
        if std == 0:
            return positions
        result = []
        for i, pos in enumerate(positions):
            z   = abs((pos - mean) / std)
            q   = qualities[i]
            thr = threshold * 0.6 if (q and not q.is_reliable) else threshold
            if z < thr:
                result.append(pos)
            else:
                result.append((positions[i - 1] + positions[i + 1]) / 2
                               if 0 < i < len(positions) - 1
                               else (positions[i - 1] if i > 0 else positions[i + 1]))
        return result

    @staticmethod
    def _gaussian_smooth(values: np.ndarray, window=7, sigma=2.0) -> np.ndarray:
        hw      = window // 2
        x       = np.arange(-hw, hw + 1)
        kernel  = np.exp(-(x ** 2) / (2 * sigma ** 2))
        kernel /= kernel.sum()
        return np.convolve(np.pad(values, hw, mode="edge"), kernel, mode="valid")
