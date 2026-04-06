from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    client.click(id="username")
    client.type("admin", id="username")

    client.click(id="password")
    client.type("1234", id="password")

    client.click(text="Login")
    result = client.verify_text(id="status", expected="Success")
    if not result.ok:
        raise SystemExit(f"Fallback login failed: {result.value}")

    print("Login with fallback succeeded")
    for entry in client.action_history():
        print(
            f"{entry.action}: tier={entry.result.trace.resolved_tier}, "
            f"attempted={entry.result.trace.attempted_tiers}"
        )


if __name__ == "__main__":
    main()
