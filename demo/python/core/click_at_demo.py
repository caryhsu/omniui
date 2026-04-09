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

    # Use a normal click first to show the checkbox is reachable by selector
    result = client.click(id="auditEnabled")
    assert result.ok, f"auditEnabled not accessible: {result}"

    # Now demonstrate click_at with coordinates — use approximate centre
    at_result = client.click_at(x=200, y=260)
    assert at_result.ok, f"click_at failed: {at_result}"
    print("click_at(x=200, y=260) succeeded (ok)")

    print("\nclick_at_demo succeeded (ok)")


if __name__ == "__main__":
    main()
