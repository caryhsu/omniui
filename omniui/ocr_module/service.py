"""OCR fallback services."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(slots=True)
class OcrMatch:
    text: str
    confidence: float
    bounds: dict[str, int]


class SimpleOcrEngine:
    """A lightweight OCR provider contract for Phase 1.

    The default implementation parses UTF-8 fixture lines in the format:
    `text|confidence|x|y|width|height`
    """

    def read(self, image: bytes) -> list[OcrMatch]:
        if not image:
            return []
        try:
            decoded = image.decode("utf-8")
        except UnicodeDecodeError:
            return []

        matches: list[OcrMatch] = []
        for raw_line in decoded.splitlines():
            line = raw_line.strip()
            if not line:
                continue
            parts = line.split("|")
            if len(parts) != 6:
                continue
            text, confidence, x, y, width, height = parts
            try:
                matches.append(
                    OcrMatch(
                        text=text,
                        confidence=float(confidence),
                        bounds={
                            "x": int(x),
                            "y": int(y),
                            "width": int(width),
                            "height": int(height),
                        },
                    )
                )
            except ValueError:
                continue
        return matches
