"""multi_select_demo.py — Smoke test for select_multiple() and get_selected_items()."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def _check(result, label: str) -> None:
    if not result.ok:
        raise SystemExit(f"{label} FAILED — {result!r}")
    print(f"  {label}  ✓")


def main() -> None:
    client = connect_or_exit()

    # Select two items
    _check(
        client.select_multiple(["alpha-node", "gamma-node"], id="serverList"),
        "select_multiple ['alpha-node', 'gamma-node']"
    )

    # Verify both are selected
    result = client.get_selected_items(id="serverList")
    _check(result, "get_selected_items after multi-select")
    selected = sorted(result.value)
    assert selected == ["alpha-node", "gamma-node"], \
        f"Expected ['alpha-node', 'gamma-node'] but got {selected}"
    print(f"  selected items = {selected}  ✓")

    # Select a single item (clears previous)
    _check(
        client.select_multiple(["beta-node"], id="serverList"),
        "select_multiple ['beta-node'] (single)"
    )
    result2 = client.get_selected_items(id="serverList")
    _check(result2, "get_selected_items after single select")
    assert result2.value == ["beta-node"], \
        f"Expected ['beta-node'] but got {result2.value}"
    print(f"  selected items = {result2.value}  ✓")

    print("multi_select tests passed")


if __name__ == "__main__":
    main()
