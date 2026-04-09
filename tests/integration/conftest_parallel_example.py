"""Example: worker-aware conftest for parallel OmniUI tests.

Copy / adapt this file to your project's conftest.py when running tests with
pytest-xdist (``pytest --numprocesses auto``).

How it works
------------
Each xdist worker (``gw0``, ``gw1``, …) is an isolated Python process.
A session-scoped fixture runs once per worker.  By offsetting the port with
the worker index we guarantee that every worker connects to its own JavaFX
app instance — no shared state, no port collisions.

Port assignment
---------------
  DRAG_APP_BASE = 49000
  gw0  → port 49000
  gw1  → port 49001
  gw2  → port 49002
  …
  master (no -n flag) → port 49000   (single process, no offset)

Stateless tests
---------------
Tests that share a session-scoped fixture MUST leave the app in a known
state after each test.  Use a reset button, fixture teardown, or test-level
``autouse`` fixture to clean up.

Usage
-----
1. ``pip install pytest-xdist``
2. ``mvn clean package -pl demo/java/drag-app``
3. ``pytest tests/test_parallel_example.py --numprocesses auto``
"""
from __future__ import annotations

import os
from pathlib import Path

import pytest

from omniui import OmniUI

# ---------------------------------------------------------------------------
# Port helper
# ---------------------------------------------------------------------------

_DRAG_APP_BASE_PORT = 49000


def _worker_port(base: int) -> int:
    """Compute the port for the current xdist worker.

    Reads PYTEST_XDIST_WORKER env var set by xdist (``gw0``, ``gw1``, …).
    Falls back to *base* when running without xdist (``master``).
    """
    worker_id = os.environ.get("PYTEST_XDIST_WORKER", "master")
    if worker_id == "master":
        return base
    try:
        return base + int(worker_id[2:])
    except (ValueError, IndexError):
        return base


# ---------------------------------------------------------------------------
# App launch command builder (drag-app)
# ---------------------------------------------------------------------------

_ROOT = Path(__file__).resolve().parent
_AGENT_JAR = _ROOT / "java-agent" / "target" / "omniui-java-agent-0.1.0-SNAPSHOT.jar"


def _build_drag_app_cmd(port: int) -> list[str]:
    import platform

    m2 = Path.home() / ".m2" / "repository"
    jfx = "24.0.1"
    gson = "2.11.0"
    clf = "win" if "windows" in platform.system().lower() else ("mac" if "darwin" in platform.system().lower() else "linux")

    def jfx_jar(a: str) -> str:
        return str(m2 / "org" / "openjfx" / a / jfx / f"{a}-{jfx}-{clf}.jar")

    gson_jar = str(m2 / "com" / "google" / "code" / "gson" / "gson" / gson / f"gson-{gson}.jar")
    classes = str(_ROOT / "demo" / "java" / "drag-app" / "target" / "classes")

    return [
        "java",
        f"-javaagent:{_AGENT_JAR}=port={port}",
        "-p", os.pathsep.join([
            classes, str(_AGENT_JAR),
            jfx_jar("javafx-controls"), jfx_jar("javafx-graphics"), jfx_jar("javafx-base"),
            gson_jar,
        ]),
        "-m", "dev.omniui.demo.drag/dev.omniui.demo.drag.DragDropApp",
    ]


# ---------------------------------------------------------------------------
# Session-scoped fixture — one app per xdist worker
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def drag_client():
    """Isolated drag-app client for the duration of one xdist worker session.

    Launches drag-app on a worker-unique port; closes it when the session ends.
    Non-parallel runs (no --numprocesses) behave identically using base port.
    """
    port = _worker_port(_DRAG_APP_BASE_PORT)
    cmd = _build_drag_app_cmd(port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client
