from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- Read initial value -------------------------------------------------
    result = client.get_value(id="demoChoiceBox")
    if not result.ok:
        raise SystemExit(f"get_value(demoChoiceBox) failed: {result.trace.details}")
    print(f"Initial value: {result.value}")

    # ---- Select each item and verify ----------------------------------------
    for item in ("Banana", "Cherry", "Apple"):
        result = client.select(item, id="demoChoiceBox")
        if not result.ok:
            raise SystemExit(f"select('{item}') failed: {result.trace.details}")
        result = client.get_value(id="demoChoiceBox")
        if not result.ok:
            raise SystemExit(f"get_value after select('{item}') failed: {result.trace.details}")
        assert result.value == item, f"Expected '{item}', got '{result.value}'"
        print(f"Selected '{item}' → value: {result.value} ✓")

    # ---- Verify item_not_found error ----------------------------------------
    result = client.select("Dragonfruit", id="demoChoiceBox")
    assert not result.ok, "Expected failure for non-existent item"
    details = result.trace.details
    reason = details.get("reason", "") if isinstance(details, dict) else str(details)
    assert reason == "item_not_found", f"Expected item_not_found reason, got: {reason}"
    print(f"select('Dragonfruit') correctly rejected: {reason}")

    print("\nchoicebox_demo succeeded ✓")


if __name__ == "__main__":
    main()
