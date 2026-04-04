from __future__ import annotations

from omniui import OmniUI


WITH_AGENT_HINT = (
    "OmniUI demo scripts require a demo app to be running in with-agent mode.\n"
    "Start one of the apps from demo/java/core-app/, input-app/, or advanced-app/.\n"
    "For example:\n"
    "- demo\\java\\core-app\\run-dev-with-agent.bat\n"
    "- demo\\java\\input-app\\run-dev-with-agent.bat\n"
    "- demo\\java\\advanced-app\\run-dev-with-agent.bat"
)


def connect_or_exit() -> OmniUI:
    try:
        return OmniUI.connect(app_name="LoginDemo")
    except Exception as exc:
        raise SystemExit(f"{WITH_AGENT_HINT}\n\nConnection error: {exc}") from exc
