from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Set date directly (reliable, no popup navigation needed)
    target_date = "2025-09-15"
    result = client.set_date(target_date, id="demoPicker")
    if not result.ok:
        raise SystemExit(f"set_date failed: {result.trace.details}")

    result = client.verify_text(id="datePickerStatus", expected=f"Picked date: {target_date}")
    if not result.ok:
        raise SystemExit(f"DatePicker date verification failed: {result.value}")
    print(f"DatePicker demo succeeded — date {target_date} selected")


if __name__ == "__main__":
    main()
