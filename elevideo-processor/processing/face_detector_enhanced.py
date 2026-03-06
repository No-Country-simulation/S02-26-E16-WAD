import cv2
import mediapipe as mp
import numpy as np
from collections import deque
from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass
class TrackingQuality:
    confidence: float
    stability: float
    age: int
    consecutive_detections: int
    lost_frames: int
    is_reliable: bool


class EnhancedFaceTracker:

    def __init__(self, face_id, initial_bbox, initial_confidence):
        self.id                    = face_id
        self.bbox                  = initial_bbox
        self.confidence            = initial_confidence
        self.age                   = 0
        self.lost_frames           = 0
        self.consecutive_detections = 1
        self.is_predicted          = False
        self.prediction_confidence = 1.0

        self.history          = deque(maxlen=30)
        self.velocity_history = deque(maxlen=10)
        self.current_velocity = (0, 0)
        self.quality_scores   = deque(maxlen=15)

        self.history.append(initial_bbox)
        self.quality_scores.append(initial_confidence)

    def update(self, bbox, confidence, is_predicted=False):
        if not is_predicted:
            self.consecutive_detections += 1
            self.lost_frames            = 0
            self.is_predicted           = False
            self.prediction_confidence  = 1.0
        else:
            self.is_predicted          = True
            self.prediction_confidence *= 0.85

        if self.history:
            prev = self.history[-1]
            self.current_velocity = (bbox[0] - prev[0], bbox[1] - prev[1])
            self.velocity_history.append(self.current_velocity)

        self.bbox = bbox
        self.confidence = confidence
        self.age += 1
        self.history.append(bbox)
        self.quality_scores.append(confidence)

    def mark_lost(self):
        self.lost_frames += 1
        self.consecutive_detections = 0
        self.update(self.predict_next_position(), self.confidence * 0.9, is_predicted=True)

    def predict_next_position(self) -> Tuple[int, int, int, int]:
        if len(self.history) < 2:
            return self.bbox
        x, y, w, h = self.bbox
        if len(self.velocity_history) >= 2:
            decay = 0.7 ** self.lost_frames
            vx    = np.median([v[0] for v in list(self.velocity_history)[-5:]]) * decay
            vy    = np.median([v[1] for v in list(self.velocity_history)[-5:]]) * decay
            return (int(x + vx), int(y + vy), w, h)
        return (x, y, w, h)

    def get_quality_metrics(self) -> TrackingQuality:
        avg_confidence = np.mean(list(self.quality_scores)) if self.quality_scores else 0
        stability      = (1.0 / (1.0 + np.std([h[0] for h in list(self.history)[-10:]]) / 10.0)
                          if len(self.history) >= 3 else 0.5)
        return TrackingQuality(
            confidence             = avg_confidence * self.prediction_confidence,
            stability              = stability,
            age                    = self.age,
            consecutive_detections = self.consecutive_detections,
            lost_frames            = self.lost_frames,
            is_reliable            = (avg_confidence > 0.7 and stability > 0.6
                                      and self.lost_frames < 5 and self.consecutive_detections > 2),
        )

    def is_alive(self, max_lost_frames=15) -> bool:
        return self.lost_frames < max_lost_frames


class SkinDetector:

    _RANGES = [
        (np.array([0, 20, 70]),  np.array([20, 150, 255])),
        (np.array([0, 25, 50]),  np.array([25, 170, 255])),
        (np.array([0, 30, 30]),  np.array([30, 200, 255])),
    ]

    def detect_face_regions(self, frame) -> List[Tuple[int, int, int, int]]:
        hsv  = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        h, w = frame.shape[:2]

        mask = np.zeros((h, w), dtype=np.uint8)
        for lo, hi in self._RANGES:
            mask = cv2.bitwise_or(mask, cv2.inRange(hsv, lo, hi))

        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (7, 7))
        mask   = cv2.morphologyEx(cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel), cv2.MORPH_OPEN, kernel)

        contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        candidates  = []
        for c in contours:
            if cv2.contourArea(c) < 2000:
                continue
            x, y, cw, ch = cv2.boundingRect(c)
            ar = cw / ch if ch > 0 else 0
            if 0.5 <= ar <= 2.0 and cw <= w * 0.6 and ch <= h * 0.6:
                candidates.append((x, y, cw, ch))
        return candidates


