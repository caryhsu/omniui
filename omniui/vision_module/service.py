"""Vision fallback services."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(slots=True)
class VisionMatch:
    matched: bool
    confidence: float
    bounds: dict[str, int] | None = None


class SimpleVisionEngine:
    """A tiny template matcher for deterministic tests and fixtures."""

    def match(self, image: bytes, template: bytes) -> VisionMatch:
        if not image or not template:
            return VisionMatch(matched=False, confidence=0.0, bounds=None)
        position = image.find(template)
        if position < 0:
            return VisionMatch(matched=False, confidence=0.0, bounds=None)

        confidence = min(1.0, max(0.1, len(template) / max(len(image), 1)))
        return VisionMatch(
            matched=True,
            confidence=confidence,
            bounds={
                "x": position,
                "y": 0,
                "width": len(template),
                "height": 1,
            },
        )
