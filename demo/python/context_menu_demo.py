from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Open the context menu on the labeled target node
    result = client.right_click(id="contextMenuTarget")
    if not result.ok:
        raise SystemExit(f"right_click failed: {result.trace.details}")
    print("ContextMenu opened")

    # Click the 'Copy' item
    result = client.click_menu_item("Copy")
    if not result.ok:
        raise SystemExit(f"click_menu_item failed: {result.trace.details}")

    # Verify the status label was updated
    result = client.verify_text(id="contextMenuStatus", expected="Context menu: Copy")
    if not result.ok:
        raise SystemExit(f"ContextMenu action verification failed: {result.value}")
    print("ContextMenu demo succeeded — Copy action confirmed")

    # Open again and navigate into a submenu item (More > Details)
    result = client.right_click(id="contextMenuTarget")
    if not result.ok:
        raise SystemExit(f"right_click (second) failed: {result.trace.details}")

    result = client.click_menu_item(path="More/Details")
    if not result.ok:
        raise SystemExit(f"click_menu_item (submenu path) failed: {result.trace.details}")

    result = client.verify_text(id="contextMenuStatus", expected="Context menu: Details")
    if not result.ok:
        raise SystemExit(f"Submenu action verification failed: {result.value}")
    print("ContextMenu submenu demo succeeded — Details action confirmed")


if __name__ == "__main__":
    main()
