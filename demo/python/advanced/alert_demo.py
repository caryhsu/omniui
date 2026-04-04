from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit

_ALERT_BUTTONS = {
    "infoAlertButton": "OK",
    "confirmAlertButton": "Yes",
    "warnAlertButton": "OK",
    "errorAlertButton": "OK",
}


def _run_alert(client, button_id: str, dismiss_with: str, label: str) -> None:
    result = client.click(id=button_id)
    if not result.ok:
        raise SystemExit(f"click {button_id} failed: {result.trace.details}")
    print(f"{label} alert triggered")

    result = client.get_dialog()
    if not result.ok:
        raise SystemExit(f"get_dialog ({label}) failed: {result.trace.details}")

    descriptor = result.value or {}
    print(f"  alertType : {descriptor.get('alertType')}")
    print(f"  buttons   : {descriptor.get('buttons')}")

    result = client.dismiss_dialog(dismiss_with)
    if not result.ok:
        raise SystemExit(f"dismiss_dialog ({label}) failed: {result.trace.details}")
    print(f"  Dismissed with '{dismiss_with}'")


def main() -> None:
    client = connect_or_exit()

    _run_alert(client, "infoAlertButton",    "OK",  "INFORMATION")
    _run_alert(client, "confirmAlertButton", "Yes", "CONFIRMATION")
    _run_alert(client, "warnAlertButton",    "OK",  "WARNING")
    _run_alert(client, "errorAlertButton",   "OK",  "ERROR")

    print("Alert demo succeeded — all four alert types verified")


if __name__ == "__main__":
    main()
