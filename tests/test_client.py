"""Tests for the Python client protocol integration."""

from __future__ import annotations

import json
import io
import unittest
import unittest.mock
from urllib.error import HTTPError
from urllib.error import URLError
from unittest.mock import patch

from omniui import OmniUI
from omniui.core.engine import retry, soft_assert, SoftAssertContext
from omniui.ocr_module import SimpleOcrEngine
from omniui.vision_module import SimpleVisionEngine


class _FakeResponse:
    def __init__(self, payload: dict):
        self._payload = json.dumps(payload).encode("utf-8")

    def read(self) -> bytes:
        return self._payload

    def __enter__(self) -> "_FakeResponse":
        return self

    def __exit__(self, exc_type, exc, tb) -> None:
        return None


class OmniUiClientTests(unittest.TestCase):
    @patch("urllib.request.urlopen")
    def test_connect_raises_runtime_error_when_agent_returns_404(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            HTTPError(
                url="http://127.0.0.1:48100/sessions",
                code=404,
                msg="Not Found",
                hdrs=None,
                fp=io.BytesIO(
                    json.dumps(
                        {
                            "error": "Target app not available. Launch the JavaFX app with the OmniUI Java agent enabled."
                        }
                    ).encode("utf-8")
                ),
            ),
        ]

        with self.assertRaises(RuntimeError) as context:
            OmniUI.connect(port=48100)

        self.assertIn("Target app not available", str(context.exception))

    @patch("urllib.request.urlopen")
    def test_connect_raises_runtime_error_when_agent_is_unavailable(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = URLError("Connection refused")

        with self.assertRaises(RuntimeError) as context:
            OmniUI.connect(port=48100)

        self.assertIn("Connection refused", str(context.exception))

    @patch("urllib.request.urlopen")
    def test_connect_get_nodes_and_click_flow(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]}),
            _FakeResponse({"nodes": [{"handle": "node-login", "fxId": "loginButton", "nodeType": "Button", "text": "Login", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True}]}),
            _FakeResponse({"ok": True, "resolved": {"tier": "javafx", "targetRef": "node-login", "matchedAttributes": {"fxId": "loginButton"}, "confidence": None}, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"}, "value": None}),
        ]

        client = OmniUI.connect(port=48100)
        nodes = client.get_nodes()
        result = client.click(id="loginButton")

        self.assertEqual(nodes[0]["fxId"], "loginButton")
        self.assertTrue(result.ok)
        self.assertEqual(result.trace.resolved_tier, "javafx")
        self.assertEqual(result.resolved.target_ref, "node-login")
        self.assertEqual(len(client.action_history()), 1)
        self.assertEqual(client.action_history()[0].action, "click")

    @patch("urllib.request.urlopen")
    def test_verify_text_uses_get_text_result(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]}),
            _FakeResponse({"ok": True, "resolved": {"tier": "javafx", "targetRef": "node-status", "matchedAttributes": {"fxId": "status"}, "confidence": None}, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"}, "value": "Success"}),
        ]

        client = OmniUI.connect(port=48100)
        result = client.verify_text(id="status", expected="Success")

        self.assertTrue(result.ok)
        self.assertEqual(result.value["actual"], "Success")
        self.assertTrue(result.value["matches"])

    @patch("urllib.request.urlopen")
    def test_select_sends_selection_payload(self, mock_urlopen) -> None:
        captured_requests: list[dict[str, object]] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured_requests.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]})
            return _FakeResponse(
                {
                    "ok": True,
                    "resolved": {"tier": "javafx", "targetRef": "node-role", "matchedAttributes": {"fxId": "roleCombo", "value": "Operator"}, "confidence": None},
                    "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                    "value": "Operator",
                }
            )

        mock_urlopen.side_effect = fake_urlopen

        client = OmniUI.connect(port=48100)
        result = client.select("Operator", id="roleCombo")

        self.assertTrue(result.ok)
        self.assertEqual(captured_requests[0]["action"], "select")
        self.assertEqual(captured_requests[0]["selector"], {"id": "roleCombo"})
        self.assertEqual(captured_requests[0]["payload"], {"value": "Operator"})

    @patch("urllib.request.urlopen")
    def test_click_falls_back_to_ocr_when_javafx_resolution_fails(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]}),
            _FakeResponse({"ok": False, "resolved": None, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}}, "value": None}),
            _FakeResponse({"nodes": []}),
            _FakeResponse({"ok": False, "resolved": None, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}}, "value": None}),
            _FakeResponse({"contentType": "image/png", "encoding": "base64", "data": "TG9naW58MC45OHwxMHwyMHw5MHwzMA=="}),
        ]

        client = OmniUI.connect(port=48100, ocr_engine=SimpleOcrEngine())
        result = client.click(text="Login")

        self.assertTrue(result.ok)
        self.assertEqual(result.trace.resolved_tier, "ocr")
        self.assertEqual(result.trace.attempted_tiers, ["javafx", "refresh", "ocr"])
        self.assertAlmostEqual(result.trace.confidence, 0.98)

    @patch("urllib.request.urlopen")
    def test_click_falls_back_to_vision_when_ocr_does_not_match(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]}),
            _FakeResponse({"ok": False, "resolved": None, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}}, "value": None}),
            _FakeResponse({"nodes": []}),
            _FakeResponse({"ok": False, "resolved": None, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}}, "value": None}),
            _FakeResponse({"contentType": "image/png", "encoding": "base64", "data": "WFhYTE9HSU5fQlVUVE9OX1RFTVBMQVRFWVlZ"}),
        ]

        client = OmniUI.connect(port=48100, ocr_engine=SimpleOcrEngine(), vision_engine=SimpleVisionEngine())
        result = client.click(text="Missing", template=b"LOGIN_BUTTON_TEMPLATE")

        self.assertTrue(result.ok)
        self.assertEqual(result.trace.resolved_tier, "vision")
        self.assertEqual(result.trace.attempted_tiers, ["javafx", "refresh", "ocr", "vision"])
        self.assertGreater(result.trace.confidence, 0.0)

    @patch("urllib.request.urlopen")
    def test_action_retries_after_refresh_when_selector_is_stale(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]}),
            _FakeResponse({"ok": False, "resolved": None, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}}, "value": None}),
            _FakeResponse({"nodes": [{"handle": "node-login", "fxId": "loginButton", "nodeType": "Button", "text": "Login", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True}]}),
            _FakeResponse({"ok": True, "resolved": {"tier": "javafx", "targetRef": "node-login", "matchedAttributes": {"fxId": "loginButton"}, "confidence": None}, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"}, "value": None}),
        ]

        client = OmniUI.connect(port=48100)
        result = client.click(id="loginButton")

        self.assertTrue(result.ok)
        self.assertEqual(result.trace.attempted_tiers, ["javafx", "refresh"])
        self.assertTrue(result.trace.details["retried_after_refresh"])


class WaitUntilTests(unittest.TestCase):
    @patch("urllib.request.urlopen")
    def _make_client(self, mock_urlopen):
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "LoginDemo", "platform": "javafx", "capabilities": []}),
        ]
        return OmniUI.connect(port=48100)

    def test_wait_until_returns_when_condition_becomes_true(self) -> None:
        client = self._make_client()
        counter = {"n": 0}

        def condition():
            counter["n"] += 1
            return counter["n"] >= 3

        client.wait_until(condition, timeout=2.0, interval=0.01)
        self.assertGreaterEqual(counter["n"], 3)

    def test_wait_until_raises_timeout_error_when_condition_never_true(self) -> None:
        client = self._make_client()

        with self.assertRaises(TimeoutError) as ctx:
            client.wait_until(lambda: False, timeout=0.05, interval=0.01, message="never true")

        self.assertIn("never true", str(ctx.exception))

    def test_wait_until_tolerates_exceptions_from_condition(self) -> None:
        client = self._make_client()
        calls = {"n": 0}

        def flaky():
            calls["n"] += 1
            if calls["n"] < 3:
                raise RuntimeError("not ready")
            return True

        client.wait_until(flaky, timeout=2.0, interval=0.01)
        self.assertGreaterEqual(calls["n"], 3)


