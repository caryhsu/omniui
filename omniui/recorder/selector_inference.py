"""Selector inference: derive the most stable selector from a RecordedEvent."""
from __future__ import annotations

from omniui.core.models import RecordedEvent


def infer_selector(event: RecordedEvent) -> dict:
    """Return the best selector dict for *event*.

    Priority:
    1. ``fxId`` present → ``{"id": fx_id}``
    2. ``text`` non-empty and ≤ 40 chars → ``{"text": text}``
    3. fallback → ``{"type": node_type, "index": node_index, "_fragile": True}``
    """
    if event.fx_id:
        return {"id": event.fx_id}
    if event.text and len(event.text) <= 40:
        return {"text": event.text}
    return {"type": event.node_type, "index": event.node_index, "_fragile": True}
