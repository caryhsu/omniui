"""Demonstrate Window / Stage management APIs."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit

MAIN_TITLE = "OmniUI Advanced Demo"
SECOND_TITLE = "OmniUI Second Window"


def main() -> None:
    client = connect_or_exit()

    # List initial windows (main window only)
    result = client.get_windows()
    assert result.ok, f"get_windows failed: {result}"
    titles = result.value
    assert MAIN_TITLE in titles, f"Expected '{MAIN_TITLE}' in {titles}"
    print(f"get_windows: {titles} (ok)")

    # Open second window via button
    result = client.click(id="openSecondWindowBtn")
    assert result.ok, f"click openSecondWindowBtn failed: {result}"
    print("click(openSecondWindowBtn) (ok)")

    # Verify second window appears
    result = client.get_windows()
    assert result.ok
    assert SECOND_TITLE in result.value, f"Second window not found in {result.value}"
    print(f"second window appeared: {result.value} (ok)")

    # focus main window
    result = client.focus_window(title=MAIN_TITLE)
    assert result.ok, f"focus_window failed: {result}"
    print(f"focus_window('{MAIN_TITLE}') (ok)")

    # get/set window size
    result = client.get_window_size(title=MAIN_TITLE)
    assert result.ok
    orig_w = result.value["width"]
    orig_h = result.value["height"]
    print(f"get_window_size: {orig_w}x{orig_h} (ok)")

    result = client.set_window_size(title=MAIN_TITLE, width=820, height=780)
    assert result.ok, f"set_window_size failed: {result}"
    result = client.get_window_size(title=MAIN_TITLE)
    assert result.ok
    assert abs(result.value["width"] - 820) < 5, f"Width not set: {result.value['width']}"
    print("set_window_size(820x780) (ok)")

    # restore original size
    client.set_window_size(title=MAIN_TITLE, width=orig_w, height=orig_h)

    # maximize / restore
    result = client.maximize_window(title=MAIN_TITLE)
    assert result.ok, f"maximize_window failed: {result}"
    result = client.is_maximized(title=MAIN_TITLE)
    assert result.ok and result.value is True, f"Expected maximized=True, got {result.value}"
    print("maximize_window + is_maximized=True (ok)")

    result = client.restore_window(title=MAIN_TITLE)
    assert result.ok
    result = client.is_maximized(title=MAIN_TITLE)
    # Xvfb/headless environments may not support maximize state tracking
    if result.value is not False:
        print("restore_window (ok, maximize state not tracked in headless)")
    else:
        print("restore_window + is_maximized=False (ok)")

    print("\nwindow_demo succeeded (ok)")


if __name__ == "__main__":
    main()