class FormatTraceTests(unittest.TestCase):
    @patch("urllib.request.urlopen")
    def _make_client(self, mock_urlopen):
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "LoginDemo", "platform": "javafx", "capabilities": []}),
        ]
        return OmniUI.connect(port=48100)

    def test_format_trace_empty(self) -> None:
        client = self._make_client()
        output = client.format_trace()
        self.assertIn("empty", output)

    @patch("urllib.request.urlopen")
    def test_format_trace_shows_action_and_status(self, mock_urlopen) -> None:
        from omniui.core.models import ActionResult, ActionTrace, Selector

        client = self._make_client()
        # Manually inject a log entry
        from omniui.core.models import ActionLogEntry
        entry = ActionLogEntry.from_result(
            "click",
            ActionResult(
                ok=True,
                trace=ActionTrace(
                    selector=Selector(id="loginBtn"),
                    attempted_tiers=["javafx"],
                    resolved_tier="javafx",
                ),
            ),
        )
        client._action_log.append(entry)

        output = client.format_trace()
        self.assertIn("click", output)
        self.assertIn("loginBtn", output)
        self.assertIn("✓", output)
        self.assertIn("javafx", output)


class LaunchTests(unittest.TestCase):
    def test_launch_raises_value_error_without_cmd_or_jar(self) -> None:
        with self.assertRaises(ValueError):
            OmniUI.launch(app_name="App")

    @patch("subprocess.Popen")
    @patch("urllib.request.urlopen")
    def test_launch_starts_process_and_connects(
        self, mock_urlopen, mock_popen
    ) -> None:
        import json as _json

        fake_proc = mock_popen.return_value
        fake_proc.poll.return_value = None

        health_resp = _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
        session_resp = _FakeResponse({"sessionId": "s-launch", "appName": "App", "platform": "javafx", "capabilities": []})
        # 1st call: health poll inside launch(); 2nd: health in connect(); 3rd: session in connect()
        mock_urlopen.side_effect = [health_resp, health_resp, session_resp]

        proc = OmniUI.launch(jar="app.jar", agent_jar="agent.jar", app_name="App")
        self.assertEqual(proc.session_id, "s-launch")
        mock_popen.assert_called_once()
        cmd = mock_popen.call_args[0][0]
        self.assertIn("-javaagent:agent.jar=port=48100", cmd)

    @patch("subprocess.Popen")
    @patch("omniui.client.request.urlopen")
    def test_launch_raises_when_process_exits_early(
        self, mock_urlopen, mock_popen
    ) -> None:
        fake_proc = mock_popen.return_value
        fake_proc.poll.return_value = 1  # already exited

        with self.assertRaises(RuntimeError, msg="exited unexpectedly"):
            OmniUI.launch(jar="app.jar", agent_jar="agent.jar", timeout=0.1)

    def test_launch_context_manager_terminates_process(self) -> None:
        from unittest.mock import MagicMock
        from omniui.core.engine import OmniUIProcess

        fake_proc = MagicMock()
        fake_proc.poll.return_value = None

        proc = OmniUIProcess(
            process=fake_proc,
            base_url="http://127.0.0.1:48100",
            session_id="s1",
        )
        with proc:
            pass

        fake_proc.terminate.assert_called_once()


