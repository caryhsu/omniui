"""Parallel-safe integration tests for the drag-app.

These tests demonstrate the stateless pattern required for pytest-xdist
parallel execution.  Each test:
  1. Resets the app to a known state via ``resetBtn``
  2. Performs its action
  3. Asserts the result
  4. Leaves the app ready for the next test (reset at start, not just end)

Run sequentially:
    pytest tests/test_parallel_example.py

Run in parallel (4 workers):
    pytest tests/test_parallel_example.py --numprocesses 4

See ``tests/conftest_parallel_example.py`` for the fixture definition and
``docs/parallel-testing.md`` for the full setup guide.
"""
from __future__ import annotations

import os
from pathlib import Path

import pytest

from omniui import OmniUI

# ---------------------------------------------------------------------------
# Port + launch helpers (duplicated here so this file is self-contained)
# ---------------------------------------------------------------------------

_ROOT = Path(__file__).resolve().parents[2]
_AGENT_JAR = _ROOT / "java-agent" / "target" / "omniui-java-agent-0.1.0-SNAPSHOT.jar"
_DRAG_APP_BASE_PORT = 49000


def _worker_port(base: int) -> int:
    worker_id = os.environ.get("PYTEST_XDIST_WORKER", "master")
    if worker_id == "master":
        return base
    try:
        return base + int(worker_id[2:])
    except (ValueError, IndexError):
        return base


def _build_drag_cmd(port: int) -> list[str]:
    import platform
    m2 = Path.home() / ".m2" / "repository"
    jfx = "21.0.2"
    gson_ver = "2.11.0"
    clf = "win" if "windows" in platform.system().lower() else ("mac" if "darwin" in platform.system().lower() else "linux")

    def jfx_jar(a: str) -> str:
        return str(m2 / "org" / "openjfx" / a / jfx / f"{a}-{jfx}-{clf}.jar")

    gson = str(m2 / "com" / "google" / "code" / "gson" / "gson" / gson_ver / f"gson-{gson_ver}.jar")
    classes = str(_ROOT / "demo" / "java" / "drag-app" / "target" / "classes")
    return [
        "java", f"-javaagent:{_AGENT_JAR}=port={port}",
        "-p", os.pathsep.join([classes, str(_AGENT_JAR), jfx_jar("javafx-controls"),
                                jfx_jar("javafx-graphics"), jfx_jar("javafx-base"), gson]),
        "-m", "dev.omniui.demo.drag/dev.omniui.demo.drag.DragDropApp",
    ]


# ---------------------------------------------------------------------------
# Session-scoped fixture — one app per xdist worker
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def drag_client():
    port = _worker_port(_DRAG_APP_BASE_PORT)
    cmd = _build_drag_cmd(port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


# ---------------------------------------------------------------------------
# Tests — each begins with a reset so they are order-independent
# ---------------------------------------------------------------------------

def test_drag_apple_to_right(drag_client):
    """Drag Apple to the right panel and verify status."""
    drag_client.click(id="resetBtn")

    result = drag_client.drag(id="item_apple").to(id="rightPanel")
    assert result.ok, f"drag Apple failed: {result}"

    status = drag_client.get_text(id="dragStatus")
    assert status.ok
    assert "dropped Apple" in status.value, f"Unexpected status: {status.value!r}"


def test_drag_banana_to_right(drag_client):
    """Drag Banana to the right panel and verify status."""
    drag_client.click(id="resetBtn")

    result = drag_client.drag(id="item_banana").to(id="rightPanel")
    assert result.ok, f"drag Banana failed: {result}"

    status = drag_client.get_text(id="dragStatus")
    assert status.ok
    assert "dropped Banana" in status.value, f"Unexpected status: {status.value!r}"


def test_reset_restores_all_items(drag_client):
    """After dragging and resetting, all source items are back."""
    # Drag something first
    drag_client.drag(id="item_cherry").to(id="rightPanel")

    # Reset
    drag_client.click(id="resetBtn")

    for item_id in ("item_apple", "item_banana", "item_cherry", "item_date", "item_elderberry"):
        r = drag_client.get_text(id=item_id)
        assert r.ok, f"After reset, expected {item_id} in leftPanel: {r}"

    status = drag_client.get_text(id="dragStatus")
    assert "idle" in status.value, f"Expected idle status after reset: {status.value!r}"
