"""Demonstrate get_scroll_position() and set_scroll_position() on a ScrollBar."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Read initial value (should be 30)
    result = client.get_scroll_position(id="demoScrollBar")
    assert result.ok, f"get_scroll_position failed: {result}"
    pos = result.value
    assert pos["min"] == 0.0, f"Expected min=0, got {pos['min']}"
    assert pos["max"] == 100.0, f"Expected max=100, got {pos['max']}"
    assert pos["value"] == 30.0, f"Expected initial value=30, got {pos['value']}"
    print(f"get_scroll_position: value={pos['value']}, min={pos['min']}, max={pos['max']} (ok)")

    # Set to 75
    result = client.set_scroll_position(id="demoScrollBar", value=75.0)
    assert result.ok, f"set_scroll_position failed: {result}"
    print("set_scroll_position(value=75) (ok)")

    # Verify round-trip
    result = client.get_scroll_position(id="demoScrollBar")
    assert result.ok
    assert result.value["value"] == 75.0, f"Expected 75, got {result.value['value']}"
    print("round-trip verified: value=75.0 (ok)")

    # Test clamping: set out-of-range value
    result = client.set_scroll_position(id="demoScrollBar", value=999.0)
    assert result.ok, f"set out-of-range failed: {result}"
    result = client.get_scroll_position(id="demoScrollBar")
    assert result.ok
    assert result.value["value"] == 100.0, f"Expected clamped to 100, got {result.value['value']}"
    print("clamping: 999 -> 100.0 (ok)")

    # Restore to 30
    client.set_scroll_position(id="demoScrollBar", value=30.0)

    print("\nscrollbar_demo succeeded (ok)")


if __name__ == "__main__":
    main()