class EnhancedFaceDetector:

    def __init__(self, config):
        self.config         = config
        self.mp_face        = mp.solutions.face_detection.FaceDetection(
            model_selection=config.FACE_DETECTION["model_selection"],
            min_detection_confidence=config.FACE_DETECTION["min_confidence"],
        )
        self.skin_detector  = SkinDetector()
        self.trackers: List[EnhancedFaceTracker] = []
        self.next_id        = 0
        self.frame_count    = 0
        self.detection_failures  = 0
        self.fallback_activations = 0
        self.detection_cache = deque(maxlen=config.FACE_DETECTION["temporal_smoothing_window"])

        self.min_face_size     = config.FACE_DETECTION["min_face_size"]
        self.max_faces         = config.FACE_DETECTION["max_faces"]
        self.priority_mode     = config.FACE_DETECTION["priority_mode"]
        self.redetect_interval = config.FACE_DETECTION["redetect_interval_frames"]

    def detect(self, frame) -> List[dict]:
        h, w, _ = frame.shape
        self.frame_count += 1

        raw_faces = self._detect_mediapipe(frame)
        if not raw_faces and self.trackers and self.frame_count > 30:
            raw_faces = self._detect_with_fallback(frame)

        if raw_faces or (self.frame_count % self.redetect_interval == 0):
            self._update_trackers(raw_faces, w, h)
            if raw_faces:
                self.detection_failures = 0
        else:
            self.detection_failures += 1
            self._predict_trackers()

        self.trackers = [t for t in self.trackers if t.is_alive()]
        if len(self.trackers) > self.max_faces:
            self.trackers = self._prioritize_trackers()[:self.max_faces]

        faces = [self._tracker_to_face(t) for t in self.trackers]
        return self._apply_temporal_smoothing(faces) if faces else faces

    def get_primary_face(self, faces) -> Optional[dict]:
        if not faces:
            return None
        reliable = [f for f in faces if f.get("quality") and f["quality"].is_reliable]
        return reliable[0] if reliable else faces[0]

    def get_tracking_stats(self) -> dict:
        return {
            "active_trackers":     len(self.trackers),
            "detection_failures":  self.detection_failures,
            "fallback_activations": self.fallback_activations,
            "reliable_trackers":   sum(1 for t in self.trackers if t.get_quality_metrics().is_reliable),
        }

    def reset(self) -> None:
        self.trackers = []
        self.detection_cache.clear()
        self.frame_count          = 0
        self.detection_failures   = 0
        self.fallback_activations = 0

    def _detect_mediapipe(self, frame) -> List[dict]:
        h, w, _ = frame.shape

        _MAX_DETECT_WIDTH = 1280
        if w > _MAX_DETECT_WIDTH:
            scale        = _MAX_DETECT_WIDTH / w
            detect_frame = cv2.resize(frame, (int(w * scale), int(h * scale)), interpolation=cv2.INTER_AREA)
            dh, dw       = detect_frame.shape[:2]
        else:
            scale        = 1.0
            detect_frame = frame
            dh, dw       = h, w

        result = self.mp_face.process(cv2.cvtColor(detect_frame, cv2.COLOR_BGR2RGB))
        faces  = []
        if result.detections:
            for det in result.detections:
                conf = det.score[0]
                if conf < self.config.FACE_DETECTION["min_confidence"]:
                    continue
                bb          = det.location_data.relative_bounding_box
                dx, dy      = int(bb.xmin * dw), int(bb.ymin * dh)
                dbw, dbh    = int(bb.width * dw), int(bb.height * dh)
                x  = int(dx  / scale)
                y  = int(dy  / scale)
                bw = int(dbw / scale)
                bh = int(dbh / scale)
                if self._is_valid(x, y, bw, bh, w, h):
                    faces.append({"bbox": (x, y, bw, bh), "confidence": conf,
                                  "center": (x + bw // 2, y + bh // 2), "area": bw * bh, "method": "mediapipe"})
        return faces

    def _detect_with_fallback(self, frame) -> List[dict]:
        self.fallback_activations += 1
        skin_regions = self.skin_detector.detect_face_regions(frame)
        if not skin_regions:
            return []

        faces = []
        for tracker in self.trackers:
            px, py, pw, ph = tracker.predict_next_position()
            pred_cx, pred_cy = px + pw // 2, py + ph // 2
            best, min_dist = None, float("inf")
            for sx, sy, sw, sh in skin_regions:
                d = np.hypot(pred_cx - (sx + sw // 2), pred_cy - (sy + sh // 2))
                if d < min_dist and d < 150:
                    min_dist, best = d, (sx, sy, sw, sh)
            if best:
                x, y, bw, bh = best
                faces.append({"bbox": (x, y, bw, bh), "confidence": max(0.5, 1.0 - min_dist / 150),
                               "center": (x + bw // 2, y + bh // 2), "area": bw * bh, "method": "fallback"})
        return faces

    def _is_valid(self, x, y, w, h, fw, fh) -> bool:
        if w < self.min_face_size or h < self.min_face_size:   return False
        if w > fw * 0.85 or h > fh * 0.85:                     return False
        ar = w / h if h > 0 else 0
        if not (0.4 <= ar <= 2.5):                             return False
        if x < -w * 0.3 or y < -h * 0.3 or x + w > fw + 5 or y + h > fh + 5: return False
        return True

    def _update_trackers(self, detections, fw, fh):
        if not detections:
            for t in self.trackers: t.mark_lost()
            return

        matched_t, matched_d = set(), set()
        sorted_t = sorted(enumerate(self.trackers),
                          key=lambda x: x[1].get_quality_metrics().confidence, reverse=True)

        for i, det in enumerate(detections):
            best_j, best_score = None, 0.2
            for j, tracker in sorted_t:
                if j in matched_t: continue
                iou    = self._iou(det["bbox"], tracker.bbox)
                dc, tc = det["center"], (tracker.bbox[0] + tracker.bbox[2] // 2,
                                          tracker.bbox[1] + tracker.bbox[3] // 2)
                dist   = np.hypot(dc[0] - tc[0], dc[1] - tc[1])
                score  = iou * 0.7 + (1.0 - dist / np.hypot(fw, fh)) * 0.3
                if score > best_score:
                    best_score, best_j = score, j
            if best_j is not None:
                self.trackers[best_j].update(det["bbox"], det["confidence"])
                matched_t.add(best_j); matched_d.add(i)

        for j, t in enumerate(self.trackers):
            if j not in matched_t: t.mark_lost()

        for i, det in enumerate(detections):
            if i not in matched_d:
                self.trackers.append(EnhancedFaceTracker(self.next_id, det["bbox"], det["confidence"]))
                self.next_id += 1

    def _predict_trackers(self):
        for t in self.trackers: t.mark_lost()

    def _prioritize_trackers(self) -> List[EnhancedFaceTracker]:
        if self.priority_mode == "quality":
            return sorted(self.trackers, key=lambda t: t.get_quality_metrics().confidence, reverse=True)
        if self.priority_mode == "largest":
            return sorted(self.trackers, key=lambda t: t.bbox[2] * t.bbox[3], reverse=True)
        if self.priority_mode == "hybrid":
            return sorted(self.trackers, key=self._hybrid_score, reverse=True)
        return self.trackers

    def _hybrid_score(self, tracker) -> float:
        q = tracker.get_quality_metrics()
        x, y, w, h = tracker.bbox
        cx, cy     = x + w // 2, y + h // 2
        size_s     = (w * h) / (1920 * 1080)
        cent_s     = 1 - np.hypot(cx - 960, cy - 540) / np.hypot(960, 540)
        track_q    = q.confidence * (q.stability ** 0.5)
        return size_s * 0.25 + cent_s * 0.20 + track_q * 0.40 + min(q.age / 30, 1.0) * 0.15

    def _tracker_to_face(self, tracker) -> dict:
        x, y, w, h = tracker.bbox
        q = tracker.get_quality_metrics()
        return {"bbox": tracker.bbox, "confidence": q.confidence,
                "center": (x + w // 2, y + h // 2), "area": w * h,
                "tracker_id": tracker.id, "age": tracker.age,
                "quality": q, "is_predicted": tracker.is_predicted}

    def _apply_temporal_smoothing(self, faces) -> List[dict]:
        self.detection_cache.append(faces)
        if len(self.detection_cache) < 3:
            return faces

        smoothed = []
        for i, face in enumerate(faces):
            q      = face.get("quality")
            window = 3 if (q and q.is_reliable) else min(7, len(self.detection_cache))

            history, weights = [], []
            for j, past in enumerate(list(self.detection_cache)[-window:]):
                if i < len(past):
                    history.append(past[i]["bbox"])
                    weights.append(1.0 / (window - j))

            if len(history) > 1:
                w_arr = np.array(weights) / sum(weights)
                sx = int(np.average([b[0] for b in history], weights=w_arr))
                sy = int(np.average([b[1] for b in history], weights=w_arr))
                sw = int(np.average([b[2] for b in history], weights=w_arr))
                sh = int(np.average([b[3] for b in history], weights=w_arr))
                f  = face.copy()
                f["bbox"]   = (sx, sy, sw, sh)
                f["center"] = (sx + sw // 2, sy + sh // 2)
                smoothed.append(f)
            else:
                smoothed.append(face)
        return smoothed

    def _iou(self, b1, b2) -> float:
        x1, y1, w1, h1 = b1
        x2, y2, w2, h2 = b2
        xl, yt = max(x1, x2), max(y1, y2)
        xr, yb = min(x1 + w1, x2 + w2), min(y1 + h1, y2 + h2)
        if xr < xl or yb < yt: return 0.0
        inter = (xr - xl) * (yb - yt)
        union = w1 * h1 + w2 * h2 - inter
        return inter / union if union > 0 else 0.0