if __name__ == "__main__":
    unittest.main()


class ModifierClickTests(unittest.TestCase):
    """Tests for click() with modifiers parameter."""

    def _make_client_and_capture(self, mock_urlopen):
        captured: list[dict] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []})
            return _FakeResponse({
                "ok": True,
                "resolved": {"tier": "javafx", "targetRef": "node-1", "matchedAttributes": {"fxId": "serverList"}, "confidence": None},
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": None,
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        return client, captured

    @patch("urllib.request.urlopen")
    def test_click_with_modifiers_includes_modifiers_in_payload(self, mock_urlopen) -> None:
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.click(id="serverList", modifiers=["Ctrl"])
        self.assertTrue(result.ok)
        self.assertEqual(captured[0]["action"], "click")
        self.assertEqual(captured[0]["payload"], {"modifiers": ["Ctrl"]})

    @patch("urllib.request.urlopen")
    def test_click_without_modifiers_omits_payload(self, mock_urlopen) -> None:
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.click(id="serverList")
        self.assertTrue(result.ok)
        self.assertEqual(captured[0]["action"], "click")
        # No modifiers: payload should be empty (not contain modifiers key)
        self.assertNotIn("modifiers", captured[0].get("payload", {}))


class HoverTests(unittest.TestCase):
    """Tests for hover() action."""

    @patch("urllib.request.urlopen")
    def test_hover_sends_correct_action(self, mock_urlopen) -> None:
        captured: list[dict] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []})
            return _FakeResponse({
                "ok": True,
                "resolved": {"tier": "javafx", "targetRef": "btn", "matchedAttributes": {"fxId": "tooltipBtn"}, "confidence": None},
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": None,
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        result = client.hover(id="tooltipBtn")
        self.assertTrue(result.ok)
        self.assertEqual(captured[0]["action"], "hover")
        self.assertEqual(captured[0]["selector"], {"id": "tooltipBtn"})


class FocusManagementTests(unittest.TestCase):
    """Tests for focus(), tab_focus(), get_focused(), verify_focused()."""

    def _make_client_and_capture(self, mock_urlopen, action_response=None):
        captured: list[dict] = []
        if action_response is None:
            action_response = {
                "ok": True,
                "resolved": {"tier": "javafx", "targetRef": "n1", "matchedAttributes": {"fxId": "username"}, "confidence": None},
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": None,
            }

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []})
            return _FakeResponse(action_response)

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        return client, captured

    @patch("urllib.request.urlopen")
    def test_focus_sends_correct_action(self, mock_urlopen) -> None:
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.focus(id="username")
        self.assertTrue(result.ok)
        self.assertEqual(captured[0]["action"], "focus")
        self.assertEqual(captured[0]["selector"], {"id": "username"})

    @patch("urllib.request.urlopen")
    def test_tab_focus_sends_tab_key(self, mock_urlopen) -> None:
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.tab_focus()
        self.assertEqual(captured[0]["action"], "press_key")
        self.assertEqual(captured[0]["payload"]["key"], "Tab")

    @patch("urllib.request.urlopen")
    def test_tab_focus_reverse_sends_shift_tab(self, mock_urlopen) -> None:
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.tab_focus(reverse=True)
        self.assertEqual(captured[0]["payload"]["key"], "Shift+Tab")

    @patch("urllib.request.urlopen")
    def test_tab_focus_times_sends_multiple_presses(self, mock_urlopen) -> None:
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.tab_focus(times=3)
        self.assertEqual(len(captured), 3)
        for req in captured:
            self.assertEqual(req["payload"]["key"], "Tab")

    @patch("urllib.request.urlopen")
    def test_get_focused_returns_fxid_and_nodetype(self, mock_urlopen) -> None:
        action_response = {
            "ok": True,
            "resolved": {"tier": "javafx", "targetRef": None, "matchedAttributes": {}, "confidence": None},
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
            "value": {"fxId": "username", "nodeType": "TextField"},
        }
        client, captured = self._make_client_and_capture(mock_urlopen, action_response)
        result = client.get_focused()
        self.assertTrue(result.ok)
        self.assertEqual(captured[0]["action"], "get_focused")
        self.assertEqual(result.value["fxId"], "username")
        self.assertEqual(result.value["nodeType"], "TextField")

    @patch("urllib.request.urlopen")
    def test_verify_focused_passes_when_match(self, mock_urlopen) -> None:
        action_response = {
            "ok": True,
            "resolved": {"tier": "javafx", "targetRef": None, "matchedAttributes": {}, "confidence": None},
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
            "value": {"fxId": "username", "nodeType": "TextField"},
        }
        client, _ = self._make_client_and_capture(mock_urlopen, action_response)
        client.verify_focused(id="username")  # should not raise

    @patch("urllib.request.urlopen")
    def test_verify_focused_raises_on_mismatch(self, mock_urlopen) -> None:
        action_response = {
            "ok": True,
            "resolved": {"tier": "javafx", "targetRef": None, "matchedAttributes": {}, "confidence": None},
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
            "value": {"fxId": "password", "nodeType": "PasswordField"},
        }
        client, _ = self._make_client_and_capture(mock_urlopen, action_response)
        with self.assertRaises(AssertionError):
            client.verify_focused(id="username")


