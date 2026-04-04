"""Standalone demo for close_app().

Run AFTER starting the app in with-agent mode. This script:
1. Connects and verifies the app is alive
2. Calls close_app()
3. Confirms the app is no longer reachable (connection refused)

Because close_app() terminates the JVM, this script CANNOT be included
in run_all.py — it must be run on its own as the final step of a session.
"""
from __future__ import annotations

import time
from urllib.error import URLError

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── sanity check: app is alive ────────────────────────────────────────────
    nodes = client.get_nodes()
    assert len(nodes) > 0, "Expected at least one node before close_app()"

    # ── trigger shutdown ──────────────────────────────────────────────────────
    result = client.close_app()
    assert result.ok, f"close_app() returned failure: {result}"

    # ── wait up to 5s for the JVM to exit ────────────────────────────────────
    deadline = time.monotonic() + 5.0
    app_gone = False
    while time.monotonic() < deadline:
        time.sleep(0.3)
        try:
            client.get_nodes()
        except (URLError, ConnectionRefusedError, OSError):
            app_gone = True
            break

    assert app_gone, "App was still reachable 5s after close_app() — expected ConnectionRefusedError"

    print("close_app_demo succeeded — app shut down gracefully after close_app() (ok)")


if __name__ == "__main__":
    main()

