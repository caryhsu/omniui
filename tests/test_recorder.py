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


class RecorderRunTests(unittest.TestCase):
    """Tests for Recorder GUI Run All / Run Selection logic."""

    def _make_app(self):
        """Create a RecorderApp with a mocked Tk root."""
        import sys
        if sys.platform.startswith("linux"):
            import os
            if not os.environ.get("DISPLAY") and not os.environ.get("WAYLAND_DISPLAY"):
                self.skipTest("No display available")

        import tkinter as tk
        from unittest.mock import patch
        from omniui.recorder.gui import RecorderApp
        try:
            root = tk.Tk()
            root.withdraw()
        except tk.TclError:
            self.skipTest("Tkinter display not available")
        # Suppress background port scan so it doesn't interfere with tests
        with patch("omniui.recorder.gui._scan_agents", return_value=[]):
            app = RecorderApp(root)
        self.addCleanup(root.destroy)
        return app

    def test_run_all_calls_exec_script(self) -> None:
        from unittest.mock import patch, MagicMock
        app = self._make_app()
        app._script_text.insert("1.0", 'client.click(id="btn")')
        called_with = []
        with patch.object(app, "_exec_script", side_effect=lambda c: called_with.append(c)):
            app._run_all()
        self.assertEqual(len(called_with), 1)
        self.assertIn('client.click(id="btn")', called_with[0])

    def test_run_all_empty_script_shows_warning(self) -> None:
        app = self._make_app()
        app._script_text.delete("1.0", "end")
        app._run_all()
        self.assertIn("No script", app._run_status_var.get())

    def test_run_selection_no_selection_shows_warning(self) -> None:
        app = self._make_app()
        app._script_text.insert("1.0", 'client.click(id="btn")')
        # No selection made → TclError
        app._run_selection()
        self.assertIn("No text selected", app._run_status_var.get())

    def test_run_selection_calls_exec_script_with_selection(self) -> None:
        import tkinter as tk
        from unittest.mock import patch
        app = self._make_app()
        app._script_text.insert("1.0", 'client.click(id="btn")\nclient.click(id="btn2")\n')
        app._script_text.tag_add(tk.SEL, "1.0", "1.end")
        called_with = []
        with patch.object(app, "_exec_script", side_effect=lambda c: called_with.append(c)):
            app._run_selection()
        self.assertEqual(len(called_with), 1)
        self.assertIn('client.click(id="btn")', called_with[0])

    def test_exec_script_sets_passed_status(self) -> None:
        from omniui.core.engine import OmniUIClient
        from unittest.mock import patch, MagicMock
        app = self._make_app()
        app._client = OmniUIClient(base_url="http://127.0.0.1:48100", session_id="s1")

        def sync_after(_delay, func, *args):
            func(*args)

        # Only intercept Thread created with a plain target kwarg (our run thread)
        original_thread = __import__("threading").Thread

        def sync_thread(*args, target=None, daemon=None, **kw):
            t = MagicMock()
            if target is not None and not args and not kw:
                t.start.side_effect = target
            else:
                t = original_thread(*args, target=target, daemon=daemon, **kw)
            return t

        with patch.object(app.root, "after", side_effect=sync_after), \
             patch("omniui.recorder.gui.threading.Thread", side_effect=sync_thread):
            app._exec_script("x = 1")

        self.assertIn("Passed", app._run_status_var.get())

    def test_exec_script_sets_failed_status_on_exception(self) -> None:
        from omniui.core.engine import OmniUIClient
        from unittest.mock import patch, MagicMock
        app = self._make_app()
        app._client = OmniUIClient(base_url="http://127.0.0.1:48100", session_id="s1")

        def sync_after(_delay, func, *args):
            func(*args)

        original_thread = __import__("threading").Thread

        def sync_thread(*args, target=None, daemon=None, **kw):
            t = MagicMock()
            if target is not None and not args and not kw:
                t.start.side_effect = target
            else:
                t = original_thread(*args, target=target, daemon=daemon, **kw)
            return t

        with patch.object(app.root, "after", side_effect=sync_after), \
             patch("omniui.recorder.gui.threading.Thread", side_effect=sync_thread):
            app._exec_script("raise ValueError('boom')")

        self.assertIn("Failed", app._run_status_var.get())
        self.assertIn("boom", app._run_status_var.get())

    def test_exec_script_applies_replay_delay_and_restores(self) -> None:
        """step_delay is set to replay_delay_var during exec, then restored."""
        from omniui.core.engine import OmniUIClient
        from unittest.mock import patch, MagicMock
        app = self._make_app()
        client = OmniUIClient(base_url="http://127.0.0.1:48100", session_id="s1")
        client.step_delay = 0.0
        app._client = client
        app._replay_delay_var.set("0.30")

        delays_during_exec: list[float] = []

        def sync_after(_delay, func, *args):
            func(*args)

        original_thread = __import__("threading").Thread
        import builtins as _builtins

        def sync_thread(*args, target=None, daemon=None, **kw):
            t = MagicMock()
            if target is not None and not args and not kw:
                t.start.side_effect = target
            else:
                t = original_thread(*args, target=target, daemon=daemon, **kw)
            return t

        original_exec = _builtins.exec

        def spy_exec(code, ns=None, *a):
            if ns is not None:
                delays_during_exec.append(ns.get("client", client).step_delay)
            return original_exec(code, ns, *a) if ns is not None else original_exec(code)

        with patch.object(app.root, "after", side_effect=sync_after), \
             patch("omniui.recorder.gui.threading.Thread", side_effect=sync_thread), \
             patch("builtins.exec", side_effect=spy_exec):
            app._exec_script("x = 1")

        self.assertEqual(len(delays_during_exec), 1)
        self.assertAlmostEqual(delays_during_exec[0], 0.30)
        self.assertEqual(client.step_delay, 0.0)  # restored after exec

    def test_exec_script_strips_recorder_header(self) -> None:
        """Header lines generated by the Recorder are stripped before exec.

        Without stripping, 'from omniui import OmniUI' inside the script would
        overwrite the _OmniUIStubWithProxy injected into exec globals, causing
        OmniUI.connect() to create a real client instead of returning the proxy.
        """
        from omniui.core.engine import OmniUIClient
        from unittest.mock import patch, MagicMock

        app = self._make_app()
        client = OmniUIClient(base_url="http://127.0.0.1:48100", session_id="s1")
        app._client = client

        executed_code: list[str] = []

        def sync_after(_delay, func, *args):
            func(*args)

        original_thread = __import__("threading").Thread

        def sync_thread(*args, target=None, daemon=None, **kw):
            t = MagicMock()
            if target is not None and not args and not kw:
                t.start.side_effect = target
            else:
                t = original_thread(*args, target=target, daemon=daemon, **kw)
            return t

        import builtins as _builtins
        original_exec = _builtins.exec

        def capturing_exec(code, ns=None, *a):
            if ns is not None:
                executed_code.append(code)
            return original_exec(code, ns, *a) if ns is not None else original_exec(code)

        # Use a sentinel line that doesn't call client (avoids HTTP / wait_for_node)
        script = (
            "# Generated by OmniUI Recorder\n"
            "from omniui import OmniUI\n"
            "\n"
            "client = OmniUI.connect()\n"
            "\n"
            "_sentinel = True  # marker\n"
        )

        with patch.object(app.root, "after", side_effect=sync_after), \
             patch("omniui.recorder.gui.threading.Thread", side_effect=sync_thread), \
             patch("builtins.exec", side_effect=capturing_exec):
            app._exec_script(script)

        self.assertEqual(len(executed_code), 1)
        stripped = executed_code[0]
        self.assertNotIn("from omniui import OmniUI", stripped)
        self.assertNotIn("OmniUI.connect()", stripped)
        self.assertNotIn("# Generated by OmniUI Recorder", stripped)
        self.assertIn("_sentinel = True", stripped)