class RetryHelperTests(unittest.TestCase):
    """Tests for the retry() helper (module-level and OmniUIClient.retry)."""

    def test_succeeds_first_attempt(self):
        """Callable that never raises should succeed without retry."""
        calls = {"n": 0}

        def ok():
            calls["n"] += 1

        retry(ok, times=3, delay=0)
        self.assertEqual(calls["n"], 1)

    def test_succeeds_on_nth_attempt(self):
        """Callable that fails twice then succeeds should be retried."""
        calls = {"n": 0}

        def flaky():
            calls["n"] += 1
            if calls["n"] < 3:
                raise AssertionError("not ready")

        retry(flaky, times=5, delay=0)
        self.assertEqual(calls["n"], 3)

    def test_exhausts_and_reraises(self):
        """When all attempts fail the original exception is re-raised."""
        def always_fails():
            raise ValueError("boom")

        with self.assertRaises(ValueError):
            retry(always_fails, times=3, delay=0)

    def test_action_result_ok_false_triggers_retry(self):
        """Callable returning ActionResult(ok=False) should be retried."""
        from omniui.core.engine import ActionResult

        calls = {"n": 0}
        dummy_trace = unittest.mock.MagicMock()

        def flaky() -> ActionResult:
            calls["n"] += 1
            if calls["n"] < 3:
                return ActionResult(ok=False, trace=dummy_trace, value=None)
            return ActionResult(ok=True, trace=dummy_trace, value="done")

        retry(flaky, times=5, delay=0)
        self.assertEqual(calls["n"], 3)

    def test_action_result_ok_false_exhausted_raises_assertion_error(self):
        """Exhausted ActionResult(ok=False) should raise AssertionError."""
        from omniui.core.engine import ActionResult

        dummy_trace = unittest.mock.MagicMock()

        def always_bad() -> ActionResult:
            return ActionResult(ok=False, trace=dummy_trace, value=None)

        with self.assertRaises(AssertionError):
            retry(always_bad, times=3, delay=0)

    def test_custom_exceptions(self):
        """Only exception types listed in exceptions= should trigger retry."""
        calls = {"n": 0}

        def raises_type_error():
            calls["n"] += 1
            raise TypeError("wrong type")

        with self.assertRaises(TypeError):
            retry(raises_type_error, times=3, delay=0, exceptions=(ValueError,))
        self.assertEqual(calls["n"], 1)

    @unittest.mock.patch("time.sleep")
    def test_sleep_called_between_attempts(self, mock_sleep):
        """time.sleep(delay) should be called between attempts, not before first."""
        calls = {"n": 0}

        def flaky():
            calls["n"] += 1
            if calls["n"] < 3:
                raise AssertionError("retry")

        retry(flaky, times=5, delay=0.5)
        self.assertEqual(mock_sleep.call_count, 2)
        mock_sleep.assert_called_with(0.5)

    def test_client_retry_delegates(self):
        """OmniUIClient.retry() should delegate to the module-level retry()."""
        from omniui.core.engine import OmniUIClient

        client = OmniUIClient.__new__(OmniUIClient)
        calls = {"n": 0}

        def flaky():
            calls["n"] += 1
            if calls["n"] < 2:
                raise AssertionError("retry")

        client.retry(flaky, times=5, delay=0)
        self.assertEqual(calls["n"], 2)


