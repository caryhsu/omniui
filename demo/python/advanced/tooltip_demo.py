"""tooltip_demo.py — Smoke test for the get_tooltip() action.

Verifies:
1. A node with a tooltip returns the expected text.
2. A node without a tooltip returns an empty string.
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

    # tooltipBtn has tooltip "Hover to see this tooltip"
    result = client.get_tooltip(id="tooltipBtn")
    if not result.ok:
        raise SystemExit(f"get_tooltip(tooltipBtn) failed: {result.trace.details}")
    expected = "Hover to see this tooltip"
    if result.value != expected:
        raise SystemExit(
            f"Tooltip mismatch: expected {expected!r}, got {result.value!r}"
        )
    print(f"  tooltipBtn tooltip = {result.value!r}  (ok)")

    # showDialogButton has no tooltip — should return ""
    result2 = client.get_tooltip(id="showDialogButton")
    if not result2.ok:
        raise SystemExit(f"get_tooltip(showDialogButton) failed: {result2.trace.details}")
    if result2.value != "":
        raise SystemExit(
            f"Expected empty tooltip for 'showDialogButton', got {result2.value!r}"
        )
    print(f"  showDialogButton tooltip = {result2.value!r} (empty, as expected)  (ok)")

    print("get_tooltip tests passed")


if __name__ == "__main__":
    main()

