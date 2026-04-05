from __future__ import annotations

from omniui import OmniUI

WITH_AGENT_HINT = (
    "OmniUI drag-app demo scripts require the drag-app to be running in with-agent mode.\n"
    "Start it first:\n"
    "- demo\\java\\drag-app\\run-dev-with-agent.bat"
)

_PREFERRED_PORT = 48103


def connect_or_exit() -> OmniUI:
    first_err: RuntimeError | None = None
    try:
        return OmniUI.connect(port=_PREFERRED_PORT)
    except RuntimeError as exc:
        first_err = exc
    try:
        return OmniUI.connect()
    except RuntimeError:
        raise SystemExit(f"{WITH_AGENT_HINT}\n\nConnection error: {first_err}") from first_err
