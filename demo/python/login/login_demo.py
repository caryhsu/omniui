"""Login demo — runs direct and fallback login scenarios in sequence.

Usage::

    python demo/python/login/login_demo.py
"""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def _clear_login(client) -> None:
    """Clear the login fields and reset any previous status."""
    client.triple_click(id="username")
    client.type("", id="username")
    client.triple_click(id="password")
    client.type("", id="password")


def scenario_direct_login(client) -> None:
    """Scenario 1: login using fx:id selectors directly."""
    print("\n── Scenario 1: direct login ──")
    client.click(id="username")
    client.type("admin", id="username")
    client.click(id="password")
    client.type("1234", id="password")
    client.click(id="loginButton")
    result = client.verify_text(id="status", expected="Success")
    if not result.ok:
        raise SystemExit(f"Direct login failed — status: {result.value!r}")
    print("  ✓ Login succeeded")


def scenario_fallback_login(client) -> None:
    """Scenario 2: login using text/type fallback selectors."""
    print("\n── Scenario 2: fallback (text-based) login ──")
    client.click(id="username")
    client.type("admin", id="username")
    client.click(id="password")
    client.type("1234", id="password")
    client.click(text="Login")
    result = client.verify_text(id="status", expected="Success")
    if not result.ok:
        raise SystemExit(f"Fallback login failed — status: {result.value!r}")
    print("  ✓ Login succeeded")
    for entry in client.action_history():
        tier = entry.result.trace.resolved_tier
        print(f"    {entry.action}: tier={tier}")


def main() -> None:
    client = connect_or_exit()

    scenario_direct_login(client)
    scenario_fallback_login(client)

    print("\n✅ All login scenarios passed")


if __name__ == "__main__":
    main()
