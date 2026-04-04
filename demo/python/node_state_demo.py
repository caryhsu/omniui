from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # loginButton should be visible and enabled
    assert client.is_visible(id="loginButton"), "loginButton should be visible"
    assert client.is_enabled(id="loginButton"), "loginButton should be enabled"

    # demoScrollPane should be visible
    assert client.is_visible(id="demoScrollPane"), "demoScrollPane should be visible"

    # Non-existent node should return False without raising
    assert not client.is_visible(id="__no_such_node__"), "non-existent node should return False for is_visible"
    assert not client.is_enabled(id="__no_such_node__"), "non-existent node should return False for is_enabled"

    print("node_state_demo succeeded — is_visible and is_enabled verified ✓")


if __name__ == "__main__":
    main()
