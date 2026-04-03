from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- ProgressBar --------------------------------------------------------
    result = client.get_progress(id="demoProgressBar")
    if not result.ok:
        raise SystemExit(f"get_progress (bar) failed: {result.trace.details}")
    progress = float(result.value)
    print(f"ProgressBar value: {progress:.2f}")
    assert abs(progress - 0.4) < 0.01, f"Expected ~0.4, got {progress}"

    # ---- ProgressIndicator --------------------------------------------------
    result = client.get_progress(id="demoProgressIndicator")
    if not result.ok:
        raise SystemExit(f"get_progress (indicator) failed: {result.trace.details}")
    progress = float(result.value)
    print(f"ProgressIndicator value: {progress:.2f}")
    assert abs(progress - 0.7) < 0.01, f"Expected ~0.7, got {progress}"

    print("\nprogress_demo succeeded ✓")


if __name__ == "__main__":
    main()
