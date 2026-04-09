from __future__ import annotations

import os
import platform
import sys
from contextlib import nullcontext
from pathlib import Path

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    # Core app demos
    import core.select_combo_role as select_combo_role  # type: ignore
    import core.select_list_item as select_list_item  # type: ignore
    import core.select_tree_item as select_tree_item  # type: ignore
    import core.select_table_row as select_table_row  # type: ignore
    import core.multi_select_demo as multi_select_demo  # type: ignore
    import core.discover_nodes as discover_nodes  # type: ignore
    import core.index_selector_demo as index_selector_demo  # type: ignore
    import core.keyboard_shortcuts_demo as keyboard_shortcuts_demo  # type: ignore
    import core.double_click_demo as double_click_demo  # type: ignore
    import core.flexible_verify_text_demo as flexible_verify_text_demo  # type: ignore
    import core.css_style_demo as css_style_demo  # type: ignore
    import core.recorder_preview as recorder_preview  # type: ignore
    import core.modifier_click_demo as modifier_click_demo  # type: ignore
    import core.focus_demo as focus_demo  # type: ignore
    import core.retry_demo as retry_demo  # type: ignore
    import core.soft_assert_demo as soft_assert_demo  # type: ignore
    import core.clipboard_demo as clipboard_demo  # type: ignore
    import core.click_at_demo as click_at_demo  # type: ignore
    import core.tableview_demo as tableview_demo  # type: ignore
    # Login app demos
    import login.login_direct as login_direct  # type: ignore
    import login.login_with_fallback as login_with_fallback  # type: ignore
    import login.login_demo as login_demo  # type: ignore
    # Input app demos
    import input.text_area_demo as text_area_demo  # type: ignore
    import input.password_field_demo as password_field_demo  # type: ignore
    import input.hyperlink_demo as hyperlink_demo  # type: ignore
    import input.checkbox_demo as checkbox_demo  # type: ignore
    import input.choicebox_demo as choicebox_demo  # type: ignore
    import input.radio_toggle_demo as radio_toggle_demo  # type: ignore
    import input.slider_spinner_demo as slider_spinner_demo  # type: ignore
    import input.color_picker_demo as color_picker_demo  # type: ignore
    import input.date_picker_demo as date_picker_demo  # type: ignore
    # Advanced app demos
    import advanced.context_menu_demo as context_menu_demo  # type: ignore
    import advanced.menu_bar_demo as menu_bar_demo  # type: ignore
    import advanced.dialog_demo as dialog_demo  # type: ignore
    import advanced.alert_demo as alert_demo  # type: ignore
    import advanced.tab_demo as tab_demo  # type: ignore
    import advanced.accordion_demo as accordion_demo  # type: ignore
    import advanced.treetableview_demo as treetableview_demo  # type: ignore
    import advanced.split_pane_demo as split_pane_demo  # type: ignore
    import advanced.node_state_demo as node_state_demo  # type: ignore
    import advanced.scroll_demo as scroll_demo  # type: ignore
    import advanced.tooltip_demo as tooltip_demo  # type: ignore
    import advanced.hover_demo as hover_demo  # type: ignore
    import advanced.toolbar_demo as toolbar_demo  # type: ignore
    import advanced.scrollbar_demo as scrollbar_demo  # type: ignore
    import advanced.pagination_demo as pagination_demo  # type: ignore
    import advanced.window_demo as window_demo  # type: ignore
    import advanced.within_demo as within_demo  # type: ignore
    import advanced.snapshot_diff_demo as snapshot_diff_demo  # type: ignore
    import advanced.recorder_demo as recorder_demo  # type: ignore
    import advanced.discover_advanced_controls as discover_advanced_controls  # type: ignore
    # Drag app demos
    import drag.drag_listview_demo as drag_listview_demo  # type: ignore
    # Progress app demos
    import progress.progress_demo as progress_demo  # type: ignore
    # Image app demos
    import image.image_demo as image_demo  # type: ignore
    # Color app demos
    import color.color_demo as color_demo  # type: ignore
    # Todo app demos
    import todo.todo_demo as todo_demo  # type: ignore
    import settings.settings_demo as settings_demo  # type: ignore
    import dynamicfxml.dynamic_fxml_demo as dynamic_fxml_demo  # type: ignore
    import explorer.explorer_demo as explorer_demo  # type: ignore
    import usersearch.user_search_demo as user_search_demo  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from .core import (
        select_combo_role,
        select_list_item,
        select_tree_item,
        select_table_row,
        multi_select_demo,
        discover_nodes,
        index_selector_demo,
        keyboard_shortcuts_demo,
        double_click_demo,
        flexible_verify_text_demo,
        css_style_demo,
        recorder_preview,
        modifier_click_demo,
        focus_demo,
        retry_demo,
        soft_assert_demo,
        clipboard_demo,
        click_at_demo,
        tableview_demo,
    )
    from .input import (
        text_area_demo,
        password_field_demo,
        hyperlink_demo,
        checkbox_demo,
        choicebox_demo,
        radio_toggle_demo,
        slider_spinner_demo,
        color_picker_demo,
        date_picker_demo,
    )
    from .advanced import (
        context_menu_demo,
        menu_bar_demo,
        dialog_demo,
        alert_demo,
        tab_demo,
        accordion_demo,
        treetableview_demo,
        split_pane_demo,
        node_state_demo,
        scroll_demo,
        tooltip_demo,
        hover_demo,
        toolbar_demo,
        scrollbar_demo,
        pagination_demo,
        window_demo,
        within_demo,
        snapshot_diff_demo,
        recorder_demo,
        discover_advanced_controls,
    )
    from .drag import drag_listview_demo
    from .login import login_direct, login_with_fallback, login_demo
    from .progress import progress_demo
    from .image import image_demo
    from .color import color_demo
    from .todo import todo_demo
    from .settings import settings_demo
    from .dynamicfxml import dynamic_fxml_demo
    from .explorer import explorer_demo
    from .usersearch import user_search_demo

