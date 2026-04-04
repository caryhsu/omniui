from __future__ import annotations

from omniui import OmniUI


WITH_AGENT_HINT = (
    "OmniUI core demo scripts require the core-app to be running in with-agent mode.\n"
    "Start one of these first:\n"
    "- demo\\java\\core-app\\run-dev-with-agent.bat\n"
    "- demo\\java\\core-app\\run-with-agent.bat\n"
    "- powershell -ExecutionPolicy Bypass -File .\\demo\\java\\core-app\\run-dev-with-agent.ps1\n"
    "- powershell -ExecutionPolicy Bypass -File .\\demo\\java\\core-app\\run-with-agent.ps1\n"
    "- ./demo/java/core-app/run-dev-with-agent.sh\n"
    "- ./demo/java/core-app/run-with-agent.sh"
)

_PREFERRED_PORT = 48100


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
