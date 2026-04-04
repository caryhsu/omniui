"""Demonstrate soft_assert(): collect multiple assertion failures at once."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit

import omniui


def main() -> None:
    client = connect_or_exit()

    # --- Scenario 1: no failures exits cleanly ---
    with client.soft_assert() as sa:
        sa.check(lambda: client.verify_text("", id="username", match="contains"))
        # empty field contains empty string — passes
    print("soft_assert with no failures exits cleanly (ok)")

    # --- Scenario 2: multiple failures collected and reported together ---
    failures_seen = 0
    try:
        with client.soft_assert() as sa:
            sa.check(lambda: client.verify_text("__WRONG_A__", id="username"))
            sa.check(lambda: client.verify_text("__WRONG_B__", id="password"))
    except AssertionError as exc:
        msg = str(exc)
        assert msg.startswith("2 assertion(s) failed:"), f"Unexpected message: {msg}"
        failures_seen = 2
    assert failures_seen == 2, "Expected combined AssertionError"
    print("soft_assert collected 2 failures and reported them together (ok)")

    # --- Scenario 3: module-level omniui.soft_assert() ---
    failures_seen = 0
    try:
        with omniui.soft_assert() as sa:
            sa.check(lambda: client.verify_text("__WRONG__", id="username"))
    except AssertionError as exc:
        assert "1 assertion(s) failed:" in str(exc)
        failures_seen = 1
    assert failures_seen == 1
    print("omniui.soft_assert() module-level usage works (ok)")

    # --- Scenario 4: non-AssertionError still propagates immediately ---
    try:
        with client.soft_assert():
            raise RuntimeError("hard failure")
        raise SystemExit("RuntimeError should have propagated")
    except RuntimeError:
        pass
    print("non-AssertionError propagates immediately (ok)")

    print("\nsoft_assert_demo succeeded (ok)")


if __name__ == "__main__":
    main()
