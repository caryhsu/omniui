"""Demonstrate set_color() / get_color() on the color-app.

Scenario:
- Set color to #ff5733 via set_color → verify colorResult label updates
- Read color back via get_color → verify hex matches
- Click resetColorButton → verify colorResult is cleared
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

    # ── Set color and verify result label ─────────────────────────────────────
    result = client.set_color("#ff5733", id="demoPicker")
    assert result.ok, f"set_color failed: {result}"

    label = client.get_text(id="colorResult")
    assert label.ok and "ff5733" in label.value.lower(), (
        f"Expected colorResult to contain 'ff5733', got: {label.value!r}"
    )
    print(f"set_color ok, colorResult = {label.value!r} (ok)")

    # ── Read color back ────────────────────────────────────────────────────────
    color = client.get_color(id="demoPicker")
    assert color.ok, f"get_color failed: {color}"
    assert color.value and "ff5733" in color.value.lower(), (
        f"Expected get_color to return '#ff5733', got: {color.value!r}"
    )
    print(f"get_color = {color.value!r} (ok)")

    # ── Reset and verify ───────────────────────────────────────────────────────
    reset = client.click(id="resetColorButton")
    assert reset.ok, f"click resetColorButton failed: {reset}"

    label_after = client.get_text(id="colorResult")
    assert label_after.ok and label_after.value == "", (
        f"Expected colorResult to be empty after reset, got: {label_after.value!r}"
    )
    print("resetColorButton cleared colorResult (ok)")

    print("\ncolor_demo succeeded (ok)")


if __name__ == "__main__":
    main()
