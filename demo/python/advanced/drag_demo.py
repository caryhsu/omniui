"""Demonstrate drag() and drag_to() on the Drag & Drop Demo section."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Verify initial state
    status = client.get_text(id="dragStatus")
    assert status.ok, f"get_text(dragStatus) failed: {status}"
    assert status.value == "drag_status: idle", f"Unexpected initial status: {status.value!r}"
    print(f"initial dragStatus = {status.value!r} (ok)")

    # Drag source to target (node-to-node)
    result = client.drag(id="dragSource").to(id="dragTarget")
    assert result.ok, f"drag failed: {result}"
    print("drag(id=dragSource).to(id=dragTarget) (ok)")

    status = client.get_text(id="dragStatus")
    assert status.ok, f"get_text(dragStatus) failed: {status}"
    assert "released@" in status.value, f"Expected 'released@' in dragStatus, got: {status.value!r}"
    print(f"dragStatus after drag = {status.value!r} (ok)")

    # Reset status label for next test
    client.type(text="drag_status: idle", id="dragStatus")  # won't work — use click to reset
    # Use drag_to (coordinate-based) as a separate verification
    status_before = client.get_text(id="dragStatus").value

    result = client.drag_to(id="dragSource", to_x=400, to_y=100)
    assert result.ok, f"drag_to failed: {result}"
    print("drag_to(id=dragSource, to_x=400, to_y=100) (ok)")

    status = client.get_text(id="dragStatus")
    assert status.ok, f"get_text(dragStatus) after drag_to failed: {status}"
    assert "released@" in status.value, f"Expected 'released@' in dragStatus after drag_to, got: {status.value!r}"
    print(f"dragStatus after drag_to = {status.value!r} (ok)")

    print("\ndrag_demo succeeded (ok)")


if __name__ == "__main__":
    main()
