from __future__ import annotations

import os
import platform
import sys
from contextlib import nullcontext
from pathlib import Path

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    # Core app demos
    import core.login_direct as login_direct  # type: ignore
    import core.login_with_fallback as login_with_fallback  # type: ignore
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
    import advanced.progress_demo as progress_demo  # type: ignore
    import advanced.node_state_demo as node_state_demo  # type: ignore
    import advanced.scroll_demo as scroll_demo  # type: ignore
    import advanced.tooltip_demo as tooltip_demo  # type: ignore
    import advanced.hover_demo as hover_demo  # type: ignore
    import advanced.discover_advanced_controls as discover_advanced_controls  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from .core import (
        login_direct,
        login_with_fallback,
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
        progress_demo,
        node_state_demo,
        scroll_demo,
        tooltip_demo,
        hover_demo,
        discover_advanced_controls,
    )

from omniui import OmniUI

ROOT = Path(__file__).resolve().parents[2]
_AGENT_JAR = ROOT / "java-agent" / "target" / "omniui-java-agent-0.1.0-SNAPSHOT.jar"
_M2 = Path.home() / ".m2" / "repository"
_JAVAFX_VERSION = "21.0.2"
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


def _build_launch_cmd(app_dir: Path, launcher: str, module: str, main_class: str, port: int) -> list[str]:
    if not _AGENT_JAR.exists():
        raise SystemExit(
            f"Agent JAR not found: {_AGENT_JAR}\n"
            "Run: mvn install -f java-agent/pom.xml"
        )
    # Prefer jlink image (built by build_demo_runtime or mvn javafx:jlink)
    for java_name in ("java.exe", "java"):
        jlink_java = app_dir / "target" / launcher / "bin" / java_name
        if jlink_java.exists():
            print(f"  [launch] using jlink image: {jlink_java.relative_to(ROOT)}")
            return [
                str(jlink_java),
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
    ])
    return [
        "java",
        f"-javaagent:{_AGENT_JAR}=port={port}",
        "-p", module_path,
        "-m", f"{module}/{main_class}",
    ]


def _section(title: str) -> None:
    print()
    print(f"=== {title} ===")


def main(auto_launch: bool = True) -> None:
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

        _section("Direct Login")
        login_direct.main()

        _section("Login With Fallback")
        login_with_fallback.main()

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

        _section("ProgressBar Demo")
        progress_demo.main()

        _section("Node State Demo")
        node_state_demo.main()

        _section("Scroll Demo")
        scroll_demo.main()

        _section("Tooltip Demo")
        tooltip_demo.main()

        _section("Hover Demo")
        hover_demo.main()


if __name__ == "__main__":
    auto_launch = "--no-launch" not in sys.argv
    main(auto_launch=auto_launch)
