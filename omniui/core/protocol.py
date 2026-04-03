"""Protocol dataclasses for the local OmniUI agent."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from .models import Selector


@dataclass(slots=True)
class SessionInfo:
    session_id: str
    app_name: str
    platform: str
    capabilities: list[str] = field(default_factory=list)


@dataclass(slots=True)
class HealthStatus:
    status: str
    version: str
    transport: str = "http-json"


@dataclass(slots=True)
class DiscoveryRequest:
    session_id: str
    include_hidden: bool = False
    max_depth: int | None = None


@dataclass(slots=True)
class ActionRequest:
    session_id: str
    action: str
    selector: Selector
    payload: dict[str, Any] = field(default_factory=dict)


@dataclass(slots=True)
class ScreenshotRequest:
    session_id: str
    format: str = "png"
