from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- 點擊前 visited 應為 False ------------------------------------------
    visited_before = client.is_visited(id="demoHyperlink")
    assert not visited_before, f"Expected visited=False before click, got {visited_before}"
    print(f"Before click: visited={visited_before} ✓")

    # ---- 點擊 Hyperlink -----------------------------------------------------
    result = client.click(id="demoHyperlink")
    if not result.ok:
        raise SystemExit(f"click failed: {result.trace.details}")
    print("Clicked Hyperlink ✓")

    # ---- 點擊後 visited 應為 True -------------------------------------------
    visited_after = client.is_visited(id="demoHyperlink")
    assert visited_after, f"Expected visited=True after click, got {visited_after}"
    print(f"After click: visited={visited_after} ✓")

    print("\nhyperlink_demo succeeded ✓")


if __name__ == "__main__":
    main()
