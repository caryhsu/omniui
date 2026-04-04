"""Demonstrate focus management: focus(), tab_focus(), get_focused(), verify_focused()."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # focus() — move focus to username field
    result = client.focus(id="username")
    if not result.ok:
        raise SystemExit(f"focus(username) failed: {result.trace.details}")
    print(f"focus(username) succeeded (ok)")

    # get_focused() — verify username is focused
    focused = client.get_focused()
    assert focused.value is not None and focused.value.get("fxId") == "username", (
        f"Expected 'username' to be focused, got '{focused.value}'"
    )
    print(f"get_focused() = '{focused.value.get('fxId')}' ({focused.value.get('nodeType')}) (ok)")

    # verify_focused() — assertion helper
    client.verify_focused(id="username")
    print("verify_focused(username) passed (ok)")

    # tab_focus() — Tab to password field
    client.tab_focus()
    focused2 = client.get_focused()
    print(f"After tab_focus(): focused = '{focused2.value.get('fxId')}' (ok)")

    # tab_focus(reverse=True) — Shift+Tab back to username
    client.tab_focus(reverse=True)
    client.verify_focused(id="username")
    print("tab_focus(reverse=True) returned focus to username (ok)")

    # tab_focus(times=2) — skip to loginButton
    client.tab_focus(times=2)
    focused3 = client.get_focused()
    print(f"After tab_focus(times=2): focused = '{focused3.value.get('fxId')}' (ok)")

    print("\nfocus_demo succeeded (ok)")


if __name__ == "__main__":
    main()
