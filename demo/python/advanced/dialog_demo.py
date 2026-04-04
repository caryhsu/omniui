from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Trigger the dialog
    result = client.click(id="showDialogButton")
    if not result.ok:
        raise SystemExit(f"click showDialogButton failed: {result.trace.details}")
    print("Dialog triggered")

    # Inspect the open dialog
    result = client.get_dialog()
    if not result.ok:
        raise SystemExit(f"get_dialog failed: {result.trace.details}")

    descriptor = result.value or {}
    print(f"Dialog title   : {descriptor.get('title')}")
    print(f"Dialog header  : {descriptor.get('header')}")
    print(f"Dialog content : {descriptor.get('content')}")
    print(f"Dialog buttons : {descriptor.get('buttons')}")

    # Dismiss with OK
    result = client.dismiss_dialog("OK")
    if not result.ok:
        raise SystemExit(f"dismiss_dialog failed: {result.trace.details}")
    print("Dialog dismissed with OK")


if __name__ == "__main__":
    main()
