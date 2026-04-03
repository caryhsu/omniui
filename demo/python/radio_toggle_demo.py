from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- 讀取初始狀態 -------------------------------------------------------
    result = client.get_selected(id="radioA")
    if not result.ok:
        raise SystemExit(f"get_selected (radioA) failed: {result.trace.details}")
    assert result.value is True, f"Expected radioA selected=True, got {result.value}"
    print(f"radioA initial selected: {result.value}")

    result = client.get_selected(id="radioB")
    if not result.ok:
        raise SystemExit(f"get_selected (radioB) failed: {result.trace.details}")
    assert result.value is False, f"Expected radioB selected=False, got {result.value}"
    print(f"radioB initial selected: {result.value}")

    # ---- 切換 RadioButton ---------------------------------------------------
    result = client.set_selected(True, id="radioB")
    if not result.ok:
        raise SystemExit(f"set_selected (radioB) failed: {result.trace.details}")
    print("Selected radioB")

    result = client.get_selected(id="radioB")
    assert result.value is True, f"Expected radioB selected=True after set, got {result.value}"
    result = client.get_selected(id="radioA")
    assert result.value is False, f"Expected radioA deselected after selecting B, got {result.value}"
    print("ToggleGroup mutual exclusion verified")

    # 恢復 radioA
    client.set_selected(True, id="radioA")

    # ---- ToggleButton -------------------------------------------------------
    result = client.get_selected(id="toggleButton")
    if not result.ok:
        raise SystemExit(f"get_selected (toggleButton) failed: {result.trace.details}")
    initial = result.value
    print(f"toggleButton initial selected: {initial}")

    result = client.set_selected(not initial, id="toggleButton")
    if not result.ok:
        raise SystemExit(f"set_selected (toggleButton) failed: {result.trace.details}")

    result = client.get_selected(id="toggleButton")
    assert result.value is not initial, "ToggleButton state should have changed"
    print(f"toggleButton toggled to: {result.value}")

    print("\nradio_toggle_demo succeeded ✓")


if __name__ == "__main__":
    main()
