from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()
    selection = client.select("Operator", id="roleCombo")
    if not selection.ok:
        raise SystemExit(f"ComboBox select request failed: {selection.trace.details}")
    result = client.verify_text(id="roleStatus", expected="Selected role: Operator")
    if not result.ok:
        raise SystemExit(f"ComboBox select failed: {result.value}")
    print("ComboBox selection succeeded")


if __name__ == "__main__":
    main()
