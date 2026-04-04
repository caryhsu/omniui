"""flexible_verify_text_demo.py — Smoke test for verify_text() match modes.

Tests all four modes against live nodes in the demo app:
  exact      — default, unchanged behaviour
  contains   — substring match
  starts_with — prefix match
  regex      — re.search pattern match
"""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def _check(result, label: str) -> None:
    if not result.ok:
        raise SystemExit(
            f"{label} FAILED — actual={result.value.get('actual')!r}, "
            f"expected={result.value.get('expected')!r}, mode={result.value.get('match')!r}"
        )
    print(f"  {label}  (ok)")


def main() -> None:
    client = connect_or_exit()

    # loginSectionTitle label has text "Login Flow"
    _check(
        client.verify_text("Login Flow", id="loginSectionTitle"),
        "exact: 'Login Flow'"
    )
    _check(
        client.verify_text("Login", match="contains", id="loginSectionTitle"),
        "contains: 'Login' in 'Login Flow'"
    )
    _check(
        client.verify_text("Login", match="starts_with", id="loginSectionTitle"),
        "starts_with: 'Login...'"
    )
    _check(
        client.verify_text(r"^Login \w+$", match="regex", id="loginSectionTitle"),
        r"regex: '^Login \w+$'"
    )

    # Verify unknown mode raises ValueError
    try:
        client.verify_text("x", match="fuzzy", id="loginSectionTitle")
        raise SystemExit("Expected ValueError for unknown match mode but none raised")
    except ValueError:
        print("  ValueError on unknown match mode  (ok)")

    print("flexible verify_text tests passed")


if __name__ == "__main__":
    main()

