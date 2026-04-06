"""Demonstrate full CRUD workflow on the todo-app (dialog-based UI).

Scenario:
- Add "Buy groceries" (High, 2026-04-30) via dialog → verify in list
- Add "Call doctor" (Medium, 2026-05-01) via dialog → verify in list
- Edit "Buy groceries" title → "Buy groceries & vegetables" via dialog
- Click check_1 (Call doctor) → assert it disappears (showCompleted=off)
- Type "Buy" in searchField → assert "Buy groceries & vegetables" visible
- Clear searchField → assert it is still visible
- Delete "Buy groceries & vegetables" via delete_0 + confirmation dialog
- Toggle showCompleted → assert "Call doctor" now visible
"""
from __future__ import annotations
import time

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

    # ── Add task 1: Buy groceries (High, 2026-04-30) ──────────────────────────
    r = client.click(id="addButton")
    assert r.ok, f"click addButton: {r}"

    r = client.type("Buy groceries", id="dialogTitleField")
    assert r.ok, f"type dialogTitleField: {r}"
    r = client.select("High", id="dialogPriorityCombo")
    assert r.ok, f"select dialogPriorityCombo High: {r}"
    r = client.set_date("2026-04-30", id="dialogDatePicker")
    assert r.ok, f"set_date 2026-04-30: {r}"
    r = client.dismiss_dialog(button="OK")
    assert r.ok, f"dismiss_dialog OK (add task 1): {r}"

    r = client.select("Buy groceries [High] 2026-04-30", id="taskList")
    assert r.ok, f"'Buy groceries' not found in taskList: {r}"
    print("added 'Buy groceries [High] 2026-04-30' (ok)")

    # ── Add task 2: Call doctor (Medium, 2026-05-01) ──────────────────────────
    r = client.click(id="addButton")
    assert r.ok, f"click addButton (task 2): {r}"

    r = client.type("Call doctor", id="dialogTitleField")
    assert r.ok, f"type dialogTitleField: {r}"
    # Medium is default; set date
    r = client.set_date("2026-05-01", id="dialogDatePicker")
    assert r.ok, f"set_date 2026-05-01: {r}"
    r = client.dismiss_dialog(button="OK")
    assert r.ok, f"dismiss_dialog OK (add task 2): {r}"

    r = client.select("Call doctor [Medium] 2026-05-01", id="taskList")
    assert r.ok, f"'Call doctor' not found in taskList: {r}"
    print("added 'Call doctor [Medium] 2026-05-01' (ok)")

    # ── Edit task 0: rename "Buy groceries" → "Buy groceries & vegetables" ────
    r = client.click(id="edit_0")
    assert r.ok, f"click edit_0: {r}"

    r = client.press_key("Control+A", id="dialogTitleField")
    assert r.ok, f"Ctrl+A on dialogTitleField: {r}"
    r = client.type("Buy groceries & vegetables", id="dialogTitleField")
    assert r.ok, f"type new title: {r}"
    r = client.dismiss_dialog(button="OK")
    assert r.ok, f"dismiss_dialog OK (edit): {r}"

    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert r.ok, f"Edited title not found in taskList: {r}"
    print("edit title → 'Buy groceries & vegetables [High] 2026-04-30' (ok)")

    # ── Check "Call doctor" (index 1) ─────────────────────────────────────────
    r = client.click(id="check_1")
    assert r.ok, f"click check_1 (Call doctor): {r}"
    # showCompleted=off → completed task disappears
    r = client.select("Call doctor [Medium] 2026-05-01", id="taskList")
    assert not r.ok, (
        "Expected 'Call doctor' to be hidden after completing, but select succeeded"
    )
    print("check_1 toggled → 'Call doctor' hidden (completed, showCompleted=off) (ok)")

    # ── Search filter ─────────────────────────────────────────────────────────
    client.press_key("Control+A", id="searchField")
    client.type("Buy", id="searchField")
    time.sleep(0.3)

    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert r.ok, f"'Buy groceries & vegetables' not found during search: {r}"
    print("search 'Buy' → 'Buy groceries & vegetables' visible (ok)")

    # ── Clear search ──────────────────────────────────────────────────────────
    client.press_key("Control+A", id="searchField")
    client.press_key("Delete", id="searchField")
    time.sleep(0.3)

    r = client.select("Buy groceries & vegetables [High] 2026-04-30", id="taskList")
    assert r.ok, f"'Buy groceries & vegetables' not found after clearing search: {r}"
    print("cleared search → 'Buy groceries & vegetables' still visible (ok)")

    # ── Delete task 0: "Buy groceries & vegetables" ───────────────────────────
    r = client.click(id="delete_0")
    assert r.ok, f"click delete_0: {r}"
    r = client.dismiss_dialog(button="OK")  # confirm deletion
    assert r.ok, f"dismiss_dialog OK (delete): {r}"

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

