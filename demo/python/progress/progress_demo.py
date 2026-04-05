"""Demonstrate get_progress() and wait_for_value() on the progress-app.

Scenario:
- Verify initial state (progressBar = 0, statusLabel = idle)
- Click Run Job
- Wait for job to complete (statusLabel = done)
- Verify progressBar = 1.0
- Verify doneIndicator (checkmark) is visible
- Verify percentLabel shows 100 %
"""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── Reset to known state ─────────────────────────────────────────────────
    client.click(id="resetBtn")
    status = client.get_text(id="statusLabel")
    assert status.ok, f"get_text(statusLabel) failed: {status}"
    assert status.value == "idle", f"Expected idle after reset, got: {status.value!r}"
    print(f"initial status = {status.value!r} (ok)")

    progress = client.get_progress(id="progressBar")
    assert progress.ok, f"get_progress(progressBar) failed: {progress}"
    assert progress.value == 0.0, f"Expected 0.0, got: {progress.value}"
    print(f"initial progressBar = {progress.value} (ok)")

    # ── Start job ────────────────────────────────────────────────────────────
    result = client.click(id="runBtn")
    assert result.ok, f"click runBtn failed: {result}"
    print("clicked runBtn (ok)")

    # ── Wait for completion (up to 10 s) — raises TimeoutError on failure ────
    client.wait_for_text(id="statusLabel", expected="done", timeout=10.0)
    print("job completed (ok)")

    # ── Verify final state ───────────────────────────────────────────────────
    progress = client.get_progress(id="progressBar")
    assert progress.ok
    assert abs(progress.value - 1.0) < 0.01, f"Expected 1.0, got: {progress.value}"
    print(f"progressBar = {progress.value:.2f} (ok)")

    percent = client.get_text(id="percentLabel")
    assert percent.ok
    assert "100" in percent.value, f"Expected 100 in percentLabel, got: {percent.value!r}"
    print(f"percentLabel = {percent.value!r} (ok)")

    # doneIndicator (checkmark) should be visible
    assert client.is_visible(id="doneIndicator"), "Expected doneIndicator to be visible"
    print("doneIndicator (✓) is visible (ok)")

    # progressIndicator should be hidden
    assert not client.is_visible(id="progressIndicator"), "Expected progressIndicator to be hidden"
    print("progressIndicator is hidden (ok)")

    # get_progress on progressIndicator should also return 1.0 after job completes
    pi_prog = client.get_progress(id="progressIndicator")
    assert pi_prog.ok, f"get_progress(progressIndicator) failed: {pi_prog}"
    assert abs(pi_prog.value - 1.0) < 0.01, f"Expected progressIndicator=1.0, got: {pi_prog.value}"
    print(f"progressIndicator value = {pi_prog.value:.2f} (ok)")

    # ── Job Detail button: wait for enabled, click, read log ─────────────────
    client.wait_for_enabled(id="jobDetailBtn", timeout=3.0)
    print("jobDetailBtn is enabled (ok)")

    result = client.click(id="jobDetailBtn")
    assert result.ok, f"click jobDetailBtn failed: {result}"
    print("clicked jobDetailBtn (ok)")

    log = client.get_text(id="jobLogArea")
    assert log.ok, f"get_text(jobLogArea) failed: {log}"
    assert "Job started" in log.value, f"Expected 'Job started' in log: {log.value!r}"
    assert "SUCCESS" in log.value, f"Expected 'SUCCESS' in log: {log.value!r}"
    print(f"job log:\n{log.value}")

    print("\nprogress_demo succeeded (ok)")


if __name__ == "__main__":
    main()
