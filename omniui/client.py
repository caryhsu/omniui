"""Public Python client entry points."""

from __future__ import annotations

import subprocess
import time
from pathlib import Path
from typing import Any
from urllib import request
from urllib.error import URLError

from .core.engine import OmniUIClient, OmniUIProcess
from .ocr_module import SimpleOcrEngine
from .vision_module import SimpleVisionEngine

_DEFAULT_PORT = 48100
_DEFAULT_APP_NAME = "LoginDemo"


class OmniUI:
    """Factory for creating OmniUI client sessions."""

    @staticmethod
    def connect(
        base_url: str = f"http://127.0.0.1:{_DEFAULT_PORT}",
        app_name: str = _DEFAULT_APP_NAME,
        pid: int | None = None,
        ocr_engine: SimpleOcrEngine | None = None,
        vision_engine: SimpleVisionEngine | None = None,
    ) -> OmniUIClient:
        return OmniUIClient.connect(
            base_url=base_url,
            app_name=app_name,
            pid=pid,
            ocr_engine=ocr_engine,
            vision_engine=vision_engine,
        )

    @staticmethod
    def launch(
        *,
        cmd: list[str] | None = None,
        jar: str | Path | None = None,
        agent_jar: str | Path | None = None,
        app_name: str = _DEFAULT_APP_NAME,
        port: int = _DEFAULT_PORT,
        java: str = "java",
        timeout: float = 30.0,
        extra_args: list[str] | None = None,
        ocr_engine: SimpleOcrEngine | None = None,
        vision_engine: SimpleVisionEngine | None = None,
    ) -> OmniUIProcess:
        """Start a JavaFX app with the embedded OmniUI agent and return a connected client.

        You must supply either ``cmd`` (a full command list) or both ``jar``
        and ``agent_jar`` for the convenience shorthand.

        Examples::

            # Convenience — build the command from jar paths
            ui = OmniUI.launch(jar="app.jar", agent_jar="agent.jar", port=48100)

            # Full control
            ui = OmniUI.launch(
                cmd=["java", "-javaagent:agent.jar=port=48100",
                     "-m", "com.example/com.example.App"],
                app_name="MyApp",
                port=48100,
            )

            # Context manager — process is terminated on exit
            with OmniUI.launch(jar="app.jar", agent_jar="agent.jar") as ui:
                ui.click(id="loginBtn")

        Args:
            cmd: Full command list. Mutually exclusive with ``jar`` / ``agent_jar``.
            jar: Path to the application JAR (requires ``agent_jar``).
            agent_jar: Path to the OmniUI Java agent JAR.
            app_name: App name registered with the agent (used for session creation).
            port: HTTP port the agent will listen on.
            java: Path or name of the ``java`` executable.
            timeout: Seconds to wait for the agent to become ready.
            extra_args: Additional JVM or app arguments appended to the command.
            ocr_engine: Optional custom OCR engine.
            vision_engine: Optional custom vision engine.

        Raises:
            ValueError: If neither ``cmd`` nor ``jar``/``agent_jar`` are provided.
            RuntimeError: If the agent does not become reachable within ``timeout`` seconds.
        """
        if cmd is None:
            if jar is None or agent_jar is None:
                raise ValueError("Provide either 'cmd' or both 'jar' and 'agent_jar'.")
            cmd = [
                java,
                f"-javaagent:{agent_jar}=port={port}",
                "-jar", str(jar),
            ]
        if extra_args:
            cmd = list(cmd) + extra_args

        process = subprocess.Popen(
            cmd,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )

        base_url = f"http://127.0.0.1:{port}"
        health_url = f"{base_url}/health"
        deadline = time.monotonic() + timeout
        last_err: Exception | None = None

        while time.monotonic() < deadline:
            if process.poll() is not None:
                raise RuntimeError(
                    f"App process exited unexpectedly (exit code {process.returncode}) "
                    "before the OmniUI agent became reachable."
                )
            try:
                with request.urlopen(health_url, timeout=1) as resp:
                    import json as _json
                    if _json.loads(resp.read())["status"] == "ok":
                        break
            except Exception as exc:
                last_err = exc
            time.sleep(0.25)
        else:
            process.terminate()
            raise RuntimeError(
                f"OmniUI agent did not become ready within {timeout}s "
                f"(last error: {last_err})"
            )

        # Retry session creation: JavaFX scene may still be initialising after
        # the agent HTTP server becomes healthy.
        connect_deadline = time.monotonic() + timeout
        connect_err: Exception | None = None
        while time.monotonic() < connect_deadline:
            if process.poll() is not None:
                raise RuntimeError(
                    f"App process exited unexpectedly (exit code {process.returncode}) "
                    "while waiting for the JavaFX session to become available."
                )
            try:
                client = OmniUIClient.connect(
                    base_url=base_url,
                    app_name=app_name,
                    ocr_engine=ocr_engine,
                    vision_engine=vision_engine,
                )
                break
            except RuntimeError as exc:
                connect_err = exc
                time.sleep(0.5)
        else:
            process.terminate()
            raise RuntimeError(
                f"JavaFX session did not become available within {timeout}s "
                f"(last error: {connect_err})"
            )
        return OmniUIProcess(
            process=process,
            base_url=client.base_url,
            session_id=client.session_id,
            ocr_engine=client.ocr_engine,
            vision_engine=client.vision_engine,
        )

