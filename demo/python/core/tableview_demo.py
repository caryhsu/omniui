"""Demonstrate TableView operations: get_cell, click_cell, edit_cell, sort_column."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit

TABLE_ID = "userTable"


def main() -> None:
    client = connect_or_exit()

    # --- get_cell ---
    result = client.get_cell(id=TABLE_ID, row=0, column=0)
    assert result.ok, f"get_cell failed: {result}"
    assert result.value == "Ava", f"Expected 'Ava', got {result.value!r}"
    print(f"get_cell(row=0, col=0) = {result.value!r} (ok)")

    result = client.get_cell(id=TABLE_ID, row=1, column=1)
    assert result.ok, f"get_cell row=1 col=1 failed: {result}"
    print(f"get_cell(row=1, col=1) = {result.value!r} (ok)")

    # --- get_cell out-of-bounds ---
    bad = client.get_cell(id=TABLE_ID, row=99, column=0)
    assert not bad.ok, "Expected failure for row out of bounds"
    print("get_cell out-of-bounds returns ok=False (ok)")

    # --- click_cell ---
    result = client.click_cell(id=TABLE_ID, row=0, column=0)
    assert result.ok, f"click_cell failed: {result}"
    print("click_cell(row=0, col=0) (ok)")

    # --- sort_column ---
    result = client.sort_column(id=TABLE_ID, column=0, direction="asc")
    assert result.ok, f"sort_column asc failed: {result}"
    print("sort_column(col=0, direction=asc) (ok)")

    result = client.sort_column(id=TABLE_ID, column=0, direction="desc")
    assert result.ok, f"sort_column desc failed: {result}"
    print("sort_column(col=0, direction=desc) (ok)")

    # --- edit_cell ---
    result = client.edit_cell(id=TABLE_ID, row=0, column=0, value="Alice")
    assert result.ok, f"edit_cell failed: {result}"
    print(f"edit_cell(row=0, col=0, value='Alice') (ok)")

    print("\ntableview_demo succeeded (ok)")


if __name__ == "__main__":
    main()
