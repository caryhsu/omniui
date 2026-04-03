from __future__ import annotations

import unittest

from omniui.core.models import ActionLogEntry, ActionResult, ActionTrace, Selector
from omniui.recorder_lite import RecorderLite


def _entry(action: str, selector: Selector, tier: str, ok: bool = True) -> ActionLogEntry:
    return ActionLogEntry.from_result(
        action,
        ActionResult(
            ok=ok,
            trace=ActionTrace(
                selector=selector,
                attempted_tiers=[tier],
                resolved_tier=tier,
            ),
        ),
    )


class RecorderLiteTests(unittest.TestCase):
    def test_generates_click_by_id(self) -> None:
        recorder = RecorderLite()

        line = recorder.capture_click(_entry("click", Selector(id="loginButton"), "javafx"))

        self.assertEqual(line, 'click(id="loginButton")')

    def test_generates_click_by_type_and_text(self) -> None:
        recorder = RecorderLite()

        line = recorder.capture_click(_entry("click", Selector(text="Login", type="Button"), "javafx"))

        self.assertEqual(line, 'click(text="Login", type="Button")')

    def test_skips_unstable_click_without_selector(self) -> None:
        recorder = RecorderLite()

        line = recorder.capture_click(_entry("click", Selector(), "vision"))

        self.assertIsNone(line)

    def test_generates_script_for_supported_clicks_only(self) -> None:
        recorder = RecorderLite()
        history = [
            _entry("click", Selector(id="username"), "javafx"),
            _entry("type", Selector(id="username"), "javafx"),
            _entry("click", Selector(text="Login"), "ocr"),
        ]

        script = recorder.generate_script(history)

        self.assertEqual(script, ['click(id="username")', 'click(text="Login")'])


if __name__ == "__main__":
    unittest.main()
