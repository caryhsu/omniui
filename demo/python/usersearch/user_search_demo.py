"""Demonstrate the user-search-app: search, pagination, and filtering.

Scenario:
1. Verify initial status contains "Ready"
2. Click searchButton with no filters → "Found 25 users."
3. Verify pageLabel = "Page 1 / 5"
4. Click nextButton → "Page 2 / 5"
5. Click prevButton → back to "Page 1 / 5"
6. Filter by name "Alice" → "Found 1 user."
7. Clear filter and search → "Found 25 users."
"""
from __future__ import annotations

import time

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── 1. Initial state ──────────────────────────────────────────────────────
    r = client.get_text(id="statusLabel")
    assert r.ok, f"get_text(statusLabel) failed: {r}"
    assert "Ready" in r.value, f"Expected 'Ready' in statusLabel, got {r.value!r}"
    print(f"initial statusLabel = {r.value!r} (ok)")

    # ── 2. Search all (no filter) ─────────────────────────────────────────────
    r = client.click(id="searchButton")
    assert r.ok, f"searchButton click failed: {r}"

    client.wait_for_text(id="statusLabel", expected="Found 25 users.", timeout=10.0)
    print("search all → 'Found 25 users.' (ok)")

    # ── 3. Verify page label ──────────────────────────────────────────────────
    r = client.get_text(id="pageLabel")
    assert r.ok, f"get_text(pageLabel) failed: {r}"
    assert r.value == "Page 1 / 5", f"Expected 'Page 1 / 5', got {r.value!r}"
    print(f"pageLabel = {r.value!r} (ok)")

    # ── 4. Next page ──────────────────────────────────────────────────────────
    r = client.click(id="nextButton")
    assert r.ok, f"nextButton click failed: {r}"
    time.sleep(0.2)

    r = client.get_text(id="pageLabel")
    assert r.ok
    assert r.value == "Page 2 / 5", f"Expected 'Page 2 / 5', got {r.value!r}"
    print(f"nextButton → pageLabel = {r.value!r} (ok)")

    # ── 5. Previous page ──────────────────────────────────────────────────────
    r = client.click(id="prevButton")
    assert r.ok, f"prevButton click failed: {r}"
    time.sleep(0.2)

    r = client.get_text(id="pageLabel")
    assert r.ok
    assert r.value == "Page 1 / 5", f"Expected back to 'Page 1 / 5', got {r.value!r}"
    print(f"prevButton → pageLabel = {r.value!r} (ok)")

    # ── 6. Filter by name "Alice" ─────────────────────────────────────────────
    r = client.press_key("Control+A", id="nameFilter")
    assert r.ok
    r = client.type("Alice", id="nameFilter")
    assert r.ok, f"type nameFilter failed: {r}"

    r = client.click(id="searchButton")
    assert r.ok, f"searchButton with filter failed: {r}"

    client.wait_for_text(id="statusLabel", expected="Found 1 user.", timeout=10.0)
    print("filter 'Alice' → 'Found 1 user.' (ok)")

    # ── 7. Clear and search again ─────────────────────────────────────────────
    r = client.press_key("Control+A", id="nameFilter")
    assert r.ok
    r = client.press_key("Delete", id="nameFilter")
    assert r.ok

    r = client.click(id="searchButton")
    assert r.ok

    client.wait_for_text(id="statusLabel", expected="Found 25 users.", timeout=10.0)
    print("clear filter → 'Found 25 users.' (ok)")

    print("\nuser_search_demo succeeded (ok)")


if __name__ == "__main__":
    main()
