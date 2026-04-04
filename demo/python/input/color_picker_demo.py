from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Set color directly
    target_color = "#ff6347"
    result = client.set_color(target_color, id="demoPicker2")
    if not result.ok:
        raise SystemExit(f"set_color failed: {result.trace.details}")

    result = client.verify_text(id="colorResult", expected=f"Selected: {target_color}")
    if not result.ok:
        raise SystemExit(f"ColorPicker color verification failed: {result.value}")

    # Get color back
    result = client.get_color(id="demoPicker2")
    if not result.ok:
        raise SystemExit(f"get_color failed: {result.trace.details}")
    if result.value != target_color:
        raise SystemExit(f"get_color mismatch: expected {target_color}, got {result.value}")

    # Reset via button
    result = client.click(id="resetColorButton")
    if not result.ok:
        raise SystemExit(f"Reset click failed: {result.trace.details}")

    result = client.verify_text(id="colorResult", expected="Selected: #1e90ff")
    if not result.ok:
        raise SystemExit(f"ColorPicker reset verification failed: {result.value}")

    print(f"ColorPicker demo succeeded — color {target_color} set and reset")


if __name__ == "__main__":
    main()
