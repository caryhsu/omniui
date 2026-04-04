"""double_click_demo.py — Smoke test for the double_click() action.

Fires a synthesized MouseEvent(clickCount=2) on the assetTree TreeView.
In a real application, nodes with setOnMouseClicked handlers that check
event.getClickCount() == 2 will be triggered by this action.
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
    result = client.double_click(id="assetTree")
    if not result.ok:
        raise SystemExit(f"double_click failed: {result.trace.details}")
    print("double_click succeeded")


if __name__ == "__main__":
    main()
