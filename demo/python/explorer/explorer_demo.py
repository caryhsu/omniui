"""Demonstrate the explorer-app: tree navigation and file table.

Scenario:
1. Verify initial statusBar shows count of top-level items
2. Select the "Documents" folder in folderTree
   → statusBar shows "📁 Documents"
   → fileTable shows its contents
3. Select another top-level folder "Pictures"
   → statusBar updates
4. Expand and collapse via menu items
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

    # ── 1. Initial status ─────────────────────────────────────────────────────
    r = client.get_text(id="statusBar")
    assert r.ok, f"get_text(statusBar) failed: {r}"
    assert "top-level" in r.value, f"Expected 'top-level' in statusBar, got {r.value!r}"
    print(f"initial statusBar = {r.value!r} (ok)")

    # ── 2. Select Documents folder ────────────────────────────────────────────
    r = client.select("Documents", id="folderTree")
    assert r.ok, f"select(Documents) in folderTree failed: {r}"
    time.sleep(0.3)

    r = client.get_text(id="statusBar")
    assert r.ok, f"get_text(statusBar) after select failed: {r}"
    assert "Documents" in r.value, f"Expected 'Documents' in statusBar, got {r.value!r}"
    print(f"select Documents → statusBar = {r.value!r} (ok)")

    # ── 3. Select Pictures folder ─────────────────────────────────────────────
    r = client.select("Pictures", id="folderTree")
    assert r.ok, f"select(Pictures) in folderTree failed: {r}"
    time.sleep(0.2)

    r = client.get_text(id="statusBar")
    assert r.ok, f"get_text(statusBar) after Pictures failed: {r}"
    assert "Pictures" in r.value, f"Expected 'Pictures' in statusBar, got {r.value!r}"
    print(f"select Pictures → statusBar = {r.value!r} (ok)")

    # ── 4. Expand all via menu ─────────────────────────────────────────────────
    r = client.click(id="viewMenu")
    assert r.ok, f"viewMenu click failed: {r}"
    time.sleep(0.1)

    r = client.click(id="expandAllItem")
    assert r.ok, f"expandAllItem click failed: {r}"
    time.sleep(0.2)
    print("expandAllItem clicked (ok)")

    # collapse all
    r = client.click(id="viewMenu")
    assert r.ok
    time.sleep(0.1)
    r = client.click(id="collapseAllItem")
    assert r.ok, f"collapseAllItem click failed: {r}"
    time.sleep(0.2)
    print("collapseAllItem clicked (ok)")

    print("\nexplorer_demo succeeded (ok)")


if __name__ == "__main__":
    main()
