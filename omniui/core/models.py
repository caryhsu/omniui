"""Core data models shared across adapters and clients."""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, UTC
from typing import Any


@dataclass(slots=True)
class Selector:
    """Normalized selector fields accepted by the engine."""

    id: str | None = None
    text: str | None = None
    type: str | None = None


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


@dataclass
class UIDiff:
    """Result of comparing two UISnapshot objects.

    Each entry in ``changed`` is a dict with keys ``"before"`` and ``"after"``.
    """

    added: list[dict[str, Any]]
    removed: list[dict[str, Any]]
    changed: list[dict[str, Any]]
