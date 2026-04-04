from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- Read initial expanded state ----------------------------------------
    for pane_id in ("pane1", "pane2", "pane3"):
        expanded = client.get_expanded(id=pane_id)
        print(f"Initial {pane_id} expanded: {expanded}")

    # ---- Expand pane1 and verify --------------------------------------------
    result = client.expand_pane(id="pane1")
    if not result.ok:
        raise SystemExit(f"expand_pane(pane1) failed: {result.trace.details}")
    assert client.get_expanded(id="pane1"), "pane1 should be expanded"
    print("Expanded pane1 ✓")

    # ---- Expand pane2; due to Accordion single-pane constraint, pane1 collapses
    result = client.expand_pane(id="pane2")
    if not result.ok:
        raise SystemExit(f"expand_pane(pane2) failed: {result.trace.details}")
    assert client.get_expanded(id="pane2"), "pane2 should be expanded"
    assert not client.get_expanded(id="pane1"), "pane1 should be collapsed after expanding pane2"
    print("Expanded pane2; pane1 auto-collapsed (mutual exclusion) ✓")

    # ---- Expand pane3 -------------------------------------------------------
    result = client.expand_pane(id="pane3")
    if not result.ok:
        raise SystemExit(f"expand_pane(pane3) failed: {result.trace.details}")
    assert client.get_expanded(id="pane3"), "pane3 should be expanded"
    assert not client.get_expanded(id="pane2"), "pane2 should be collapsed after expanding pane3"
    print("Expanded pane3; pane2 auto-collapsed ✓")

    # ---- Collapse pane3 -----------------------------------------------------
    result = client.collapse_pane(id="pane3")
    if not result.ok:
        raise SystemExit(f"collapse_pane(pane3) failed: {result.trace.details}")
    assert not client.get_expanded(id="pane3"), "pane3 should be collapsed"
    print("Collapsed pane3 ✓")

    print("\naccordion_demo succeeded ✓")


if __name__ == "__main__":
    main()