class SoftAssertTests(unittest.TestCase):
    """Tests for SoftAssertContext / soft_assert() / client.soft_assert()."""

    def test_no_failures_exits_cleanly(self):
        """Block with no failures should not raise."""
        with soft_assert() as sa:
            sa.check(lambda: None)

    def test_single_failure_raised_at_exit(self):
        """A single failure collected by check() should raise at block exit."""
        with self.assertRaises(AssertionError) as cm:
            with soft_assert() as sa:
                sa.check(lambda: (_ for _ in ()).throw(AssertionError("oops")))
        self.assertIn("1 assertion(s) failed:", str(cm.exception))
        self.assertIn("oops", str(cm.exception))

    def test_multiple_failures_all_collected(self):
        """All check() failures should be collected and combined."""
        with self.assertRaises(AssertionError) as cm:
            with soft_assert() as sa:
                sa.check(lambda: (_ for _ in ()).throw(AssertionError("first")))
                sa.check(lambda: (_ for _ in ()).throw(AssertionError("second")))
                sa.check(lambda: (_ for _ in ()).throw(AssertionError("third")))
        msg = str(cm.exception)
        self.assertIn("3 assertion(s) failed:", msg)
        self.assertIn("first", msg)
        self.assertIn("second", msg)
        self.assertIn("third", msg)

    def test_non_assertion_error_propagates_immediately(self):
        """Non-AssertionError inside the block should propagate immediately."""
        with self.assertRaises(RuntimeError):
            with soft_assert():
                raise RuntimeError("hard failure")

    def test_combined_message_format(self):
        """Message must match 'N assertion(s) failed:\\n  1. A\\n  2. B'."""
        with self.assertRaises(AssertionError) as cm:
            with soft_assert() as sa:
                sa.check(lambda: (_ for _ in ()).throw(AssertionError("A")))
                sa.check(lambda: (_ for _ in ()).throw(AssertionError("B")))
        msg = str(cm.exception)
        self.assertTrue(msg.startswith("2 assertion(s) failed:"))
        self.assertIn("  1. A", msg)
        self.assertIn("  2. B", msg)

    def test_client_soft_assert_delegates(self):
        """OmniUIClient.soft_assert() should return a SoftAssertContext."""
        from omniui.core.engine import OmniUIClient

        client = OmniUIClient.__new__(OmniUIClient)
        ctx = client.soft_assert()
        self.assertIsInstance(ctx, SoftAssertContext)

    def test_module_export(self):
        """soft_assert and SoftAssertContext must be importable from omniui."""
        import omniui
        self.assertTrue(hasattr(omniui, "soft_assert"))
        self.assertTrue(hasattr(omniui, "SoftAssertContext"))


