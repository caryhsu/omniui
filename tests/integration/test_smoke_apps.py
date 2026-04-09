"""Smoke tests for all OmniUI demo apps.

Each app gets a session-scoped fixture that launches the JavaFX process and
a handful of basic tests that verify core interactions work.

Run all smoke tests:
    pytest tests/integration/test_smoke_apps.py

Run a specific app:
    pytest tests/integration/test_smoke_apps.py -k core
"""
from __future__ import annotations

import os
import platform
import time
from pathlib import Path

import pytest

from omniui import OmniUI

# ---------------------------------------------------------------------------
# Shared helpers
# ---------------------------------------------------------------------------

_ROOT = Path(__file__).resolve().parents[2]
_AGENT_JAR = _ROOT / "java-agent" / "target" / "omniui-java-agent-0.1.0-SNAPSHOT.jar"

# Smoke test ports — distinct from demo (48100-48108) and parallel-example (49000+)
_PORTS = {
    "core":        49100,
    "input":       49101,
    "advanced":    49102,
    "progress":    49104,
    "image":       49105,
    "color":       49106,
    "todo":        49107,
    "login":       49108,
    "settings":    49109,
    "drag":        49110,
    "dynamicfxml": 49111,
    "explorer":    49112,
    "usersearch":  49113,
}


def _clf() -> str:
    s = platform.system().lower()
    if "windows" in s:
        return "win"
    if "darwin" in s:
        return "mac"
    return "linux"


def _build_app_cmd(app_dir: str, module_main: str, port: int, extra_jfx: tuple = ()) -> list[str]:
    """Build the java launch command for a demo app.

    Args:
        app_dir:     Directory name under demo/java/ (e.g. "core-app").
        module_main: Java module/main-class string (e.g.
                     "dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp").
        port:        HTTP port for the OmniUI agent.
        extra_jfx:   Extra JavaFX artifact names to include in the module path
                     (e.g. ``("javafx-fxml",)`` for FXML-based apps).
    """
    m2 = Path.home() / ".m2" / "repository"
    jfx = "26"
    gson_ver = "2.11.0"
    clf = _clf()

    def jfx_jar(artifact: str) -> str:
        return str(m2 / "org" / "openjfx" / artifact / jfx / f"{artifact}-{jfx}-{clf}.jar")

    gson = str(m2 / "com" / "google" / "code" / "gson" / "gson" / gson_ver / f"gson-{gson_ver}.jar")
    classes = str(_ROOT / "demo" / "java" / app_dir / "target" / "classes")
    module_path = os.pathsep.join([
        classes,
        str(_AGENT_JAR),
        jfx_jar("javafx-controls"),
        jfx_jar("javafx-graphics"),
        jfx_jar("javafx-base"),
        gson,
        *[jfx_jar(j) for j in extra_jfx],
    ])
    return [
        "java",
        f"-javaagent:{_AGENT_JAR}=port={port}",
        "-p", module_path,
        "-m", module_main,
    ]


