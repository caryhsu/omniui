"""Public Python client entry points."""

from __future__ import annotations

import socket
import subprocess
import time
from pathlib import Path
from urllib import request

from .core.engine import OmniUIClient, OmniUIProcess
from .ocr_module import SimpleOcrEngine
from .vision_module import SimpleVisionEngine

_DEFAULT_APP_NAME = "OmniUIApp"
_SCAN_START = 48100
_SCAN_END = 48999


class OmniUI:
    """Factory for creating OmniUI client sessions."""

    @staticmethod
    def find_free_port(start: int = _SCAN_START, end: int = _SCAN_END) -> int:
        """Return the first available TCP port in [start, end].

        Useful for picking a port before launching a Java app, so that port
        conflicts in CI/CD environments are avoided automatically.

        Raises:
            RuntimeError: If no free port is found in the given range.
        """
        for p in range(start, end + 1):
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                try:
                    s.bind(("127.0.0.1", p))
                    return p
                except OSError:
                    continue
        raise RuntimeError(f"No free port found in range {start}-{end}")

    @staticmethod
    def find_agents(start: int = _SCAN_START, end: int = _SCAN_END) -> list[int]:
        """Return ports in [start, end] where an OmniUI agent is responding.

        Scans each port for a healthy ``/health`` endpoint. Ports that are
        closed or return an unexpected response are silently skipped.
        """
        import json as _json
        active: list[int] = []
        for p in range(start, end + 1):
            try:
                with request.urlopen(f"http://127.0.0.1:{p}/health", timeout=0.2) as resp:
                    if _json.loads(resp.read()).get("status") == "ok":
                        active.append(p)
            except Exception:
                continue
        return active

    @staticmethod
    def connect(
        base_url: str | None = None,
        port: int | None = None,
        app_name: str = _DEFAULT_APP_NAME,
        pid: int | None = None,
        ocr_engine: SimpleOcrEngine | None = None,
        vision_engine: SimpleVisionEngine | None = None,
        scan_range: tuple[int, int] = (_SCAN_START, _SCAN_END),
    ) -> OmniUIClient:
        """Connect to a running OmniUI agent.

        Port resolution order:

        1. ``port`` → ``http://127.0.0.1:<port>``
        2. ``base_url`` → used as-is
        3. neither → scan ``scan_range`` for the first active agent

        Examples::

            # Explicit port
            ui = OmniUI.connect(port=48100)

            # Auto-scan (finds the first running agent in 48100-48999)
            ui = OmniUI.connect()

            # Legacy base_url style
            ui = OmniUI.connect(base_url="http://127.0.0.1:48100")
        """
        if port is not None:
            resolved = f"http://127.0.0.1:{port}"
        elif base_url is not None:
            resolved = base_url
        else:
            agents = OmniUI.find_agents(scan_range[0], scan_range[1])
            if not agents:
                raise RuntimeError(
                    f"No OmniUI agent found in port range {scan_range[0]}-{scan_range[1]}. "
                    "Start a JavaFX app with the OmniUI Java agent enabled."
                )
            resolved = f"http://127.0.0.1:{agents[0]}"
        return OmniUIClient.connect(
            base_url=resolved,
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
        port: int | None = None,
        java: str = "java",
        timeout: float = 30.0,
        extra_args: list[str] | None = None,
        ocr_engine: SimpleOcrEngine | None = None,
        vision_engine: SimpleVisionEngine | None = None,
    ) -> OmniUIProcess:
        """Start a JavaFX app with the embedded OmniUI agent and return a connected client.

        You must supply either ``cmd`` (a full command list) or both ``jar``
        and ``agent_jar`` for the convenience shorthand.

        When using the ``jar``/``agent_jar`` shorthand, ``port=None`` (the
        default) automatically picks a free port in the scan range so that
        CI/CD runs never conflict on hardcoded ports.

        When using ``cmd``, the port must match the ``-javaagent:...=port=N``
        argument already embedded in the command.  Pass ``port=None`` only if
        you built the command with :meth:`find_free_port` first.

        Examples::

            # Auto-assign port (jar shorthand)
            ui = OmniUI.launch(jar="app.jar", agent_jar="agent.jar")

            # Explicit port (jar shorthand)
            ui = OmniUI.launch(jar="app.jar", agent_jar="agent.jar", port=48100)

            # Full control — port must match the value embedded in cmd
            port = OmniUI.find_free_port()
            cmd = ["java", f"-javaagent:agent.jar=port={port}", "-jar", "app.jar"]
            with OmniUI.launch(cmd=cmd, port=port) as ui:
                ui.click(id="loginBtn")

        Args:
            cmd: Full command list. Mutually exclusive with ``jar`` / ``agent_jar``.
            jar: Path to the application JAR (requires ``agent_jar``).
            agent_jar: Path to the OmniUI Java agent JAR.
            app_name: App name registered with the agent (used for session creation).
            port: HTTP port the agent will listen on. ``None`` auto-selects a
                free port (only supported for the ``jar``/``agent_jar`` shorthand;
                required when ``cmd`` is provided).
            java: Path or name of the ``java`` executable.
            timeout: Seconds to wait for the agent to become ready.
            extra_args: Additional JVM or app arguments appended to the command.
            ocr_engine: Optional custom OCR engine.
            vision_engine: Optional custom vision engine.

        Raises:
            ValueError: If neither ``cmd`` nor ``jar``/``agent_jar`` are provided,
                or if ``cmd`` is provided without an explicit ``port``.
            RuntimeError: If the agent does not become reachable within ``timeout`` seconds.
        """
        if cmd is None:
            if jar is None or agent_jar is None:
                raise ValueError("Provide either 'cmd' or both 'jar' and 'agent_jar'.")
            if port is None:
                port = OmniUI.find_free_port()
            cmd = [
                java,
                f"-javaagent:{agent_jar}=port={port}",
                "-jar", str(jar),
            ]
        else:
            if port is None:
                raise ValueError(
                    "When providing 'cmd', you must also supply 'port' matching "
                    "the -javaagent port embedded in the command. "
                    "Use OmniUI.find_free_port() to pick a free port first."
                )
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