class ClipboardTests(unittest.TestCase):
    """Tests for get_clipboard / set_clipboard / copy / paste."""

    def _make_client_and_capture(self, mock_urlopen, action_response=None):
        captured: list[dict] = []
        if action_response is None:
            action_response = {
                "ok": True,
                "resolved": {"tier": "javafx", "targetRef": "n1",
                             "matchedAttributes": {"fxId": "username"}, "confidence": None},
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": None,
            }

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            return _FakeResponse(action_response)

        mock_urlopen.side_effect = fake_urlopen
        from omniui.core.engine import OmniUIClient
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_get_clipboard_sends_correct_action(self, mock_urlopen):
        action_response = {
            "ok": True,
            "resolved": None,
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
            "value": "hello",
        }
        client, captured = self._make_client_and_capture(mock_urlopen, action_response)
        result = client.get_clipboard()
        self.assertTrue(result.ok)
        self.assertEqual(result.value, "hello")
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "get_clipboard")

    @patch("urllib.request.urlopen")
    def test_set_clipboard_sends_text_payload(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.set_clipboard("OmniUI")
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "set_clipboard")
        self.assertEqual(captured[0]["payload"]["text"], "OmniUI")

    @patch("urllib.request.urlopen")
    def test_copy_sends_two_press_key_actions(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.copy(id="username")
        self.assertEqual(len(captured), 2)
        self.assertEqual(captured[0]["action"], "press_key")
        self.assertEqual(captured[0]["payload"]["key"], "Control+A")
        self.assertEqual(captured[1]["action"], "press_key")
        self.assertEqual(captured[1]["payload"]["key"], "Control+C")

    @patch("urllib.request.urlopen")
    def test_paste_sends_ctrl_v(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.paste(id="username")
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "press_key")
        self.assertEqual(captured[0]["payload"]["key"], "Control+V")

    @patch("urllib.request.urlopen")
    def test_clipboard_methods_present_on_client(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        for method_name in ("get_clipboard", "set_clipboard", "copy", "paste"):
            self.assertTrue(callable(getattr(client, method_name, None)),
                            f"Missing method: {method_name}")


class ClickAtTests(unittest.TestCase):
    """Tests for OmniUIClient.click_at()."""

    def _make_client_and_capture(self, mock_urlopen):
        captured: list[dict] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            return _FakeResponse({
                "ok": True, "resolved": None,
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": {"x": 100, "y": 200},
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_click_at_sends_correct_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.click_at(x=100, y=200)
        self.assertTrue(result.ok)
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "click_at")
        self.assertEqual(captured[0]["payload"]["x"], 100)
        self.assertEqual(captured[0]["payload"]["y"], 200)

    @patch("urllib.request.urlopen")
    def test_click_at_float_coordinates(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.click_at(x=12.5, y=34.7)
        self.assertAlmostEqual(captured[0]["payload"]["x"], 12.5)
        self.assertAlmostEqual(captured[0]["payload"]["y"], 34.7)

    @patch("urllib.request.urlopen")
    def test_click_at_method_present(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        self.assertTrue(callable(getattr(client, "click_at", None)))


class TableViewTests(unittest.TestCase):
    """Tests for OmniUIClient TableView methods."""

    def _make_client_and_capture(self, mock_urlopen):
        captured: list[dict] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            return _FakeResponse({
                "ok": True, "resolved": None,
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": "Ava",
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_get_cell_sends_correct_payload(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.get_cell(id="userTable", row=0, column=1)
        self.assertTrue(result.ok)
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "get_cell")
        self.assertEqual(captured[0]["selector"]["id"], "userTable")
        self.assertEqual(captured[0]["payload"]["row"], 0)
        self.assertEqual(captured[0]["payload"]["column"], 1)

    @patch("urllib.request.urlopen")
    def test_click_cell_sends_correct_payload(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.click_cell(id="userTable", row=2, column=0)
        self.assertEqual(captured[0]["action"], "click_cell")
        self.assertEqual(captured[0]["payload"]["row"], 2)
        self.assertEqual(captured[0]["payload"]["column"], 0)

    @patch("urllib.request.urlopen")
    def test_edit_cell_sends_correct_payload(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.edit_cell(id="userTable", row=1, column=0, value="Alice")
        self.assertEqual(captured[0]["action"], "edit_cell")
        self.assertEqual(captured[0]["payload"]["row"], 1)
        self.assertEqual(captured[0]["payload"]["column"], 0)
        self.assertEqual(captured[0]["payload"]["value"], "Alice")

    @patch("urllib.request.urlopen")
    def test_sort_column_asc(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.sort_column(id="userTable", column=0, direction="asc")
        self.assertEqual(captured[0]["action"], "sort_column")
        self.assertEqual(captured[0]["payload"]["direction"], "asc")

    @patch("urllib.request.urlopen")
    def test_sort_column_no_direction(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.sort_column(id="userTable", column=0)
        self.assertIsNone(captured[0]["payload"]["direction"])

    @patch("urllib.request.urlopen")
    def test_tableview_methods_present(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        for method in ("get_cell", "click_cell", "edit_cell", "sort_column"):
            self.assertTrue(callable(getattr(client, method, None)), f"Missing: {method}")


class ToolBarTests(unittest.TestCase):
    """Tests for OmniUIClient.get_toolbar_items()."""

    def _make_client_and_capture(self, mock_urlopen):
        captured: list[dict] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            return _FakeResponse({
                "ok": True, "resolved": None,
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": [
                    {"fxId": "tbNew", "text": "New", "type": "Button", "disabled": False},
                    {"fxId": "tbSave", "text": "Save", "type": "Button", "disabled": True},
                    {"fxId": "tbSeparator", "text": "", "type": "Separator", "disabled": False},
                ],
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_get_toolbar_items_sends_correct_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.get_toolbar_items(id="mainToolBar")
        self.assertTrue(result.ok)
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "get_toolbar_items")
        self.assertEqual(captured[0]["selector"]["id"], "mainToolBar")

    @patch("urllib.request.urlopen")
    def test_get_toolbar_items_returns_list(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        result = client.get_toolbar_items(id="mainToolBar")
        self.assertIsInstance(result.value, list)
        self.assertEqual(len(result.value), 3)
        self.assertEqual(result.value[0]["fxId"], "tbNew")
        self.assertTrue(result.value[1]["disabled"])

    @patch("urllib.request.urlopen")
    def test_get_toolbar_items_method_present(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        self.assertTrue(callable(getattr(client, "get_toolbar_items", None)))


class ScrollBarTests(unittest.TestCase):
    """Tests for OmniUIClient.get_scroll_position() and set_scroll_position()."""

    def _make_client_and_capture(self, mock_urlopen, value=50.0):
        captured: list[dict] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/actions"):
                captured.append(json.loads(req.data.decode("utf-8")))
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            return _FakeResponse({
                "ok": True, "resolved": None,
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": {"value": value, "min": 0.0, "max": 100.0},
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_get_scroll_position_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.get_scroll_position(id="demoScrollBar")
        self.assertTrue(result.ok)
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "get_scroll_position")
        self.assertEqual(captured[0]["selector"]["id"], "demoScrollBar")

    @patch("urllib.request.urlopen")
    def test_get_scroll_position_returns_dict(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen, value=30.0)
        result = client.get_scroll_position(id="demoScrollBar")
        self.assertIsInstance(result.value, dict)
        self.assertEqual(result.value["value"], 30.0)
        self.assertEqual(result.value["min"], 0.0)
        self.assertEqual(result.value["max"], 100.0)

    @patch("urllib.request.urlopen")
    def test_set_scroll_position_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.set_scroll_position(id="demoScrollBar", value=75.0)
        self.assertTrue(result.ok)
        self.assertEqual(len(captured), 1)
        self.assertEqual(captured[0]["action"], "set_scroll_position")
        self.assertEqual(captured[0]["selector"]["id"], "demoScrollBar")
        self.assertEqual(captured[0]["payload"]["value"], 75.0)

    @patch("urllib.request.urlopen")
    def test_scrollbar_methods_present(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        for m in ("get_scroll_position", "set_scroll_position"):
            self.assertTrue(callable(getattr(client, m, None)), f"Missing: {m}")


class LocatorTests(unittest.TestCase):
    """Tests for OmniUIClient.locator() and the Locator class."""

    def _make_client(self, mock_urlopen):
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "LoginDemo", "platform": "javafx", "capabilities": []}),
        ]
        return OmniUI.connect(port=48100)

    @patch("urllib.request.urlopen")
    def test_locator_repr(self, mock_urlopen) -> None:
        client = self._make_client(mock_urlopen)
        loc = client.locator(id="loginBtn")
        self.assertIn("loginBtn", repr(loc))

    @patch("urllib.request.urlopen")
    def test_locator_requires_selector(self, mock_urlopen) -> None:
        client = self._make_client(mock_urlopen)
        with self.assertRaises(ValueError):
            client.locator()

    @patch("urllib.request.urlopen")
    def test_locator_click_delegates_to_client(self, mock_urlopen) -> None:
        click_resp = _FakeResponse({
            "ok": True,
            "resolved": {"tier": "javafx", "targetRef": "btn", "matchedAttributes": {"fxId": "loginBtn"}, "confidence": None},
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
            "value": None,
        })
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "LoginDemo", "platform": "javafx", "capabilities": []}),
            click_resp,
        ]
        client = OmniUI.connect(port=48100)
        loc = client.locator(id="loginBtn")
        result = loc.click()
        self.assertTrue(result.ok)

    @patch("urllib.request.urlopen")
    def test_locator_verify_text_delegates_to_client(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "LoginDemo", "platform": "javafx", "capabilities": []}),
            _FakeResponse({
                "ok": True,
                "resolved": {"tier": "javafx", "targetRef": "lbl", "matchedAttributes": {"fxId": "label"}, "confidence": None},
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": "Hello",
            }),
        ]
        client = OmniUI.connect(port=48100)
        loc = client.locator(id="label")
        result = loc.verify_text("Hello")
        self.assertTrue(result.ok)
        self.assertTrue(result.value["matches"])

    @patch("urllib.request.urlopen")
    def test_locator_wait_for_visible_requires_id(self, mock_urlopen) -> None:
        client = self._make_client(mock_urlopen)
        loc = client.locator(text="Submit")
        with self.assertRaises(ValueError) as ctx:
            loc.wait_for_visible()
        self.assertIn("id=", str(ctx.exception))

    def test_locator_exported_from_package(self) -> None:
        from omniui import Locator
        self.assertIsNotNone(Locator)
