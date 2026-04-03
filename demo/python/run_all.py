from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    import discover_advanced_controls  # type: ignore
    import discover_nodes  # type: ignore
    import login_direct  # type: ignore
    import login_with_fallback  # type: ignore
    import recorder_preview  # type: ignore
    import run_benchmark  # type: ignore
    import select_combo_role  # type: ignore
    import select_list_item  # type: ignore
    import select_table_row  # type: ignore
    import select_tree_item  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from . import (
        discover_advanced_controls,
        discover_nodes,
        login_direct,
        login_with_fallback,
        recorder_preview,
        run_benchmark,
        select_combo_role,
        select_list_item,
        select_table_row,
        select_tree_item,
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

    _section("Recorder Preview")
    recorder_preview.main()

    _section("Benchmark")
    run_benchmark.main()


if __name__ == "__main__":
    main()
