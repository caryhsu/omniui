from __future__ import annotations

import unittest

from omniui.core.models import ActionLogEntry, ActionResult, ActionTrace, Selector, RecordedEvent
from omniui.recorder_lite import RecorderLite
from omniui.recorder.script_gen import generate_script
from omniui.recorder.wait_injection import inject_waits


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


def _event(fx_id: str = "", node_type: str = "Button", text: str = "", index: int = 0) -> RecordedEvent:
    return RecordedEvent(
        event_type="click",
        fx_id=fx_id,
        text=text,
        node_type=node_type,
        node_index=index,
        timestamp=0.0,
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


class WaitInjectionTests(unittest.TestCase):
    def test_no_wait_for_single_event(self) -> None:
        events = [_event(fx_id="tbNew")]
        lines = ['client.click(id="tbNew")']
        result = inject_waits(events, lines)
        self.assertEqual(result, ['client.click(id="tbNew")'])

    def test_inserts_wait_for_enabled_before_button(self) -> None:
        events = [_event(fx_id="tbNew", node_type="Button"), _event(fx_id="tbSave", node_type="Button")]
        lines = ['client.click(id="tbNew")', 'client.click(id="tbSave")']
        result = inject_waits(events, lines, timeout=5.0)
        self.assertEqual(result, [
            'client.click(id="tbNew")',
            'client.wait_for_enabled(id="tbSave", timeout=5.0)',
            'client.click(id="tbSave")',
        ])

    def test_inserts_wait_for_visible_for_non_interactable(self) -> None:
        events = [_event(fx_id="btnOk", node_type="Button"), _event(fx_id="lblStatus", node_type="Label")]
        lines = ['client.click(id="btnOk")', 'client.click(id="lblStatus")']
        result = inject_waits(events, lines)
        self.assertIn('client.wait_for_visible(id="lblStatus"', result[1])

    def test_skips_wait_for_fragile_selector(self) -> None:
        events = [_event(fx_id="btnOk"), _event(fx_id="", node_type="HBox", text="")]
        lines = ['client.click(id="btnOk")', 'client.click(type="HBox", index=0)']
        result = inject_waits(events, lines)
        # No wait injected for fragile selector
        self.assertEqual(len(result), 2)

    def test_generate_script_with_wait_injection(self) -> None:
        events = [
            RecordedEvent(event_type="click", fx_id="tbNew", text="", node_type="Button", node_index=0, timestamp=0.0),
            RecordedEvent(event_type="click", fx_id="tbSave", text="", node_type="Button", node_index=0, timestamp=1.0),
        ]
        script = generate_script(events, wait_injection=True, wait_timeout=3.0)
        self.assertIn('wait_for_enabled(id="tbSave", timeout=3.0)', script)
        self.assertIn('click(id="tbNew")', script)
        self.assertIn('click(id="tbSave")', script)

    def test_generate_script_without_wait_injection_default(self) -> None:
        events = [
            RecordedEvent(event_type="click", fx_id="tbNew", text="", node_type="Button", node_index=0, timestamp=0.0),
            RecordedEvent(event_type="click", fx_id="tbSave", text="", node_type="Button", node_index=0, timestamp=1.0),
        ]
        script = generate_script(events)
        self.assertNotIn("wait_for", script)

    def test_wait_uses_text_selector_when_no_fx_id(self) -> None:
        events = [
            _event(fx_id="btn1", node_type="Button"),
            _event(fx_id="", node_type="Button", text="Save"),
        ]
        lines = ['client.click(id="btn1")', 'client.click(text="Save")']
        result = inject_waits(events, lines)
        self.assertIn('wait_for_enabled(text="Save"', result[1])


if __name__ == "__main__":
    unittest.main()
