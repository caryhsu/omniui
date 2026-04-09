"""Project-wide pytest fixtures for OmniUI integration tests.

Usage
-----
Any test that needs a live OmniUI client declares the ``omniui`` fixture::

    def test_login(omniui):
        omniui.click(id="loginBtn")
        omniui.verify_text(id="status", expected="Welcome")

Configuration
-------------
Pass connection options on the command line or in ``pyproject.toml``::

    pytest --omniui-url http://127.0.0.1:48100 --omniui-app LoginDemo

Or in pyproject.toml::

    [tool.pytest.ini_options]
    omniui_url = "http://127.0.0.1:48100"
    omniui_app = "LoginDemo"

HTML Report
-----------
Run with ``--html=report.html --self-contained-html`` to generate an HTML
report.  On failure, a screenshot is automatically embedded in the report::

    pytest --html=report.html --self-contained-html

On failure a PNG screenshot is saved to ``screenshots/<test_name>.png``
automatically.
"""

from __future__ import annotations

import base64
import os
import pytest

from omniui import OmniUI


# ---------------------------------------------------------------------------
# Parallel helper — xdist worker_id → port
# ---------------------------------------------------------------------------

def _worker_port(worker_id: str, base_port: int) -> int:
    """Return a per-worker port derived from xdist *worker_id*.

    * ``"master"``  — non-parallel run; use *base_port* unchanged.
    * ``"gw0"``, ``"gw1"``, … — offset by worker index (gw0 → +0, gw1 → +1 …).
    """
    if worker_id == "master":
        return base_port
    try:
        return base_port + int(worker_id[2:])
    except (ValueError, IndexError):
        return base_port


# ---------------------------------------------------------------------------
# Session-scoped fixture that launches one app per xdist worker
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def omniui_parallel(request: pytest.FixtureRequest, tmp_path_factory):
    """Session-scoped OmniUIClient that launches an isolated JavaFX app.

    Designed for use with pytest-xdist.  Each worker gets a unique port so
    that multiple workers run fully independently without sharing state.

    Usage in conftest.py (project-level)::

        @pytest.fixture(scope="session")
        def client(omniui_parallel):
            return omniui_parallel

    Worker port assignment:
        BASE_PORT = 49000  (configurable via ``omniui_parallel_base_port`` ini)
        gw0 → 49000, gw1 → 49001, …, master → 49000
    """
    base_port: int = int(
        request.config.getini("omniui_parallel_base_port")
        if "omniui_parallel_base_port" in (request.config.inicfg or {})
        else 49000
    )
    worker_id: str = os.environ.get("PYTEST_XDIST_WORKER", "master")
    port = _worker_port(worker_id, base_port)

    from pathlib import Path
    import platform

    root = Path(__file__).parent
    java_dir = root / "demo" / "java"
    agent_jar = root / "java-agent" / "target" / "omniui-java-agent-0.1.0-SNAPSHOT.jar"

    def _javafx_classifier() -> str:
        s = platform.system().lower()
        if "windows" in s:
            return "win"
        return "mac" if "darwin" in s else "linux"

    m2 = Path.home() / ".m2" / "repository"
    jfx_ver = "24.0.1"
    gson_ver = "2.11.0"
    clf = _javafx_classifier()

    def _jfx(artifact: str) -> str:
        return str(m2 / "org" / "openjfx" / artifact / jfx_ver / f"{artifact}-{jfx_ver}-{clf}.jar")

    gson = str(m2 / "com" / "google" / "code" / "gson" / "gson" / gson_ver / f"gson-{gson_ver}.jar")

    drag_app = java_dir / "drag-app"
    classes = drag_app / "target" / "classes"

    cmd = [
        "java",
        f"-javaagent:{agent_jar}=port={port}",
        "-p", os.pathsep.join([
            str(classes), str(agent_jar),
            _jfx("javafx-controls"), _jfx("javafx-graphics"), _jfx("javafx-base"), gson,
        ]),
        "-m", "dev.omniui.demo.drag/dev.omniui.demo.drag.DragDropApp",
    ]

    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


# ---------------------------------------------------------------------------
# CLI options
# ---------------------------------------------------------------------------

def pytest_addoption(parser: pytest.Parser) -> None:
    group = parser.getgroup("omniui", "OmniUI connection options")
    group.addoption(
        "--omniui-url",
        default=None,
        help="Base URL of the OmniUI agent (default: http://127.0.0.1:48100)",
    )
    group.addoption(
        "--omniui-app",
        default=None,
        help="App name registered with the OmniUI agent (default: LoginDemo)",
    )
    parser.addini("omniui_url", default="http://127.0.0.1:48100", help="OmniUI agent base URL")
    parser.addini("omniui_app", default="LoginDemo", help="App name registered with the OmniUI agent")


# ---------------------------------------------------------------------------
# pytest-html title
# ---------------------------------------------------------------------------

def pytest_html_report_title(report) -> None:  # type: ignore[no-untyped-def]
    report.title = "OmniUI Test Report"


# ---------------------------------------------------------------------------
# Failure report hook — stores per-item call outcome for the fixture
# ---------------------------------------------------------------------------

@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item: pytest.Item, call: pytest.CallInfo) -> None:  # type: ignore[override]
    outcome = yield
    report = outcome.get_result()
    if call.when == "call":
        item._omniui_failed = report.failed  # type: ignore[attr-defined]
        # Attach screenshot to pytest-html report if available
        if report.failed:
            png_bytes: bytes | None = getattr(item, "_omniui_screenshot", None)
            if png_bytes is not None:
                pytest_html = item.config.pluginmanager.getplugin("html")
                if pytest_html is not None:
                    extras = getattr(report, "extras", [])
                    extras.append(
                        pytest_html.extras.image(
                            base64.b64encode(png_bytes).decode(),
                            mime_type="image/png",
                        )
                    )
                    report.extras = extras


# ---------------------------------------------------------------------------
# Core fixture
# ---------------------------------------------------------------------------

@pytest.fixture
def omniui(request: pytest.FixtureRequest):
    """Connected ``OmniUIClient`` for the duration of one test.

    Teardown:
    * On failure — saves a screenshot to ``screenshots/<test_name>.png``.
    * Always    — calls ``client.disconnect()``.
    """
    base_url: str = (
        request.config.getoption("--omniui-url")
        or request.config.getini("omniui_url")
        or "http://127.0.0.1:48100"
    )
    app_name: str = (
        request.config.getoption("--omniui-app")
        or request.config.getini("omniui_app")
        or "LoginDemo"
    )

    client = OmniUI.connect(base_url=base_url, app_name=app_name)

    yield client

    # --- teardown ---
    failed: bool = getattr(request.node, "_omniui_failed", False)
    if failed:
        try:
            print(f"\n{client.format_trace()}")
        except Exception:
            pass
        try:
            png = client.screenshot()
            # Store for pytest-html hook
            request.node._omniui_screenshot = png  # type: ignore[attr-defined]
            out_dir = "screenshots"
            os.makedirs(out_dir, exist_ok=True)
            safe_name = request.node.name.replace("/", "_").replace("\\", "_")
            path = os.path.join(out_dir, f"{safe_name}.png")
            with open(path, "wb") as f:
                f.write(png)
            print(f"\n📸  Screenshot saved → {path}")
        except Exception:
            pass

    client.disconnect()
