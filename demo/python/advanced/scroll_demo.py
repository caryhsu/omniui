"""scroll_demo.py — Smoke test for scroll_to() and scroll_by().

Tests:
  scroll_to  — scroll innerScrollPane so scrollRow29 is visible
  scroll_by  — scroll demoScrollPane down then back to top
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
            f"{label} FAILED — result={result!r}"
        )
    print(f"  {label}  (ok)")


def main() -> None:
    client = connect_or_exit()

    # scroll_to: bring last row into view inside innerScrollPane
    _check(
        client.scroll_to(id="scrollRow29"),
        "scroll_to scrollRow29"
    )

    # scroll_by: scroll demoScrollPane down 20%
    _check(
        client.scroll_by(0.0, 0.2, id="demoScrollPane"),
        "scroll_by demoScrollPane +0.2"
    )

    # scroll_by: scroll back to top
    _check(
        client.scroll_by(0.0, -1.0, id="demoScrollPane"),
        "scroll_by demoScrollPane -1.0 (to top)"
    )

    print("scroll tests passed")


if __name__ == "__main__":
    main()

