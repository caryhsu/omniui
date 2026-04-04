from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── wait_for_visible: hide then immediately show ──────────────────────────
    client.set_visible(False, id="nodeStateTarget")
    client.set_visible(True, id="nodeStateTarget")
    client.wait_for_visible("nodeStateTarget", timeout=3.0)

    # ── wait_for_enabled: disable then immediately re-enable ──────────────────
    client.set_disabled(True, id="nodeStateTarget")
    client.set_disabled(False, id="nodeStateTarget")
    client.wait_for_enabled("nodeStateTarget", timeout=3.0)

    # ── wait_for_text: tooltipBtn should show "Hover Me" ─────────────────────
    client.wait_for_text("tooltipBtn", "Hover Me", timeout=3.0)

    # ── wait_for_value: alias — same check ────────────────────────────────────
    client.wait_for_value("tooltipBtn", "Hover Me", timeout=3.0)

    # ── wait_for_node: a known node should be found ───────────────────────────
    client.wait_for_node("tooltipBtn", timeout=3.0)

    # ── timeout is respected: non-existent node should raise TimeoutError ─────
    raised = False
    try:
        client.wait_for_node("__no_such_node__", timeout=0.5)
    except TimeoutError:
        raised = True
    assert raised, "wait_for_node should raise TimeoutError for non-existent node"

    print("wait_conditions_demo succeeded — wait_for_text, wait_for_visible, wait_for_enabled, wait_for_node, wait_for_value all verified (ok)")


if __name__ == "__main__":
    main()

