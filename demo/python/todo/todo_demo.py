"""Demonstrate full CRUD workflow on the todo-app.

Scenario:
- Add "Buy groceries" (High, 2026-04-30) → verify in list
- Add "Call doctor" (Medium, 2026-05-01) → verify in list
- Select "Buy groceries" → verify bottom panel pre-fills with title
- Edit title to "Buy groceries & vegetables" → assert list updated
- Click check_1 (Call doctor) → assert it disappears (showCompleted is off)
- Type "Buy" in searchField → assert "Buy groceries & vegetables" visible
- Clear searchField → assert it is still visible
- Delete "Buy groceries & vegetables" → assert removed
- Toggle showCompleted → assert "Call doctor" now visible
"""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── Reset state (idempotent across multiple runs) ─────────────────────────
    r = client.click(id="clearButton")
    assert r.ok, f"clearButton failed: {r}"

    # ── Add task 1 ────────────────────────────────────────────────────────────
    client.press_key("Control+A", id="taskTitleField")
    client.type("Buy groceries", id="taskTitleField")
    r = client.select("High", id="priorityCombo")
    assert r.ok, f"select priorityCombo High: {r}"
    r = client.set_date("2026-04-30", id="dueDatePicker")
    assert r.ok, f"set_date 2026-04-30: {r}"
    r = client.click(id="addButton")
    assert r.ok, f"click addButton: {r}"

    r = client.select("Buy groceries [High] 2026-04-30", id="taskList")
    assert r.ok, f"'Buy groceries' not found in taskList: {r}"
    print("added 'Buy groceries [High] 2026-04-30' (ok)")

    # ── Add task 2 ────────────────────────────────────────────────────────────
    client.press_key("Control+A", id="taskTitleField")
    client.type("Call doctor", id="taskTitleField")
    r = client.select("Medium", id="priorityCombo")
    assert r.ok, f"select priorityCombo Medium: {r}"
    r = client.set_date("2026-05-01", id="dueDatePicker")
    assert r.ok, f"set_date 2026-05-01: {r}"
    r = client.click(id="addButton")
    assert r.ok, f"click addButton (task 2): {r}"

    r = client.select("Call doctor [Medium] 2026-05-01", id="taskList")
    assert r.ok, f"'Call doctor' not found in taskList: {r}"
    print("added 'Call doctor [Medium] 2026-05-01' (ok)")

    # ── Select "Buy groceries" → verify pre-fill ──────────────────────────────
    r = client.select("Buy groceries [High] 2026-04-30", id="taskList")
    assert r.ok, f"select 'Buy groceries': {r}"

    title_val = client.get_text(id="taskTitleField")
    assert title_val.ok and "Buy groceries" in title_val.value, (
        f"Expected taskTitleField to contain 'Buy groceries', got: {title_val.value!r}"
    )
    print(f"pre-fill taskTitleField = {title_val.value!r} (ok)")

    # ── Edit title ────────────────────────────────────────────────────────────
    client.press_key("Control+A", id="taskTitleField")
    client.type("Buy groceries & vegetables", id="taskTitleField")
    r = client.click(id="editButton")
    assert r.ok, f"click editButton: {r}"

    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert r.ok, f"Edited title not found in taskList: {r}"
    print("edit title → 'Buy groceries & vegetables [High] 2026-04-30' (ok)")

    # ── Check "Call doctor" (index 1) ─────────────────────────────────────────
    # First, click somewhere neutral to deselect and ensure index 1 is visible
    r = client.select("Call doctor [Medium] 2026-05-01", id="taskList")
    assert r.ok, f"select Call doctor before check: {r}"

    r = client.click(id="check_1")
    assert r.ok, f"click check_1 (Call doctor): {r}"
    # After toggle, showCompleted=off → Call doctor disappears
    r = client.select("Call doctor [Medium] 2026-05-01", id="taskList")
    assert not r.ok, (
        f"Expected 'Call doctor' to be hidden after completing, but select succeeded"
    )
    print("check_1 toggled → 'Call doctor' hidden (completed, showCompleted=off) (ok)")

    # ── Search filter ─────────────────────────────────────────────────────────
    r = client.press_key("Control+A", id="searchField")
    client.type("Buy", id="searchField")
    import time; time.sleep(0.3)

    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert r.ok, f"'Buy groceries & vegetables' not found during search: {r}"
    print("search 'Buy' → 'Buy groceries & vegetables' visible (ok)")

    # ── Clear search ──────────────────────────────────────────────────────────
    client.press_key("Control+A", id="searchField")
    client.press_key("Delete", id="searchField")
    import time; time.sleep(0.3)

    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert r.ok, f"'Buy groceries & vegetables' not found after clearing search: {r}"
    print("cleared search → 'Buy groceries & vegetables' still visible (ok)")

    # ── Delete "Buy groceries & vegetables" ──────────────────────────────────
    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert r.ok, f"select before delete: {r}"
    r = client.click(id="deleteButton")
    assert r.ok, f"click deleteButton: {r}"

    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert not r.ok, "Expected deleted item to be gone from taskList"
    print("deleted 'Buy groceries & vegetables' (ok)")

    # ── Show Completed toggle ─────────────────────────────────────────────────
    r = client.click(id="showCompleted")
    assert r.ok, f"click showCompleted: {r}"

    r = client.select("Call doctor [Medium] 2026-05-01", id="taskList")
    assert r.ok, f"'Call doctor' not visible after showCompleted toggle: {r}"
    print("showCompleted toggled → 'Call doctor' visible (ok)")

    print("\ntodo_demo succeeded (ok)")


if __name__ == "__main__":
    main()
