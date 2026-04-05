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
