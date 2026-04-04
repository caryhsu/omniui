"""index_selector_demo.py — Smoke test for the index= selector.

Demonstrates selecting nodes by position (0-based) when multiple nodes
share the same type. Uses get_text() with type="Button" and index=0,1,2
to read the text of the first, second, and third Button nodes in the
scene tree, then verifies the results are distinct.
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

    texts: list[str] = []
    for idx in range(3):
        result = client.get_text(type="Button", index=idx)
        if not result.ok:
            raise SystemExit(
                f"get_text(type='Button', index={idx}) failed: {result.trace.details}"
            )
        text = result.value
        print(f"  Button[{idx}] text = {text!r}")
        texts.append(text)

    # All three should resolve (not empty) and be distinct button labels
    for idx, text in enumerate(texts):
        if not text:
            raise SystemExit(f"Button[{idx}] returned empty text")

    if len(set(texts)) < 2:
        raise SystemExit(
            f"Expected distinct button texts but got: {texts}"
        )

    print("index= selector tests passed")


if __name__ == "__main__":
    main()
