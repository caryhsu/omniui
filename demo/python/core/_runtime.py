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


def connect_or_exit() -> OmniUI:
    try:
        return OmniUI.connect(base_url="http://127.0.0.1:48100")
    except Exception as exc:
        raise SystemExit(f"{WITH_AGENT_HINT}\n\nConnection error: {exc}") from exc
