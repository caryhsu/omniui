"""Core data models shared across adapters and clients."""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from datetime import datetime, UTC
from pathlib import Path
from typing import Any


@dataclass(slots=True)
class Selector:
    """Normalized selector fields accepted by the engine."""

    id: str | None = None
    text: str | None = None
    type: str | None = None
    index: int | None = None


@dataclass(slots=True)
class NodeRecord:
    """Scene graph snapshot entry returned by JavaFX discovery."""

    handle: str
    node_type: str
    hierarchy_path: str
    fx_id: str | None = None
    text: str | None = None
    visible: bool = True
    enabled: bool = True
    bounds: dict[str, float] | None = None


@dataclass(slots=True)
class ResolvedElement:
    """Result of running a selector through the resolution pipeline."""

    tier: str
    target_ref: str
    selector_used: Selector
    matched_attributes: dict[str, Any] = field(default_factory=dict)
    confidence: float | None = None
    debug_context: dict[str, Any] = field(default_factory=dict)


@dataclass(slots=True)
class ActionTrace:
    """Action metadata exposed for debugging and observability."""

    selector: Selector
    attempted_tiers: list[str]
    resolved_tier: str | None
    confidence: float | None = None
    details: dict[str, Any] = field(default_factory=dict)


@dataclass(slots=True)
class ActionResult:
    """Generic action result returned by high-level API methods."""

    ok: bool
    trace: ActionTrace
    resolved: ResolvedElement | None = None
    value: Any = None


@dataclass(slots=True)
class ActionLogEntry:
    """Persisted trace entry for a client-executed action."""

    action: str
    timestamp: datetime
    result: ActionResult

    @classmethod
    def from_result(cls, action: str, result: ActionResult) -> "ActionLogEntry":
        return cls(action=action, timestamp=datetime.now(UTC), result=result)


@dataclass
class UISnapshot:
    """Timestamped capture of the full scene graph node list."""

    nodes: list[dict[str, Any]]
    timestamp: float

    def save(self, path: str | Path) -> None:
        """Serialize snapshot to a JSON file."""
        Path(path).write_text(
            json.dumps({"timestamp": self.timestamp, "nodes": self.nodes}, indent=2),
            encoding="utf-8",
        )

    @classmethod
    def load(cls, path: str | Path) -> "UISnapshot":
        """Load a snapshot previously saved with :meth:`save`."""
        data = json.loads(Path(path).read_text(encoding="utf-8"))
        return cls(nodes=data["nodes"], timestamp=data["timestamp"])


@dataclass
class UIDiff:
    """Result of comparing two UISnapshot objects.

    Each entry in ``changed`` is a dict with keys ``"before"`` and ``"after"``.
    """

    added: list[dict[str, Any]]
    removed: list[dict[str, Any]]
    changed: list[dict[str, Any]]


@dataclass
class RecordedEvent:
    """A single user interaction event captured by the Java agent."""

    event_type: str   # "click" | "type" | "drag" | "set_color" | "select_combo" | "set_date" | "assertion" | "right_click"
    fx_id: str
    text: str
    node_type: str
    node_index: int
    timestamp: float
    # drag-only fields (populated when event_type == "drag")
    to_fx_id: str = ""
    to_text: str = ""
    to_node_type: str = ""
    to_node_index: int = 0
    # set_color-only field
    color: str = ""
    # assertion-only fields (populated when event_type == "assertion")
    assertion_type: str = ""   # "verify_text" | "verify_visible" | "verify_enabled"
    expected: str = ""         # expected value for verify_text; empty for others


@dataclass
class RecordedScript:
    """Result of a recording session: raw events plus generated Python script."""

    events: list[RecordedEvent]
    script: str

    def save(self, path: str | Path) -> None:
        """Write the generated script to *path*."""
        Path(path).write_text(self.script, encoding="utf-8")
