"""keyboard_shortcuts_demo.py — Smoke tests for press_key().

Covers:
  1. Single key:   press_key("Tab") on username field (moves focus)
  2. Single key:   press_key("Escape") globally (no selector)
  3. Modifier key: press_key("Control+A", id="username") (select all text)
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

    # 1. Type into username, then press Tab to move focus
    client.click(id="username")
    client.type("testuser", id="username")
    result = client.press_key("Tab", id="username")
    if not result.ok:
        raise SystemExit(f"press_key Tab failed: {result.trace.details}")
    print("press_key('Tab') succeeded")

    # 2. Press Escape globally (no selector — fires on focus owner)
    result = client.press_key("Escape")
    if not result.ok:
        raise SystemExit(f"press_key Escape failed: {result.trace.details}")
    print("press_key('Escape') succeeded")

    # 3. Ctrl+A to select all text in username field
    result = client.press_key("Control+A", id="username")
    if not result.ok:
        raise SystemExit(f"press_key Control+A failed: {result.trace.details}")
    print("press_key('Control+A') succeeded")

    # 4. Alias: ctrl+z (lowercase, alias)
    result = client.press_key("ctrl+z", id="username")
    if not result.ok:
        raise SystemExit(f"press_key ctrl+z failed: {result.trace.details}")
    print("press_key('ctrl+z') succeeded")

    print("All keyboard shortcut tests passed")


if __name__ == "__main__":
    main()
