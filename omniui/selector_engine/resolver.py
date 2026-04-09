"""Selector normalization helpers for the Phase 1 resolution pipeline."""

from __future__ import annotations

from omniui.core.models import Selector


def normalize_selector(
    id: str | None = None,
    text: str | None = None,
    type: str | None = None,
    index: int | str | None = None,
) -> Selector:
    normalized_index: int | None
    if isinstance(index, bool):
        normalized_index = int(index)
    elif isinstance(index, int):
        normalized_index = index
    elif isinstance(index, str) and index.strip():
        normalized_index = int(index.strip())
    else:
        normalized_index = None

    return Selector(
        id=id.strip() if isinstance(id, str) and id.strip() else None,
        text=text.strip() if isinstance(text, str) and text.strip() else None,
        type=type.strip() if isinstance(type, str) and type.strip() else None,
        index=normalized_index,
    )


def selector_priority(selector: Selector) -> list[str]:
    priorities: list[str] = []
    if selector.id:
        priorities.append("id")
    if selector.type and selector.text:
        priorities.append("type_text")
    if selector.type and selector.index is not None:
        priorities.append("type_index")
    return priorities
