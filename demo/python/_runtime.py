from __future__ import annotations

from omniui import OmniUI


WITH_AGENT_HINT = (
    "OmniUI demo scripts require the JavaFX login app to be running in with-agent mode.\n"
    "Start one of these first:\n"
    "- demo\\javafx-login-app\\run-dev-with-agent.bat\n"
    "- demo\\javafx-login-app\\run-with-agent.bat\n"
    "- powershell -ExecutionPolicy Bypass -File .\\demo\\javafx-login-app\\run-dev-with-agent.ps1\n"
    "- powershell -ExecutionPolicy Bypass -File .\\demo\\javafx-login-app\\run-with-agent.ps1\n"
    "- ./demo/javafx-login-app/run-dev-with-agent.sh\n"
    "- ./demo/javafx-login-app/run-with-agent.sh"
)


def connect_or_exit() -> OmniUI:
    try:
        return OmniUI.connect(app_name="LoginDemo")
    except Exception as exc:
        raise SystemExit(f"{WITH_AGENT_HINT}\n\nConnection error: {exc}") from exc
