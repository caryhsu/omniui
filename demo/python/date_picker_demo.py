from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Open the DatePicker popup
    result = client.open_datepicker(id="demoPicker")
    if not result.ok:
        raise SystemExit(f"open_datepicker failed: {result.trace.details}")
    print("DatePicker popup opened")

    # Navigate forward one month before picking
    result = client.navigate_month("next")
    if not result.ok:
        raise SystemExit(f"navigate_month failed: {result.trace.details}")
    print("Navigated to next month")

    # Pick a specific date (ISO-8601 format)
    target_date = "2025-09-15"
    result = client.pick_date(target_date)
    if not result.ok:
        raise SystemExit(f"pick_date failed: {result.trace.details}")

    result = client.verify_text(id="datePickerStatus", expected=f"Picked date: {target_date}")
    if not result.ok:
        raise SystemExit(f"DatePicker date verification failed: {result.value}")
    print(f"DatePicker demo succeeded — date {target_date} selected")


if __name__ == "__main__":
    main()
