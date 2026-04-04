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

    # loginButton has tooltip "Enter credentials and click to log in"
    result = client.get_tooltip(id="loginButton")
    if not result.ok:
        raise SystemExit(f"get_tooltip(loginButton) failed: {result.trace.details}")
    expected = "Enter credentials and click to log in"
    if result.value != expected:
        raise SystemExit(
            f"Tooltip mismatch: expected {expected!r}, got {result.value!r}"
        )
    print(f"  loginButton tooltip = {result.value!r}  ✓")

    # status Label has no tooltip — should return ""
    result2 = client.get_tooltip(id="status")
    if not result2.ok:
        raise SystemExit(f"get_tooltip(status) failed: {result2.trace.details}")
    if result2.value != "":
        raise SystemExit(
            f"Expected empty tooltip for 'status', got {result2.value!r}"
        )
    print(f"  status tooltip = {result2.value!r} (empty, as expected)  ✓")

    print("get_tooltip tests passed")


if __name__ == "__main__":
    main()
