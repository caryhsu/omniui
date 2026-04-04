from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- Tree expand/collapse -----------------------------------------------
    # Initially Engineering and Sales depts are collapsed
    assert not client.get_tree_table_expanded("Engineering", id="demoTreeTable"), \
        "Engineering should be collapsed initially"
    print("Engineering initially collapsed ✓")

    result = client.expand_tree_table_item("Engineering", id="demoTreeTable")
    if not result.ok:
        raise SystemExit(f"expand_tree_table_item(Engineering) failed: {result.trace.details}")
    assert client.get_tree_table_expanded("Engineering", id="demoTreeTable"), \
        "Engineering should be expanded"
    print("Expanded Engineering ✓")

    result = client.collapse_tree_table_item("Engineering", id="demoTreeTable")
    if not result.ok:
        raise SystemExit(f"collapse_tree_table_item(Engineering) failed: {result.trace.details}")
    assert not client.get_tree_table_expanded("Engineering", id="demoTreeTable"), \
        "Engineering should be collapsed again"
    print("Collapsed Engineering ✓")

    # ---- Row selection by cell value ----------------------------------------
    # Expand both departments so rows are reachable
    client.expand_tree_table_item("Engineering", id="demoTreeTable")
    client.expand_tree_table_item("Sales", id="demoTreeTable")

    result = client.select_tree_table_row("Alice", id="demoTreeTable")
    if not result.ok:
        raise SystemExit(f"select_tree_table_row(Alice) failed: {result.trace.details}")
    print("Selected row 'Alice' ✓")

    result = client.select_tree_table_row("Carol", column="Name", id="demoTreeTable")
    if not result.ok:
        raise SystemExit(f"select_tree_table_row(Carol, column=Name) failed: {result.trace.details}")
    print("Selected row 'Carol' with column hint ✓")

    # ---- Cell value read ----------------------------------------------------
    result = client.get_tree_table_cell("Bob", "Department", id="demoTreeTable")
    if not result.ok:
        raise SystemExit(f"get_tree_table_cell(Bob, Department) failed: {result.trace.details}")
    assert result.value == "Engineer", f"Expected 'Engineer', got '{result.value}'"
    print(f"Cell Bob/Department = '{result.value}' ✓")

    result = client.get_tree_table_cell("Dave", "Department", id="demoTreeTable")
    if not result.ok:
        raise SystemExit(f"get_tree_table_cell(Dave, Department) failed: {result.trace.details}")
    assert result.value == "Sales Rep", f"Expected 'Sales Rep', got '{result.value}'"
    print(f"Cell Dave/Department = '{result.value}' ✓")

    # ---- Non-existent row fails gracefully ----------------------------------
    result = client.select_tree_table_row("NoSuchPerson", id="demoTreeTable")
    assert not result.ok, "Expected failure for non-existent row"
    print("Non-existent row correctly rejected ✓")

    print("\ntreetableview_demo succeeded ✓")


if __name__ == "__main__":
    main()
