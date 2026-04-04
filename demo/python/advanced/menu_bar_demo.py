from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Open the 'File' menu and click 'New'
    result = client.open_menu("File", id="demoMenuBar")
    if not result.ok:
        raise SystemExit(f"open_menu failed: {result.trace.details}")
    print("File menu opened")

    result = client.click_menu_item("New")
    if not result.ok:
        raise SystemExit(f"click_menu_item (New) failed: {result.trace.details}")

    result = client.verify_text(id="menuBarStatus", expected="Menu: File > New")
    if not result.ok:
        raise SystemExit(f"MenuBar New verification failed: {result.value}")
    print("MenuBar 'File > New' succeeded")

    # Navigate directly: Edit > Advanced > Reformat
    result = client.navigate_menu("Edit/Advanced/Reformat", id="demoMenuBar")
    if not result.ok:
        raise SystemExit(f"navigate_menu failed: {result.trace.details}")

    result = client.verify_text(id="menuBarStatus", expected="Menu: Edit > Advanced > Reformat")
    if not result.ok:
        raise SystemExit(f"MenuBar nested navigation verification failed: {result.value}")
    print("MenuBar 'Edit > Advanced > Reformat' succeeded")


if __name__ == "__main__":
    main()