from omniui import OmniUI

ROOT = Path(__file__).resolve().parents[2]
_AGENT_JAR = ROOT / "java-agent" / "target" / "omniui-java-agent-0.1.0-SNAPSHOT.jar"
_M2 = Path.home() / ".m2" / "repository"
_JAVAFX_VERSION = "26"
_GSON_VERSION = "2.11.0"


def _javafx_classifier() -> str:
    s = platform.system().lower()
    if "windows" in s:
        return "win"
    return "mac" if "darwin" in s else "linux"


def _javafx_jar(artifact: str) -> Path:
    c = _javafx_classifier()
    return _M2 / "org" / "openjfx" / artifact / _JAVAFX_VERSION / f"{artifact}-{_JAVAFX_VERSION}-{c}.jar"


def _gson_jar() -> Path:
    return _M2 / "com" / "google" / "code" / "gson" / "gson" / _GSON_VERSION / f"gson-{_GSON_VERSION}.jar"


_HEADLESS: bool = False


def _headless_jvm_args() -> list[str]:
    """Return JVM args for JavaFX headless mode (empty list when disabled).

    Requires JavaFX 26+ (currently using 26).
    """
    if not _HEADLESS:
        return []
    return [
        "-Dglass.platform=Headless",
        "--enable-native-access=javafx.graphics",
    ]


def _build_launch_cmd(
    app_dir: Path,
    launcher: str,
    module: str,
    main_class: str,
    port: int,
    extra_jfx: tuple = (),
) -> list[str]:
    if not _AGENT_JAR.exists():
        raise SystemExit(
            f"Agent JAR not found: {_AGENT_JAR}\n"
            "Run: mvn install -f java-agent/pom.xml"
        )
    headless_args = _headless_jvm_args()
    # Prefer jlink image (built by build_demo_runtime or mvn javafx:jlink)
    for java_name in ("java.exe", "java"):
        jlink_java = app_dir / "target" / launcher / "bin" / java_name
        if jlink_java.exists():
            print(f"  [launch] using jlink image: {jlink_java.relative_to(ROOT)}")
            return [
                str(jlink_java),
                *headless_args,
                f"-javaagent:{_AGENT_JAR}=port={port}",
                "-m", f"{module}/{main_class}",
            ]
    # Fall back to compiled classes (after mvn package or mvn compile)
    classes = app_dir / "target" / "classes"
    if not classes.exists():
        rel = app_dir.relative_to(ROOT)
        raise SystemExit(
            f"Neither jlink image nor compiled classes found for {app_dir.name}.\n"
            f"Run: mvn package -f {rel}/pom.xml"
        )
    print(f"  [launch] using compiled classes: {classes.relative_to(ROOT)}")
    module_path = os.pathsep.join([
        str(classes),
        str(_AGENT_JAR),
        str(_javafx_jar("javafx-controls")),
        str(_javafx_jar("javafx-graphics")),
        str(_javafx_jar("javafx-base")),
        str(_gson_jar()),
        *[str(_javafx_jar(j)) for j in extra_jfx],
    ])
    return [
        "java",
        *headless_args,
        f"-javaagent:{_AGENT_JAR}=port={port}",
        "-p", module_path,
        "-m", f"{module}/{main_class}",
    ]


