from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── baseline: showDialogButton should be visible and enabled ─────────────
    assert client.is_visible(id="showDialogButton"), "showDialogButton should be visible"
    assert client.is_enabled(id="showDialogButton"), "showDialogButton should be enabled"

    # ── non-existent node returns False without raising ───────────────────────
    assert not client.is_visible(id="__no_such_node__"), "non-existent id: is_visible should be False"
    assert not client.is_enabled(id="__no_such_node__"), "non-existent id: is_enabled should be False"

    # ── nodeStateTarget starts visible and enabled ────────────────────────────
    assert client.is_visible(id="nodeStateTarget"), "nodeStateTarget should start visible"
    assert client.is_enabled(id="nodeStateTarget"), "nodeStateTarget should start enabled"

    # ── hide via Python → is_visible becomes False ────────────────────────────
    client.set_visible(False, id="nodeStateTarget")
    assert not client.is_visible(id="nodeStateTarget"), "nodeStateTarget should be hidden after set_visible(False)"

    # ── show again → is_visible becomes True ──────────────────────────────────
    client.set_visible(True, id="nodeStateTarget")
    assert client.is_visible(id="nodeStateTarget"), "nodeStateTarget should be visible after set_visible(True)"

    # ── disable via Python → is_enabled becomes False ─────────────────────────
    client.set_disabled(True, id="nodeStateTarget")
    assert not client.is_enabled(id="nodeStateTarget"), "nodeStateTarget should be disabled after set_disabled(True)"

    # ── re-enable → is_enabled becomes True ───────────────────────────────────
    client.set_disabled(False, id="nodeStateTarget")
    assert client.is_enabled(id="nodeStateTarget"), "nodeStateTarget should be enabled after set_disabled(False)"

    print("node_state_demo succeeded — is_visible, is_enabled, set_visible, set_disabled all verified (ok)")


if __name__ == "__main__":
    main()

