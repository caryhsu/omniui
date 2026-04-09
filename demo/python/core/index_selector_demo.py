"""index_selector_demo.py — Smoke test for the index= selector.

Demonstrates selecting nodes by position (0-based) when multiple nodes
share the same type. Uses get_text() with type="Label" and index=0,1,2
to read the text of the first three Label nodes in the core-app scene,
then verifies all three resolved successfully with distinct values.
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
        result = client.get_text(type="Label", index=idx)
        if not result.ok:
            raise SystemExit(
                f"get_text(type='Label', index={idx}) failed: {result.trace.details}"
            )
        text = result.value
        print(f"  Label[{idx}] text = {text!r}")
        texts.append(text)

    for idx, text in enumerate(texts):
        if text is None:
            raise SystemExit(f"Label[{idx}] returned None — resolve failed")

    if len(set(texts)) < 2:
        raise SystemExit(f"Expected distinct texts but got: {texts}")

    print("index= selector tests passed")


if __name__ == "__main__":
    main()
