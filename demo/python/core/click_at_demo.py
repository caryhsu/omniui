"""Demonstrate click_at(): clicking by absolute scene coordinates."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Discover the loginButton to find its approximate scene position
    nodes = client.discover()
    login_btn = next(
        (n for n in nodes if n.get("fxId") == "loginButton"),
        None,
    )
    assert login_btn is not None, "loginButton not found in scene"

    # Use bounds to compute a click coordinate near the button center
    bounds = login_btn.get("bounds", {})
    x = bounds.get("x", 0) + bounds.get("width", 10) / 2
    y = bounds.get("y", 0) + bounds.get("height", 10) / 2

    result = client.click_at(x=x, y=y)
    assert result.ok, f"click_at failed: {result}"
    print(f"click_at(x={x:.0f}, y={y:.0f}) succeeded (ok)")

    # Verify the click had the expected effect (login dialog appeared or field focused)
    # Simply verifying the call returns ok is sufficient for the demo
    print("\nclick_at_demo succeeded (ok)")


if __name__ == "__main__":
    main()