class AssertionCodegenTests(unittest.TestCase):
    """Tests for generate_script with assertion event type."""

    def test_generate_script_assertion_verify_text(self) -> None:
        events = [
            RecordedEvent(
                event_type="assertion", fx_id="statusLabel", text="", node_type="Label",
                node_index=0, timestamp=0.0,
                assertion_type="verify_text", expected="Success",
            ),
        ]
        script = generate_script(events, skip_header=True)
        self.assertIn('client.verify_text(id="statusLabel", expected="Success")', script)

    def test_generate_script_assertion_verify_visible(self) -> None:
        events = [
            RecordedEvent(
                event_type="assertion", fx_id="submitBtn", text="", node_type="Button",
                node_index=0, timestamp=0.0,
                assertion_type="verify_visible", expected="",
            ),
        ]
        script = generate_script(events, skip_header=True)
        self.assertIn('client.verify_visible(id="submitBtn")', script)

    def test_generate_script_assertion_verify_enabled(self) -> None:
        events = [
            RecordedEvent(
                event_type="assertion", fx_id="submitBtn", text="", node_type="Button",
                node_index=0, timestamp=0.0,
                assertion_type="verify_enabled", expected="",
            ),
        ]
        script = generate_script(events, skip_header=True)
        self.assertIn('client.verify_enabled(id="submitBtn")', script)

    def test_generate_script_assertion_no_warn_comment(self) -> None:
        events = [
            RecordedEvent(
                event_type="assertion", fx_id="statusLabel", text="", node_type="Label",
                node_index=0, timestamp=0.0,
                assertion_type="verify_text", expected="OK",
            ),
        ]
        script = generate_script(events, skip_header=True)
        self.assertNotIn("WARN", script)

    def test_generate_script_assertion_mixed_with_click(self) -> None:
        events = [
            RecordedEvent(
                event_type="click", fx_id="loginButton", text="", node_type="Button",
                node_index=0, timestamp=0.0,
            ),
            RecordedEvent(
                event_type="assertion", fx_id="statusLabel", text="", node_type="Label",
                node_index=0, timestamp=0.1,
                assertion_type="verify_text", expected="Success",
            ),
        ]
        script = generate_script(events, skip_header=True)
        lines = [l for l in script.splitlines() if l.strip()]
        self.assertEqual(lines[0], 'client.click(id="loginButton")')
        self.assertEqual(lines[1], 'client.verify_text(id="statusLabel", expected="Success")')


if __name__ == "__main__":
    unittest.main()
