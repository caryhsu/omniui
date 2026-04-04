from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- Slider -------------------------------------------------------------
    result = client.set_slider(75.0, id="demoSlider")
    if not result.ok:
        raise SystemExit(f"set_slider failed: {result.trace.details}")
    print(f"Slider set to 75.0")

    result = client.set_slider(0.0, id="demoSlider")
    if not result.ok:
        raise SystemExit(f"set_slider (min) failed: {result.trace.details}")
    print("Slider set to 0.0 (min)")

    result = client.set_slider(100.0, id="demoSlider")
    if not result.ok:
        raise SystemExit(f"set_slider (max) failed: {result.trace.details}")
    print("Slider set to 100.0 (max)")

    # Out-of-range should fail
    result = client.set_slider(200.0, id="demoSlider")
    if result.ok:
        raise SystemExit("Expected set_slider(200) to fail, but it succeeded")
    print(f"set_slider(200) correctly rejected: {result.trace.details.get('reason')}")

    # ---- Spinner ------------------------------------------------------------
    result = client.set_spinner("50", id="demoSpinner")
    if not result.ok:
        raise SystemExit(f"set_spinner failed: {result.trace.details}")
    print("Spinner set to 50")

    result = client.step_spinner(3, id="demoSpinner")
    if not result.ok:
        raise SystemExit(f"step_spinner (+3) failed: {result.trace.details}")
    print("Spinner incremented by 3 → should be 53")

    result = client.step_spinner(-5, id="demoSpinner")
    if not result.ok:
        raise SystemExit(f"step_spinner (-5) failed: {result.trace.details}")
    print("Spinner decremented by 5 → should be 48")

    print("\nslider_spinner_demo succeeded ✓")


if __name__ == "__main__":
    main()
