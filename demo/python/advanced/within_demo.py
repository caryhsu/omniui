"""Demonstrate scoped selector (within) API."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Click OK inside leftPanel only
    with client.within(id="leftPanel"):
        result = client.click(id="panelOkBtn")
    assert result.ok, f"click in leftPanel failed: {result}"
    print("click(id=panelOkBtn) within leftPanel (ok)")

    # Verify leftPanelStatus updated
    result = client.get_text(id="leftPanelStatus")
    assert result.ok and result.value == "left clicked", \
        f"Expected 'left clicked', got {result.value!r}"
    print(f"leftPanelStatus = {result.value!r} (ok)")

    # Verify rightPanelStatus unchanged
    result = client.get_text(id="rightPanelStatus")
    assert result.ok and result.value == "right idle", \
        f"Expected 'right idle', got {result.value!r}"
    print(f"rightPanelStatus = {result.value!r} (ok)")

    # Now click OK inside rightPanel
    with client.within(id="rightPanel"):
        result = client.click(id="panelOkBtn")
    assert result.ok, f"click in rightPanel failed: {result}"
    print("click(id=panelOkBtn) within rightPanel (ok)")

    result = client.get_text(id="rightPanelStatus")
    assert result.ok and result.value == "right clicked", \
        f"Expected 'right clicked', got {result.value!r}"
    print(f"rightPanelStatus = {result.value!r} (ok)")

    # After context exits, scope is cleared
    assert client._scope is None, "Scope should be cleared after with block"
    print("scope cleared after context (ok)")

    print("\nwithin_demo succeeded (ok)")


if __name__ == "__main__":
    main()
