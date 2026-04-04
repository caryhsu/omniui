from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- 寫入多行文字 --------------------------------------------------------
    text = "Hello\nWorld\nLine 3"
    client.type(text, id="demoTextArea")
    print(f"Set text: {text!r}")

    # ---- 讀回驗證 ------------------------------------------------------------
    result = client.get_text(id="demoTextArea")
    if not result.ok:
        raise SystemExit(f"get_text failed: {result.trace.details}")
    actual = result.value
    assert actual == text, f"Expected {text!r}, got {actual!r}"
    print(f"Read back: {actual!r} (ok)")

    # ---- 清空 ----------------------------------------------------------------
    client.type("", id="demoTextArea")
    result = client.get_text(id="demoTextArea")
    assert result.value == "" or result.value is None, f"Expected empty, got {result.value!r}"
    print("Cleared text (ok)")

    print("\ntext_area_demo succeeded (ok)")


if __name__ == "__main__":
    main()

