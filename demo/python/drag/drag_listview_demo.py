"""Demonstrate drag() on the dedicated Drag & Drop demo app.

Scenario: drag items from leftPanel (Available) to rightPanel (Selected).
- drag Apple, Banana, Cherry one by one
- verify dragStatus shows "dropped <item>" after each
- verify dropped items appear in rightPanel
- reset and verify state is restored
"""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def _check_status(client, expected_substr: str) -> None:
    r = client.get_text(id="dragStatus")
    assert r.ok, f"get_text(dragStatus) failed: {r}"
    assert expected_substr in r.value, (
        f"dragStatus: expected {expected_substr!r} in {r.value!r}"
    )


def main() -> None:
    client = connect_or_exit()

    # ── Reset to known state ─────────────────────────────────────────────────
    client.click(id="resetBtn")
    _check_status(client, "idle")
    print("reset ok")

    # ── Drag Apple → rightPanel ──────────────────────────────────────────────
    result = client.drag(id="item_apple").to(id="rightPanel")
    assert result.ok, f"drag Apple failed: {result}"
    _check_status(client, "dropped Apple")
    print("drag Apple → rightPanel (ok)")

    # ── Drag Banana → rightPanel ─────────────────────────────────────────────
    result = client.drag(id="item_banana").to(id="rightPanel")
    assert result.ok, f"drag Banana failed: {result}"
    _check_status(client, "dropped Banana")
    print("drag Banana → rightPanel (ok)")

    # ── Drag Cherry → rightPanel ─────────────────────────────────────────────
    result = client.drag(id="item_cherry").to(id="rightPanel")
    assert result.ok, f"drag Cherry failed: {result}"
    _check_status(client, "dropped Cherry")
    print("drag Cherry → rightPanel (ok)")

    # ── Verify dropped items exist in rightPanel ─────────────────────────────
    for dropped_id in ("item_apple_dropped", "item_banana_dropped", "item_cherry_dropped"):
        r = client.get_text(id=dropped_id)
        assert r.ok, f"Expected {dropped_id} in rightPanel: {r}"
    print("rightPanel contains Apple, Banana, Cherry (ok)")

    # ── Verify remaining items still in leftPanel ────────────────────────────
    for remaining_id in ("item_date", "item_elderberry"):
        r = client.get_text(id=remaining_id)
        assert r.ok, f"Expected {remaining_id} still in leftPanel: {r}"
    print("leftPanel still has Date, Elderberry (ok)")

    # ── Reset and verify ─────────────────────────────────────────────────────
    client.click(id="resetBtn")
    _check_status(client, "idle")
    for item_id in ("item_apple", "item_banana", "item_cherry", "item_date", "item_elderberry"):
        r = client.get_text(id=item_id)
        assert r.ok, f"After reset, expected {item_id} back in leftPanel: {r}"
    print("reset restores all items (ok)")

    print("\ndrag_listview_demo succeeded (ok)")


if __name__ == "__main__":
    main()
