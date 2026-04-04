from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ---- 寫入密碼 ------------------------------------------------------------
    secret = "s3cr3t!Pass"
    client.type(secret, id="demoPasswordField")
    print(f"Set password: {'*' * len(secret)}")

    # ---- 讀回應為明文 --------------------------------------------------------
    result = client.get_text(id="demoPasswordField")
    if not result.ok:
        raise SystemExit(f"get_text failed: {result.trace.details}")
    actual = result.value
    assert actual == secret, f"Expected plain text {secret!r}, got {actual!r}"
    print(f"Read back plain text: {actual!r} (ok)")

    # ---- 清空 ----------------------------------------------------------------
    client.type("", id="demoPasswordField")
    result = client.get_text(id="demoPasswordField")
    assert result.value == "" or result.value is None, f"Expected empty, got {result.value!r}"
    print("Cleared password (ok)")

    print("\npassword_field_demo succeeded (ok)")


if __name__ == "__main__":
    main()

