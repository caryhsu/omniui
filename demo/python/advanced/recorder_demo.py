"""Demonstrate the UI Recorder API: start_recording / stop_recording / save."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Start recording
    client.start_recording()
    assert client._recording, "recording flag should be True"
    print("start_recording() ok (ok)")

    # Perform some actions while recording is active (these are also captured by the agent)
    client.click(id="tbNew")
    client.click(id="tbSave")

    # Stop recording — flushes events from agent and generates script
    script = client.stop_recording()
    assert not client._recording, "recording flag should be False after stop"
    print(f"stop_recording(): {len(script.events)} events captured (ok)")
    print(f"generated script length: {len(script.script)} chars (ok)")
    assert "from omniui import OmniUI" in script.script, "script missing header"
    print("script header present (ok)")

    # Save to a temp file
    import tempfile, os
    with tempfile.NamedTemporaryFile(suffix=".py", delete=False, mode="w") as f:
        path = f.name
    try:
        script.save(path)
        content = open(path).read()
        assert "from omniui import OmniUI" in content
        print(f"script saved to {path} (ok)")
    finally:
        os.unlink(path)

    print("\nrecorder_demo succeeded (ok)")


if __name__ == "__main__":
    main()
