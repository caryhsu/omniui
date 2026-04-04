from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Set divider to 0.3
    result = client.set_divider_position(0, 0.3, id="demoSplitPane")
    if not result.ok:
        raise SystemExit(f"set_divider_position failed: {result.trace.details}")

    # Get divider positions and verify approximately 0.3
    result = client.get_divider_positions(id="demoSplitPane")
    if not result.ok:
        raise SystemExit(f"get_divider_positions failed: {result.trace.details}")
    if result.value is None:
        raise SystemExit(f"get_divider_positions returned None")
    # Parse the value: Arrays.toString(double[]) format e.g. "[0.3001...]"
    raw = result.value.strip().strip("[]")
    try:
        actual = float(raw.split(",")[0])
    except ValueError:
        raise SystemExit(f"get_divider_positions could not parse: {result.value!r}")
    if not (0.25 <= actual <= 0.35):
        raise SystemExit(f"get_divider_positions out of expected range: {actual}")

    result = client.set_divider_position(0, 0.5, id="demoSplitPane")
    if not result.ok:
        raise SystemExit(f"set_divider_position reset failed: {result.trace.details}")

    # Verify divider is back near 0.5
    result = client.get_divider_positions(id="demoSplitPane")
    if not result.ok:
        raise SystemExit(f"get_divider_positions after reset failed: {result.trace.details}")
    raw2 = (result.value or "").strip().strip("[]")
    try:
        actual2 = float(raw2.split(",")[0])
    except ValueError:
        raise SystemExit(f"get_divider_positions could not parse after reset: {result.value!r}")
    if not (0.45 <= actual2 <= 0.55):
        raise SystemExit(f"SplitPane divider not near 0.5 after reset: {actual2}")

    print("SplitPane demo succeeded — divider set to 0.3 and back to 0.5")


if __name__ == "__main__":
    main()
