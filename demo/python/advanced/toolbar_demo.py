"""Demonstrate get_toolbar_items() and toolbar button interaction."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    result = client.get_toolbar_items(id="mainToolBar")
    assert result.ok, f"get_toolbar_items failed: {result}"
    items = result.value
    assert isinstance(items, list) and len(items) > 0, "Expected at least one toolbar item"
    print(f"get_toolbar_items: {len(items)} items found (ok)")

    for item in items:
        state = "disabled" if item.get("disabled") else "enabled"
        print(f"  [{item['type']}] id={item['fxId']!r} text={item['text']!r} ({state})")

    # Verify Save button is disabled
    save_item = next((i for i in items if i.get("fxId") == "tbSave"), None)
    assert save_item is not None, "tbSave not found in toolbar items"
    assert save_item["disabled"], "Expected tbSave to be disabled"
    print("tbSave is disabled as expected (ok)")

    # Click an enabled toolbar button
    result = client.click(id="tbNew")
    assert result.ok, f"click tbNew failed: {result}"
    print("click(id=tbNew) (ok)")

    status = client.get_text(id="tbStatus")
    assert status.ok and status.value == "New clicked", f"Unexpected tbStatus: {status.value!r}"
    print(f"tbStatus = {status.value!r} (ok)")

    print("\ntoolbar_demo succeeded (ok)")


if __name__ == "__main__":
    main()
