"""css_style_demo.py — Smoke test for get_style() and get_style_class().

Verifies:
1. get_style() returns the inline style string set via setStyle()
2. get_style() returns "" for a node with no inline style
3. get_style_class() returns a list containing the default JavaFX class name
"""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # regionLabel has setStyle("-fx-text-fill: green;")
    result = client.get_style(id="regionLabel")
    if not result.ok:
        raise SystemExit(f"get_style(regionLabel) failed: {result.trace.details}")
    if "-fx-text-fill: green" not in result.value:
        raise SystemExit(f"Expected green style, got: {result.value!r}")
    print(f"  regionLabel style = {result.value!r}  (ok)")

    # roleStatus Label has no inline style
    result2 = client.get_style(id="roleStatus")
    if not result2.ok:
        raise SystemExit(f"get_style(roleStatus) failed: {result2.trace.details}")
    if result2.value != "":
        raise SystemExit(f"Expected empty style for 'roleStatus', got: {result2.value!r}")
    print(f"  roleStatus style = {result2.value!r} (empty, as expected)  (ok)")

    # auditEnabled CheckBox should have CSS class "check-box"
    result3 = client.get_style_class(id="auditEnabled")
    if not result3.ok:
        raise SystemExit(f"get_style_class(auditEnabled) failed: {result3.trace.details}")
    classes = result3.value
    if not isinstance(classes, list):
        raise SystemExit(f"Expected list, got: {type(classes)}")
    if "check-box" not in classes:
        raise SystemExit(f"Expected 'check-box' in classes, got: {classes}")
    print(f"  auditEnabled classes = {classes}  (ok)")

    print("CSS style inspection tests passed")


if __name__ == "__main__":
    main()
