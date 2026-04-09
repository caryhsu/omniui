"""Demonstrate modifier+click: Ctrl+click for additive selection,
Shift+click for range selection on a ListView."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Ctrl+click: additive selection — selects an item without clearing existing selection
    result = client.click(id="serverList", modifiers=["Ctrl"])
    if not result.ok:
        raise SystemExit(f"Ctrl+click failed: {result.trace.details}")
    print("Ctrl+click succeeded (ok)")

    # Shift+click: range selection
    result = client.click(id="serverList", modifiers=["Shift"])
    if not result.ok:
        raise SystemExit(f"Shift+click failed: {result.trace.details}")
    print("Shift+click succeeded (ok)")

    # Ctrl+Shift combination
    result = client.click(id="serverList", modifiers=["Ctrl", "Shift"])
    if not result.ok:
        raise SystemExit(f"Ctrl+Shift+click failed: {result.trace.details}")
    print("Ctrl+Shift+click succeeded (ok)")

    print("\nmodifier_click_demo succeeded (ok)")


if __name__ == "__main__":
    main()

