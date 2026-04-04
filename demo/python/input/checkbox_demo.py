from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- Read initial states ------------------------------------------------
    result = client.get_selected(id="checkA")
    if not result.ok:
        raise SystemExit(f"get_selected(checkA) failed: {result.trace.details}")
    assert result.value is True, f"Expected checkA=True initially, got {result.value}"
    print(f"checkA initial: {result.value}")

    result = client.get_selected(id="checkB")
    if not result.ok:
        raise SystemExit(f"get_selected(checkB) failed: {result.trace.details}")
    assert result.value is False, f"Expected checkB=False initially, got {result.value}"
    print(f"checkB initial: {result.value}")

    result = client.get_selected(id="checkC")
    if not result.ok:
        raise SystemExit(f"get_selected(checkC) failed: {result.trace.details}")
    assert result.value is False, f"Expected checkC=False initially, got {result.value}"
    print(f"checkC initial: {result.value}")

    # ---- Check checkB -------------------------------------------------------
    result = client.set_selected(True, id="checkB")
    if not result.ok:
        raise SystemExit(f"set_selected(checkB, True) failed: {result.trace.details}")
    result = client.get_selected(id="checkB")
    assert result.value is True, f"Expected checkB=True after set, got {result.value}"
    print(f"checkB after set True: {result.value}")

    # ---- Verify independence: checkA and checkC unchanged -------------------
    result = client.get_selected(id="checkA")
    assert result.value is True, f"Expected checkA still True (independent), got {result.value}"
    result = client.get_selected(id="checkC")
    assert result.value is False, f"Expected checkC still False (independent), got {result.value}"
    print("CheckBox independence verified (checkA and checkC unaffected)")

    # ---- Uncheck checkA -----------------------------------------------------
    result = client.set_selected(False, id="checkA")
    if not result.ok:
        raise SystemExit(f"set_selected(checkA, False) failed: {result.trace.details}")
    result = client.get_selected(id="checkA")
    assert result.value is False, f"Expected checkA=False after uncheck, got {result.value}"
    print(f"checkA after uncheck: {result.value}")

    # ---- Restore state ------------------------------------------------------
    client.set_selected(True, id="checkA")
    client.set_selected(False, id="checkB")

    print("\ncheckbox_demo succeeded (ok)")


if __name__ == "__main__":
    main()

