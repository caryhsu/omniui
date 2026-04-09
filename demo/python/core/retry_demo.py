"""Demonstrate retry() helper: retrying a callable until it passes."""
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

    # Simulate a flaky check: a counter that succeeds only on the 3rd attempt
    attempt_count = {"n": 0}

    def flaky_check() -> None:
        attempt_count["n"] += 1
        if attempt_count["n"] < 3:
            raise AssertionError(
                f"Not ready yet (attempt {attempt_count['n']})"
            )
        # Succeeds on attempt 3

    client.retry(flaky_check, times=5, delay=0.05)
    print(f"client.retry() succeeded after {attempt_count['n']} attempts (ok)")

    # Module-level omniui.retry() — same behaviour
    attempt_count["n"] = 0
    omniui.retry(flaky_check, times=5, delay=0.05)
    print(f"omniui.retry() succeeded after {attempt_count['n']} attempts (ok)")

    # Verify that retry raises on exhaustion
    def always_fails() -> None:
        raise AssertionError("always fails")

    try:
        client.retry(always_fails, times=3, delay=0.01)
        raise SystemExit("Expected AssertionError was not raised")
    except AssertionError:
        print("retry correctly raises after exhausting all attempts (ok)")

    # Verify retry with a real client action (focus + verify_focused)
    client.focus(id="regionField")
    client.retry(
        lambda: client.verify_focused(id="regionField"),
        times=3,
        delay=0.1,
    )
    print("retry with verify_focused() succeeded (ok)")

    print("\nretry_demo succeeded (ok)")


if __name__ == "__main__":
    main()