def _section(title: str) -> None:
    print()
    print(f"=== {title} ===")


def main(auto_launch: bool = True, verbose: bool = False, headless: bool = False) -> None:
    global _HEADLESS
    _HEADLESS = headless
    if headless:
        print("[headless] JavaFX Headless Platform enabled (-Dglass.platform=Headless)")
    java_dir = ROOT / "demo" / "java"

    # ── Core App ──────────────────────────────────────────────────────────────
    _section("Core App demos (port 48100+)")
    if auto_launch:
        core_port = OmniUI.find_free_port(48100, 48999)
        core_cmd = _build_launch_cmd(
            java_dir / "core-app", "omniui-core-demo",
            "dev.omniui.demo.core", "dev.omniui.demo.core.CoreDemoApp", core_port,
        )
        core_ctx = OmniUI.launch(cmd=core_cmd, port=core_port, timeout=30.0)
    else:
        core_ctx = nullcontext()

    with core_ctx:
        if verbose:
            _section("Discover Nodes")
            discover_nodes.main()

        _section("Select ComboBox Role")
        select_combo_role.main()

        _section("Select ListView Item")
        select_list_item.main()

        _section("Select TreeView Item")
        select_tree_item.main()

        _section("Select TableView Row")
        select_table_row.main()

        _section("Double-Click Demo")
        double_click_demo.main()

        _section("Keyboard Shortcuts Demo")
        keyboard_shortcuts_demo.main()

        _section("Index Selector Demo")
        index_selector_demo.main()

        _section("CSS Style Demo")
        css_style_demo.main()

        _section("Flexible verify_text Demo")
        flexible_verify_text_demo.main()

        _section("Multi-select Demo")
        multi_select_demo.main()

        _section("Recorder Preview")
        recorder_preview.main()

        _section("Modifier+Click Demo")
        modifier_click_demo.main()

        _section("Focus Management Demo")
        focus_demo.main()

        _section("Retry Helper Demo")
        retry_demo.main()

        _section("Soft Assert Demo")
        soft_assert_demo.main()

        _section("Clipboard Demo")
        clipboard_demo.main()

        _section("Click-At Demo")
        click_at_demo.main()

        _section("TableView Demo")
        tableview_demo.main()

    # ── Login App ─────────────────────────────────────────────────────────────
    _section("Login App demos (port 48108+)")
    if auto_launch:
        login_port = OmniUI.find_free_port(48108, 48999)
        login_cmd = _build_launch_cmd(
            java_dir / "login-app", "omniui-login-demo",
            "dev.omniui.demo.login", "dev.omniui.demo.login.LoginApp", login_port,
        )
        login_ctx = OmniUI.launch(cmd=login_cmd, port=login_port, timeout=30.0)
    else:
        login_ctx = nullcontext()

    with login_ctx:
        _section("Direct Login")
        login_direct.main()

        _section("Login With Fallback")
        login_with_fallback.main()

    # ── Input App ─────────────────────────────────────────────────────────────
    _section("Input App demos (port 48101+)")
    if auto_launch:
        input_port = OmniUI.find_free_port(48101, 48999)
        input_cmd = _build_launch_cmd(
            java_dir / "input-app", "omniui-input-demo",
            "dev.omniui.demo.input", "dev.omniui.demo.input.InputDemoApp", input_port,
        )
        input_ctx = OmniUI.launch(cmd=input_cmd, port=input_port, timeout=30.0)
    else:
        input_ctx = nullcontext()

    with input_ctx:
        _section("TextArea Demo")
        text_area_demo.main()

        _section("PasswordField Demo")
        password_field_demo.main()

        _section("Hyperlink Demo")
        hyperlink_demo.main()

        _section("CheckBox Demo")
        checkbox_demo.main()

        _section("ChoiceBox Demo")
        choicebox_demo.main()

        _section("RadioButton / ToggleButton Demo")
        radio_toggle_demo.main()

        _section("Slider / Spinner Demo")
        slider_spinner_demo.main()

        _section("ColorPicker Demo")
        color_picker_demo.main()

        _section("DatePicker Demo")
        date_picker_demo.main()

    # ── Advanced App ──────────────────────────────────────────────────────────
    _section("Advanced App demos (port 48102+)")
    if auto_launch:
        adv_port = OmniUI.find_free_port(48102, 48999)
        adv_cmd = _build_launch_cmd(
            java_dir / "advanced-app", "omniui-advanced-demo",
            "dev.omniui.demo.advanced", "dev.omniui.demo.advanced.AdvancedDemoApp", adv_port,
        )
        adv_ctx = OmniUI.launch(cmd=adv_cmd, port=adv_port, timeout=30.0)
    else:
        adv_ctx = nullcontext()

    with adv_ctx:
        if verbose:
            _section("Discover Advanced Controls")
            discover_advanced_controls.main()

        _section("ContextMenu Demo")
        context_menu_demo.main()

        _section("MenuBar Demo")
        menu_bar_demo.main()

        _section("Dialog Demo")
        dialog_demo.main()

        _section("Alert Demo")
        alert_demo.main()

        _section("TabPane Demo")
        tab_demo.main()

        _section("Accordion Demo")
        accordion_demo.main()

        _section("TreeTableView Demo")
        treetableview_demo.main()

        _section("SplitPane Demo")
        split_pane_demo.main()

        _section("Node State Demo")
        node_state_demo.main()

        _section("Scroll Demo")
        scroll_demo.main()

        _section("Tooltip Demo")
        tooltip_demo.main()

        _section("Hover Demo")
        hover_demo.main()

        _section("ToolBar Demo")
        toolbar_demo.main()

        _section("ScrollBar Demo")
        scrollbar_demo.main()

        _section("Pagination Demo")
        pagination_demo.main()

        _section("Window Demo")
        window_demo.main()

        _section("Within Demo")
        within_demo.main()

        _section("Snapshot/Diff Demo")
        snapshot_diff_demo.main()

        _section("Recorder Demo")
        recorder_demo.main()

    # ── Drag App ──────────────────────────────────────────────────────────────
    _section("Drag App demos (port 48103+)")
    if auto_launch:
        drag_port = OmniUI.find_free_port(48103, 48999)
        drag_cmd = _build_launch_cmd(
            java_dir / "drag-app", "omniui-drag-demo",
            "dev.omniui.demo.drag", "dev.omniui.demo.drag.DragDropApp", drag_port,
        )
        drag_ctx = OmniUI.launch(cmd=drag_cmd, port=drag_port, timeout=30.0)
    else:
        drag_ctx = nullcontext()

    with drag_ctx:
        _section("Drag ListView Demo")
        drag_listview_demo.main()

    # ── Progress App ───────────────────────────────────────────────────────────
    _section("Progress App demos (port 48104+)")
    if auto_launch:
        progress_port = OmniUI.find_free_port(48104, 48999)
        progress_cmd = _build_launch_cmd(
            java_dir / "progress-app", "omniui-progress-demo",
            "dev.omniui.demo.progress", "dev.omniui.demo.progress.ProgressDemoApp", progress_port,
        )
        progress_ctx = OmniUI.launch(cmd=progress_cmd, port=progress_port, timeout=30.0)
    else:
        progress_ctx = nullcontext()

    with progress_ctx:
        _section("Progress Demo")
        progress_demo.main()

    # ── Image App ───────────────────────────────────────────────────────────────
    _section("Image App demos (port 48105+)")
    if auto_launch:
        image_port = OmniUI.find_free_port(48105, 48999)
        image_cmd = _build_launch_cmd(
            java_dir / "image-app", "omniui-image-demo",
            "dev.omniui.demo.image", "dev.omniui.demo.image.ImageDemoApp", image_port,
        )
        image_ctx = OmniUI.launch(cmd=image_cmd, port=image_port, timeout=30.0)
    else:
        image_ctx = nullcontext()

    with image_ctx:
        _section("Image Demo")
        image_demo.main()

    # ── Color App ────────────────────────────────────────────────────────────
    _section("Color App demos (port 48106+)")
    if auto_launch:
        color_port = OmniUI.find_free_port(48106, 48999)
        color_cmd = _build_launch_cmd(
            java_dir / "color-app", "omniui-color-demo",
            "dev.omniui.demo.color", "dev.omniui.demo.color.ColorDemoApp", color_port,
        )
        color_ctx = OmniUI.launch(cmd=color_cmd, port=color_port, timeout=30.0)
    else:
        color_ctx = nullcontext()

    with color_ctx:
        _section("Color Demo")
        color_demo.main()

    # ── Todo App ─────────────────────────────────────────────────────────────
    _section("Todo App demos (port 48107+)")
    if auto_launch:
        todo_port = OmniUI.find_free_port(48107, 48999)
        todo_cmd = _build_launch_cmd(
            java_dir / "todo-app", "omniui-todo-demo",
            "dev.omniui.demo.todo", "dev.omniui.demo.todo.TodoDemoApp", todo_port,
        )
        todo_ctx = OmniUI.launch(cmd=todo_cmd, port=todo_port, timeout=30.0)
    else:
        todo_ctx = nullcontext()

    with todo_ctx:
        _section("Todo Demo")
        todo_demo.main()

    # ── Settings App ──────────────────────────────────────────────────────────
    _section("Settings App demos (port 48112+)")
    if auto_launch:
        settings_port = OmniUI.find_free_port(48112, 48999)
        settings_cmd = _build_launch_cmd(
            java_dir / "settings-app", "omniui-settings-demo",
            "dev.omniui.demo.settings", "dev.omniui.demo.settings.SettingsApp", settings_port,
        )
        settings_ctx = OmniUI.launch(cmd=settings_cmd, port=settings_port, timeout=30.0)
    else:
        settings_ctx = nullcontext()

    with settings_ctx:
        _section("Settings Demo")
        settings_demo.main()

    # ── Dynamic FXML App ───────────────────────────────────────────────────────
    _section("Dynamic FXML App demos (port 48110+)")
    if auto_launch:
        dynamicfxml_port = OmniUI.find_free_port(48110, 48999)
        dynamicfxml_cmd = _build_launch_cmd(
            java_dir / "dynamic-fxml-app", "omniui-dynamicfxml-demo",
            "dev.omniui.demo.dynamicfxml", "dev.omniui.demo.dynamicfxml.DynamicFxmlApp",
            dynamicfxml_port,
            extra_jfx=("javafx-fxml",),
        )
        dynamicfxml_ctx = OmniUI.launch(cmd=dynamicfxml_cmd, port=dynamicfxml_port, timeout=30.0)
    else:
        dynamicfxml_ctx = nullcontext()

    with dynamicfxml_ctx:
        _section("Dynamic FXML Demo")
        dynamic_fxml_demo.main()

    # ── Explorer App ───────────────────────────────────────────────────────────
    _section("Explorer App demos (port 48111+)")
    if auto_launch:
        explorer_port = OmniUI.find_free_port(48111, 48999)
        explorer_cmd = _build_launch_cmd(
            java_dir / "explorer-app", "omniui-explorer-demo",
            "dev.omniui.demo.explorer", "dev.omniui.demo.explorer.ExplorerApp",
            explorer_port,
        )
        explorer_ctx = OmniUI.launch(cmd=explorer_cmd, port=explorer_port, timeout=30.0)
    else:
        explorer_ctx = nullcontext()

    with explorer_ctx:
        _section("Explorer Demo")
        explorer_demo.main()

    # ── User Search App ────────────────────────────────────────────────────────
    _section("User Search App demos (port 48109+)")
    if auto_launch:
        usersearch_port = OmniUI.find_free_port(48109, 48999)
        usersearch_cmd = _build_launch_cmd(
            java_dir / "user-search-app", "omniui-usersearch-demo",
            "dev.omniui.demo.usersearch", "dev.omniui.demo.usersearch.UserSearchApp",
            usersearch_port,
        )
        usersearch_ctx = OmniUI.launch(cmd=usersearch_cmd, port=usersearch_port, timeout=30.0)
    else:
        usersearch_ctx = nullcontext()

    with usersearch_ctx:
        _section("User Search Demo")
        user_search_demo.main()


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="Run all OmniUI demo apps and smoke tests.",
    )
    parser.add_argument(
        "--no-launch", action="store_true",
        help="Don't auto-launch Java apps (assumes they are already running)",
    )
    parser.add_argument(
        "--verbose", action="store_true",
        help="Show detailed output including Discover Nodes JSON dump",
    )
    parser.add_argument(
        "--headless", action="store_true",
        help="Run JavaFX apps in headless mode (requires JavaFX 26+)",
    )
    args = parser.parse_args()
    if args.no_launch and args.headless:
        parser.error("--no-launch and --headless cannot be used together "
                     "(--headless injects JVM args at launch time)")
    main(auto_launch=not args.no_launch, verbose=args.verbose, headless=args.headless)
