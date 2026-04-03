from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- 列舉分頁 -----------------------------------------------------------
    result = client.get_tabs(id="demoTabPane")
    if not result.ok:
        raise SystemExit(f"get_tabs failed: {result.trace.details}")
    tabs = result.value or []
    print(f"Tabs found: {[t['text'] for t in tabs]}")
    assert len(tabs) >= 3, f"Expected at least 3 tabs, got {len(tabs)}"

    # ---- 逐一選取各分頁 -----------------------------------------------------
    for tab in tabs:
        title = tab["text"]
        result = client.select_tab(title, id="demoTabPane")
        if not result.ok:
            raise SystemExit(f"select_tab('{title}') failed: {result.trace.details}")
        print(f"Selected tab: {title}")

    # ---- 找不到分頁應回傳失敗 -----------------------------------------------
    result = client.select_tab("NonExistent", id="demoTabPane")
    if result.ok:
        raise SystemExit("Expected select_tab('NonExistent') to fail")
    print(f"select_tab('NonExistent') correctly rejected: {result.trace.details.get('reason')}")

    print("\ntab_demo succeeded ✓")


if __name__ == "__main__":
    main()
