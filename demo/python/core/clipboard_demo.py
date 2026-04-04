"""Demonstrate clipboard operations: get_clipboard, set_clipboard, copy, paste."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # --- set_clipboard + get_clipboard round-trip ---
    client.set_clipboard("OmniUI-clipboard-test")
    result = client.get_clipboard()
    assert result.ok, f"get_clipboard failed: {result}"
    assert result.value == "OmniUI-clipboard-test", (
        f"Expected 'OmniUI-clipboard-test', got {result.value!r}"
    )
    print("set_clipboard / get_clipboard round-trip (ok)")

    # --- paste: write to clipboard, paste into username field ---
    client.set_clipboard("pasted_user")
    client.click(id="username")
    client.paste(id="username")
    client.verify_text("pasted_user", id="username")
    print("paste() into text field (ok)")

    # --- copy: type text, copy from field, verify clipboard ---
    client.triple_click(id="username")
    client.type("copy_source", id="username")
    client.copy(id="username")
    clipboard = client.get_clipboard()
    assert clipboard.value == "copy_source", (
        f"Expected 'copy_source', got {clipboard.value!r}"
    )
    print("copy() from text field (ok)")

    print("\nclipboard_demo succeeded (ok)")


if __name__ == "__main__":
    main()