# ---------------------------------------------------------------------------
# Session-scoped fixtures — one JVM per app, shared across all its tests
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def core_client():
    port = _PORTS["core"]
    cmd = _build_app_cmd("core-app", "dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def input_client():
    port = _PORTS["input"]
    cmd = _build_app_cmd("input-app", "dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def advanced_client():
    port = _PORTS["advanced"]
    cmd = _build_app_cmd("advanced-app", "dev.omniui.demo.advanced/dev.omniui.demo.advanced.AdvancedDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def progress_client():
    port = _PORTS["progress"]
    cmd = _build_app_cmd("progress-app", "dev.omniui.demo.progress/dev.omniui.demo.progress.ProgressDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def image_client():
    port = _PORTS["image"]
    cmd = _build_app_cmd("image-app", "dev.omniui.demo.image/dev.omniui.demo.image.ImageDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def color_client():
    port = _PORTS["color"]
    cmd = _build_app_cmd("color-app", "dev.omniui.demo.color/dev.omniui.demo.color.ColorDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def todo_client():
    port = _PORTS["todo"]
    cmd = _build_app_cmd("todo-app", "dev.omniui.demo.todo/dev.omniui.demo.todo.TodoDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def login_client():
    port = _PORTS["login"]
    cmd = _build_app_cmd("login-app", "dev.omniui.demo.login/dev.omniui.demo.login.LoginApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


@pytest.fixture(scope="session")
def settings_client():
    port = _PORTS["settings"]
    cmd = _build_app_cmd(
        "settings-app",
        "dev.omniui.demo.settings/dev.omniui.demo.settings.SettingsApp",
        port,
    )
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


# ---------------------------------------------------------------------------
# core-app smoke tests
# ---------------------------------------------------------------------------

def test_core_server_list_exists(core_client):
    """serverList node is present in the scene graph."""
    nodes = core_client.get_nodes()
    ids = [n.get("fxId") for n in nodes]
    assert "serverList" in ids, f"serverList not found in nodes: {ids}"


def test_core_role_combo_select(core_client):
    """Can select a role from roleCombo."""
    result = core_client.select("Admin", id="roleCombo")
    assert result.ok, f"select(Admin, roleCombo) failed: {result}"


# ---------------------------------------------------------------------------
# input-app smoke tests
# ---------------------------------------------------------------------------

def test_input_checkbox_initial_state(input_client):
    """checkA starts checked, checkB and checkC start unchecked."""
    a = input_client.get_selected(id="checkA")
    b = input_client.get_selected(id="checkB")
    assert a.ok, f"get_selected(checkA) failed: {a}"
    assert b.ok, f"get_selected(checkB) failed: {b}"
    assert a.value is True,  f"checkA should be initially checked, got {a.value}"
    assert b.value is False, f"checkB should be initially unchecked, got {b.value}"


def test_input_checkbox_toggle(input_client):
    """Clicking checkB toggles its state."""
    before = input_client.get_selected(id="checkB")
    input_client.click(id="checkB")
    after = input_client.get_selected(id="checkB")
    assert before.ok and after.ok
    assert after.value != before.value, "checkB state should change after click"
    # restore
    input_client.click(id="checkB")


# ---------------------------------------------------------------------------
# advanced-app smoke tests
# ---------------------------------------------------------------------------

def test_advanced_tabs_exist(advanced_client):
    """demoTabPane has at least 3 tabs."""
    result = advanced_client.get_tabs(id="demoTabPane")
    assert result.ok, f"get_tabs failed: {result}"
    tabs = result.value or []
    assert len(tabs) >= 3, f"Expected ≥3 tabs, got {len(tabs)}: {tabs}"


def test_advanced_tab_select(advanced_client):
    """Can select each tab by title."""
    result = advanced_client.get_tabs(id="demoTabPane")
    assert result.ok
    for tab in (result.value or []):
        r = advanced_client.select_tab(tab["text"], id="demoTabPane")
        assert r.ok, f"select_tab({tab['text']!r}) failed: {r}"


# ---------------------------------------------------------------------------
# progress-app smoke tests
# ---------------------------------------------------------------------------

def test_progress_initial_idle(progress_client):
    """After reset, statusLabel is 'idle'."""
    progress_client.click(id="resetBtn")
    status = progress_client.get_text(id="statusLabel")
    assert status.ok, f"get_text(statusLabel) failed: {status}"
    assert status.value == "idle", f"Expected 'idle', got {status.value!r}"


def test_progress_run_job(progress_client):
    """Run Job completes and progressBar reaches 1.0."""
    progress_client.click(id="resetBtn")
    progress_client.click(id="runBtn")
    # wait_for_text raises TimeoutError on failure; returns None on success
    progress_client.wait_for_text(id="statusLabel", expected="done", timeout=20.0)
    bar = progress_client.get_progress(id="progressBar")
    assert bar.ok, f"get_progress failed: {bar}"
    assert bar.value == pytest.approx(1.0, abs=0.01), f"Expected progress=1.0, got {bar.value}"


# ---------------------------------------------------------------------------
# image-app smoke tests
# ---------------------------------------------------------------------------

def test_image_status_ready(image_client):
    """statusLabel becomes 'ready' once images finish loading."""
    # wait_for_text raises TimeoutError on failure; returns None on success
    image_client.wait_for_text(id="statusLabel", expected="ready", timeout=20.0)


def test_image_switch(image_client):
    """Clicking switchBtn does not crash the app."""
    image_client.wait_for_text(id="statusLabel", expected="ready", timeout=20.0)
    r = image_client.click(id="switchBtn")
    assert r.ok, f"switchBtn click failed: {r}"


# ---------------------------------------------------------------------------
# color-app smoke tests
# ---------------------------------------------------------------------------

def test_color_set_and_read(color_client):
    """set_color updates colorResult label."""
    result = color_client.set_color("#ff5733", id="demoPicker")
    assert result.ok, f"set_color failed: {result}"
    label = color_client.get_text(id="colorResult")
    assert label.ok, f"get_text(colorResult) failed: {label}"
    assert "ff5733" in label.value.lower(), f"Expected 'ff5733' in label, got {label.value!r}"


def test_color_reset(color_client):
    """resetColorButton clears colorResult."""
    color_client.click(id="resetColorButton")
    label = color_client.get_text(id="colorResult")
    assert label.ok, f"get_text(colorResult) after reset failed: {label}"
    assert "ff5733" not in label.value.lower(), f"Expected color cleared, got {label.value!r}"


# ---------------------------------------------------------------------------
# todo-app smoke tests
# ---------------------------------------------------------------------------

def test_todo_clear(todo_client):
    """clearButton resets the task list without error."""
    r = todo_client.click(id="clearButton")
    assert r.ok, f"clearButton failed: {r}"


def test_todo_add_task(todo_client):
    """Add a task via dialog and verify it appears in taskList."""
    todo_client.click(id="clearButton")

    r = todo_client.click(id="addButton")
    assert r.ok, f"addButton failed: {r}"
    time.sleep(0.3)  # wait for Platform.runLater dialog

    todo_client.type("Smoke test task", id="dialogTitleField")
    todo_client.select("Medium", id="dialogPriorityCombo")
    todo_client.set_date("2026-12-31", id="dialogDatePicker")
    todo_client.dismiss_dialog(button="OK")
    time.sleep(0.3)  # wait for list to update

    r = todo_client.select("Smoke test task [Medium] 2026-12-31", id="taskList")
    assert r.ok, f"Task not found in taskList: {r}"

    # cleanup
    todo_client.click(id="clearButton")


# ---------------------------------------------------------------------------
# login-app smoke tests
# ---------------------------------------------------------------------------

def test_login_success(login_client):
    """Type correct credentials and verify login succeeds."""
    login_client.click(id="username")
    login_client.type("admin", id="username")
    login_client.click(id="password")
    login_client.type("1234", id="password")
    login_client.click(id="loginButton")
    result = login_client.verify_text(id="status", expected="Success")
    assert result.ok, f"Login status not 'Success': {result}"


def test_login_failure(login_client):
    """Wrong password shows failure status."""
    login_client.click(id="username")
    login_client.type("admin", id="username")
    login_client.click(id="password")
    login_client.type("wrong", id="password")
    login_client.click(id="loginButton")
    status = login_client.get_text(id="status")
    assert status.ok, f"get_text(status) failed: {status}"
    assert "Success" not in status.value, f"Expected failure, got: {status.value!r}"


# ---------------------------------------------------------------------------
# settings-app smoke tests
# ---------------------------------------------------------------------------

def test_settings_tabs_count(settings_client):
    """settingsTabs has exactly 5 tabs: Account, Appearance, Notifications, Advanced, About."""
    result = settings_client.get_tabs(id="settingsTabs")
    assert result.ok, f"get_tabs(settingsTabs) failed: {result}"
    tabs = result.value or []
    assert len(tabs) == 5, f"Expected 5 tabs, got {len(tabs)}: {tabs}"


def test_settings_reset_defaults(settings_client):
    """Reset button restores usernameField to 'john_doe' and emailField to 'john@example.com'."""
    settings_client.click(id="resetBtn")
    time.sleep(0.3)

    username = settings_client.get_text(id="usernameField")
    assert username.ok, f"get_text(usernameField) failed: {username}"
    assert username.value == "john_doe", f"Expected 'john_doe', got {username.value!r}"

    email = settings_client.get_text(id="emailField")
    assert email.ok, f"get_text(emailField) failed: {email}"
    assert email.value == "john@example.com", f"Expected 'john@example.com', got {email.value!r}"


def test_settings_save_success(settings_client):
    """Matching passwords → Save shows success status."""
    settings_client.click(id="resetBtn")
    time.sleep(0.2)

    settings_client.select_tab("Account", id="settingsTabs")
    settings_client.press_key("Control+A", id="passwordField")
    settings_client.type("Pass1234", id="passwordField")
    settings_client.press_key("Control+A", id="confirmPasswordField")
    settings_client.type("Pass1234", id="confirmPasswordField")
    settings_client.click(id="saveBtn")
    time.sleep(0.2)

    status = settings_client.get_text(id="statusLabel")
    assert status.ok, f"get_text(statusLabel) failed: {status}"
    assert "saved" in status.value.lower(), f"Expected 'saved', got {status.value!r}"


def test_settings_password_mismatch(settings_client):
    """Mismatched passwords → Save shows mismatch error."""
    settings_client.select_tab("Account", id="settingsTabs")
    settings_client.press_key("Control+A", id="passwordField")
    settings_client.type("AAA", id="passwordField")
    settings_client.press_key("Control+A", id="confirmPasswordField")
    settings_client.type("BBB", id="confirmPasswordField")
    settings_client.click(id="saveBtn")
    time.sleep(0.2)

    status = settings_client.get_text(id="statusLabel")
    assert status.ok, f"get_text(statusLabel) failed: {status}"
    assert "match" in status.value.lower(), f"Expected mismatch error, got {status.value!r}"


def test_settings_dnd_toggle(settings_client):
    """DND toggle flips dndStatusLabel between 'DND: Off' and 'DND: On'."""
    settings_client.click(id="resetBtn")  # ensure DND is off
    time.sleep(0.2)

    settings_client.select_tab("Notifications", id="settingsTabs")
    time.sleep(0.2)

    before = settings_client.get_text(id="dndStatusLabel")
    assert before.ok, f"get_text(dndStatusLabel) failed: {before}"
    assert "off" in before.value.lower(), f"Expected DND off after reset, got {before.value!r}"

    settings_client.click(id="dndToggle")
    time.sleep(0.1)
    after = settings_client.get_text(id="dndStatusLabel")
    assert after.ok, f"get_text(dndStatusLabel) after toggle: {after}"
    assert "on" in after.value.lower(), f"Expected DND on, got {after.value!r}"

    # restore
    settings_client.click(id="dndToggle")


def test_settings_proxy_enable(settings_client):
    """Enabling proxy CheckBox makes proxyField accept input."""
    settings_client.click(id="resetBtn")  # ensure proxy is off
    time.sleep(0.2)

    settings_client.select_tab("Advanced", id="settingsTabs")
    time.sleep(0.2)

    # confirm proxy initially disabled (clicking should fail or be a no-op)
    proxy_before = settings_client.get_selected(id="enableProxyCheck")
    assert proxy_before.ok, f"get_selected(enableProxyCheck) failed: {proxy_before}"
    assert proxy_before.value is False, f"Expected proxy off by default, got {proxy_before.value}"

    settings_client.click(id="enableProxyCheck")
    time.sleep(0.1)

    r = settings_client.type("http://proxy.local:8080", id="proxyField")
    assert r.ok, f"type into proxyField failed (should be enabled): {r}"

    # restore
    settings_client.click(id="enableProxyCheck")


# ---------------------------------------------------------------------------
# drag-app smoke tests
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def drag_client():
    port = _PORTS["drag"]
    cmd = _build_app_cmd("drag-app", "dev.omniui.demo.drag/dev.omniui.demo.drag.DragDemoApp", port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


def test_drag_initial_idle(drag_client):
    """dragStatus starts as 'idle' after reset."""
    drag_client.click(id="resetBtn")
    time.sleep(0.2)
    status = drag_client.get_text(id="dragStatus")
    assert status.ok, f"get_text(dragStatus) failed: {status}"
    assert "idle" in status.value, f"Expected 'idle' in dragStatus, got {status.value!r}"


def test_drag_apple_to_right(drag_client):
    """Drag Apple to rightPanel → dragStatus contains 'dropped Apple'."""
    drag_client.click(id="resetBtn")
    time.sleep(0.2)
    result = drag_client.drag(id="item_apple").to(id="rightPanel")
    assert result.ok, f"drag Apple failed: {result}"
    status = drag_client.get_text(id="dragStatus")
    assert status.ok
    assert "dropped Apple" in status.value, f"Expected 'dropped Apple', got {status.value!r}"
    # restore
    drag_client.click(id="resetBtn")


# ---------------------------------------------------------------------------
# dynamic-fxml-app smoke tests
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def dynamicfxml_client():
    port = _PORTS["dynamicfxml"]
    cmd = _build_app_cmd(
        "dynamic-fxml-app",
        "dev.omniui.demo.dynamicfxml/dev.omniui.demo.dynamicfxml.DynamicFxmlApp",
        port,
        extra_jfx=("javafx-fxml",),
    )
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


def test_dynamicfxml_load_form(dynamicfxml_client):
    """formBtn loads the Form view and exposes nameField."""
    r = dynamicfxml_client.click(id="formBtn")
    assert r.ok, f"formBtn click failed: {r}"
    time.sleep(0.3)
    nodes = dynamicfxml_client.get_nodes()
    ids = [n.get("fxId") for n in nodes]
    assert "nameField" in ids, f"nameField not found after loading Form view: {ids}"


def test_dynamicfxml_form_submit(dynamicfxml_client):
    """Fill and submit the Form view → statusLabel shows 'Submitted'."""
    dynamicfxml_client.click(id="formBtn")
    time.sleep(0.3)
    dynamicfxml_client.press_key("Control+A", id="nameField")
    dynamicfxml_client.type("Bob", id="nameField")
    dynamicfxml_client.press_key("Control+A", id="emailField")
    dynamicfxml_client.type("bob@example.com", id="emailField")
    r = dynamicfxml_client.click(id="submitBtn")
    assert r.ok, f"submitBtn click failed: {r}"
    time.sleep(0.2)
    status = dynamicfxml_client.get_text(id="statusLabel")
    assert status.ok, f"get_text(statusLabel) failed: {status}"
    assert "Submitted" in status.value, f"Expected 'Submitted', got {status.value!r}"


def test_dynamicfxml_load_dashboard(dynamicfxml_client):
    """dashboardBtn loads the Dashboard view with numeric usersCountLabel."""
    r = dynamicfxml_client.click(id="dashboardBtn")
    assert r.ok, f"dashboardBtn click failed: {r}"
    time.sleep(0.3)
    count = dynamicfxml_client.get_text(id="usersCountLabel")
    assert count.ok, f"get_text(usersCountLabel) failed: {count}"
    assert count.value.isdigit(), f"Expected numeric usersCountLabel, got {count.value!r}"


# ---------------------------------------------------------------------------
# explorer-app smoke tests
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def explorer_client():
    port = _PORTS["explorer"]
    cmd = _build_app_cmd(
        "explorer-app",
        "dev.omniui.demo.explorer/dev.omniui.demo.explorer.ExplorerApp",
        port,
    )
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


def test_explorer_initial_status(explorer_client):
    """statusBar initially shows count of top-level items."""
    status = explorer_client.get_text(id="statusBar")
    assert status.ok, f"get_text(statusBar) failed: {status}"
    assert "top-level" in status.value, f"Expected 'top-level' in statusBar, got {status.value!r}"


def test_explorer_select_folder(explorer_client):
    """Selecting Documents in folderTree updates statusBar."""
    r = explorer_client.select("Documents", id="folderTree")
    assert r.ok, f"select(Documents) failed: {r}"
    time.sleep(0.3)
    status = explorer_client.get_text(id="statusBar")
    assert status.ok, f"get_text(statusBar) failed: {status}"
    assert "Documents" in status.value, f"Expected 'Documents' in statusBar, got {status.value!r}"


# ---------------------------------------------------------------------------
# user-search-app smoke tests
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def usersearch_client():
    port = _PORTS["usersearch"]
    cmd = _build_app_cmd(
        "user-search-app",
        "dev.omniui.demo.usersearch/dev.omniui.demo.usersearch.UserSearchApp",
        port,
    )
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as client:
        yield client


def test_usersearch_initial_ready(usersearch_client):
    """statusLabel initially contains 'Ready'."""
    status = usersearch_client.get_text(id="statusLabel")
    assert status.ok, f"get_text(statusLabel) failed: {status}"
    assert "Ready" in status.value, f"Expected 'Ready' in statusLabel, got {status.value!r}"


def test_usersearch_search_all(usersearch_client):
    """Search with no filter finds all 25 users across 5 pages."""
    usersearch_client.click(id="searchButton")
    usersearch_client.wait_for_text(id="statusLabel", expected="Found 25 users.", timeout=10.0)
    page = usersearch_client.get_text(id="pageLabel")
    assert page.ok, f"get_text(pageLabel) failed: {page}"
    assert page.value == "Page 1 / 5", f"Expected 'Page 1 / 5', got {page.value!r}"


def test_usersearch_pagination(usersearch_client):
    """Next/prev buttons navigate pages."""
    usersearch_client.click(id="searchButton")
    usersearch_client.wait_for_text(id="statusLabel", expected="Found 25 users.", timeout=10.0)

    usersearch_client.click(id="nextButton")
    time.sleep(0.2)
    page = usersearch_client.get_text(id="pageLabel")
    assert page.ok
    assert page.value == "Page 2 / 5", f"Expected 'Page 2 / 5', got {page.value!r}"

    usersearch_client.click(id="prevButton")
    time.sleep(0.2)
    page = usersearch_client.get_text(id="pageLabel")
    assert page.ok
    assert page.value == "Page 1 / 5", f"Expected back to 'Page 1 / 5', got {page.value!r}"


def test_usersearch_filter_name(usersearch_client):
    """Filtering by name 'Alice' returns exactly 1 result."""
    usersearch_client.press_key("Control+A", id="nameFilter")
    usersearch_client.type("Alice", id="nameFilter")
    usersearch_client.click(id="searchButton")
    usersearch_client.wait_for_text(id="statusLabel", expected="Found 1 user.", timeout=10.0)
    # cleanup
    usersearch_client.press_key("Control+A", id="nameFilter")
    usersearch_client.press_key("Delete", id="nameFilter")

