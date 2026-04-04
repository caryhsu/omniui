"""hover_demo.py — Smoke test for the hover() action.

Verifies:
1. hover() on a node with a tooltip returns success.
2. hover() on a node without a tooltip also returns success.
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

    # Hover over a node that has a tooltip
    result = client.hover(id="loginButton")
    if not result.ok:
        raise SystemExit(f"hover(loginButton) failed: {result.trace.details}")
    print("  hover(loginButton) succeeded ✓")

    # Verify the tooltip text is still readable after hover
    tooltip = client.get_tooltip(id="loginButton")
    if not tooltip.ok:
        raise SystemExit(f"get_tooltip after hover failed: {tooltip.trace.details}")
    print(f"  tooltip text = {tooltip.value!r} ✓")

    # Hover over a node without a tooltip — should still succeed
    result2 = client.hover(id="status")
    if not result2.ok:
        raise SystemExit(f"hover(status) failed: {result2.trace.details}")
    print("  hover(status) [no tooltip] succeeded ✓")

    print("\nhover_demo succeeded ✓")


if __name__ == "__main__":
    main()
