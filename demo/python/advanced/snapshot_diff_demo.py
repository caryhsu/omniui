"""Demonstrate scene graph snapshot and diff APIs."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Take snapshot before action
    before = client.snapshot()
    assert before.nodes, "snapshot returned empty node list"
    assert before.timestamp > 0
    print(f"snapshot (before): {len(before.nodes)} nodes, timestamp ok (ok)")

    # Perform an action that changes UI state
    client.click(id="tbNew")

    # Take snapshot after action
    after = client.snapshot()
    print(f"snapshot (after): {len(after.nodes)} nodes (ok)")

    # Diff
    diff = client.diff(before, after)
    print(f"diff: added={len(diff.added)}, removed={len(diff.removed)}, changed={len(diff.changed)} (ok)")

    # At minimum the toolbar status label should have changed
    changed_ids = [
        e["after"].get("fxId") or e["after"].get("handle", "")
        for e in diff.changed
    ]
    print(f"changed node ids: {changed_ids} (ok)")

    # Identical snapshots produce empty diff
    empty_diff = client.diff(before, before)
    assert empty_diff.added == [] and empty_diff.removed == [] and empty_diff.changed == [], \
        f"Expected empty diff for identical snapshots, got {empty_diff}"
    print("diff of identical snapshots is empty (ok)")

    print("\nsnapshot_diff_demo succeeded (ok)")


if __name__ == "__main__":
    main()
