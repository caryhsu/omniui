from __future__ import annotations

import unittest

from omniui.core.models import ActionLogEntry, ActionResult, ActionTrace, Selector, RecordedEvent
from omniui.recorder_lite import RecorderLite
from omniui.recorder.script_gen import generate_script
from omniui.recorder.selector_inference import infer_selector
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


class SelectorInferenceTests(unittest.TestCase):
    """Tests for selector_inference — documents fallback behaviour.

    The Java agent is expected to filter out pure layout nodes (Pane, HBox, …)
    before they reach Python. These tests document what happens if a layout-node
    event does arrive (e.g. from an older agent build) and verify the _fragile
    flag is correctly set so callers can detect it.
    """

    def test_pane_event_produces_fragile_selector(self) -> None:
        """A Pane click with no id/text falls back to the fragile selector."""
        event = _event(fx_id="", node_type="Pane", text="", index=0)
        sel = infer_selector(event)
        self.assertTrue(sel.get("_fragile"), "Pane with no id/text must be marked fragile")
        self.assertEqual(sel.get("type"), "Pane")

    def test_button_with_id_not_fragile(self) -> None:
        """A Button with an fx:id produces a stable id-based selector."""
        event = _event(fx_id="loginButton", node_type="Button")
        sel = infer_selector(event)
        self.assertFalse(sel.get("_fragile", False))
        self.assertEqual(sel.get("id"), "loginButton")

    def test_button_with_text_not_fragile(self) -> None:
        """A Button with visible text (no id) produces a text-based selector."""
        event = _event(fx_id="", node_type="Button", text="OK")
        sel = infer_selector(event)
        self.assertFalse(sel.get("_fragile", False))
        self.assertEqual(sel.get("text"), "OK")

    def test_generate_script_pane_event_has_warn_comment(self) -> None:
        """If a Pane event reaches generate_script, it emits a WARN comment."""
        pane_event = _event(fx_id="", node_type="Pane", text="", index=0)
        script = generate_script([pane_event])
        self.assertIn("# WARN: fragile selector", script)

    def test_generate_script_no_warn_for_id_based_click(self) -> None:
        """Normal id-based clicks produce no WARN comment."""
        event = _event(fx_id="loginButton", node_type="Button")
        script = generate_script([event])
        self.assertNotIn("# WARN", script)

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


class PollEventsTests(unittest.TestCase):
    """Tests for OmniUI.poll_events() — incremental event polling during recording."""

    def _make_client(self, session_id: str = "s1", base_url: str = "http://127.0.0.1:48100"):
        from omniui.core.engine import OmniUIClient
        return OmniUIClient(base_url=base_url, session_id=session_id)

    def test_poll_events_raises_when_not_recording(self) -> None:
        client = self._make_client()
        with self.assertRaises(RuntimeError):
            client.poll_events()

    def test_poll_events_returns_incremental_events(self) -> None:
        from omniui.core.engine import OmniUIClient
        from unittest.mock import patch, call

        client = self._make_client()
        client._recording = True

        batch1 = [{"type": "click", "fxId": "btn1", "text": "", "nodeType": "Button", "nodeIndex": 0, "timestamp": 1.0}]
        batch2 = [{"type": "click", "fxId": "btn2", "text": "", "nodeType": "Button", "nodeIndex": 0, "timestamp": 2.0}]

        responses = [
            {"ok": True, "events": batch1},
            {"ok": True, "events": batch2},
            {"ok": True, "events": []},
        ]
        with patch.object(client, "_request_json", side_effect=responses) as mock_req:
            result1 = client.poll_events()
            result2 = client.poll_events()
            result3 = client.poll_events()

        self.assertEqual(result1, batch1)
        self.assertEqual(result2, batch2)
        self.assertEqual(result3, [])

    def test_generate_script_skip_header(self) -> None:
        events = [
            RecordedEvent(event_type="click", fx_id="addButton", text="", node_type="Button", node_index=0, timestamp=0.0),
        ]
        partial = generate_script(events, skip_header=True)
        self.assertNotIn("# Generated by OmniUI Recorder", partial)
        self.assertNotIn("OmniUI.connect()", partial)
        self.assertIn('client.click(id="addButton")', partial)


if __name__ == "__main__":
    unittest.main()
