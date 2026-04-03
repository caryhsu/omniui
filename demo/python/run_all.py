from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    import alert_demo  # type: ignore
    import context_menu_demo  # type: ignore
    import date_picker_demo  # type: ignore
    import dialog_demo  # type: ignore
    import discover_advanced_controls  # type: ignore
    import discover_nodes  # type: ignore
    import login_direct  # type: ignore
    import login_with_fallback  # type: ignore
    import menu_bar_demo  # type: ignore
    import progress_demo  # type: ignore
    import radio_toggle_demo  # type: ignore
    import recorder_preview  # type: ignore
    import run_benchmark  # type: ignore
    import select_combo_role  # type: ignore
    import select_list_item  # type: ignore
    import select_table_row  # type: ignore
    import select_tree_item  # type: ignore
    import slider_spinner_demo  # type: ignore
    import checkbox_demo  # type: ignore
    import choicebox_demo  # type: ignore
    import hyperlink_demo  # type: ignore
    import password_field_demo  # type: ignore
    import tab_demo  # type: ignore
    import text_area_demo  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from . import (
        alert_demo,
        context_menu_demo,
        date_picker_demo,
        dialog_demo,
        discover_advanced_controls,
        discover_nodes,
        login_direct,
        login_with_fallback,
        menu_bar_demo,
        progress_demo,
        radio_toggle_demo,
        recorder_preview,
        run_benchmark,
        select_combo_role,
        select_list_item,
        select_table_row,
        select_tree_item,
        slider_spinner_demo,
        checkbox_demo,
        choicebox_demo,
        hyperlink_demo,
        password_field_demo,
        tab_demo,
        text_area_demo,
    )


def _section(title: str) -> None:
    print()
    print(f"=== {title} ===")


def main() -> None:
    _section("Discover Nodes")
    discover_nodes.main()

    _section("Discover Advanced Controls")
    discover_advanced_controls.main()

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

    _section("ContextMenu Demo")
    context_menu_demo.main()

    _section("MenuBar Demo")
    menu_bar_demo.main()

    _section("DatePicker Demo")
    date_picker_demo.main()

    _section("Dialog Demo")
    dialog_demo.main()

    _section("Alert Demo")
    alert_demo.main()

    _section("RadioButton / ToggleButton Demo")
    radio_toggle_demo.main()

    _section("Slider / Spinner Demo")
    slider_spinner_demo.main()

    _section("ProgressBar Demo")
    progress_demo.main()

    _section("TabPane Demo")
    tab_demo.main()

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

    _section("Recorder Preview")
    recorder_preview.main()

    _section("Benchmark")
    run_benchmark.main()


if __name__ == "__main__":
    main()
