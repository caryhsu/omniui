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

    @patch("urllib.request.urlopen")
    def test_self_heals_id_selector_to_text_after_cached_success(self, mock_urlopen) -> None:
        captured_selectors: list[dict[str, object]] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]})
            if req.full_url.endswith("/discover"):
                call_count = getattr(fake_urlopen, "_discover_calls", 0)
                fake_urlopen._discover_calls = call_count + 1
                if call_count == 0:
                    return _FakeResponse({"nodes": [
                        {"handle": "node-login-old", "fxId": "loginButton", "nodeType": "Button", "text": "Login", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True}
                    ]})
                return _FakeResponse({"nodes": [
                    {"handle": "node-login-new", "fxId": "submitButton", "nodeType": "Button", "text": "Login", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True}
                ]})
            if req.full_url.endswith("/actions"):
                body = json.loads(req.data.decode("utf-8"))
                captured_selectors.append(body.get("selector", {}))
                call_count = len(captured_selectors)
                if call_count == 1:
                    return _FakeResponse({
                        "ok": True,
                        "resolved": {"tier": "javafx", "targetRef": "node-login-old", "matchedAttributes": {"fxId": "loginButton"}, "confidence": None},
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                        "value": None,
                    })
                if call_count in (2, 3):
                    return _FakeResponse({
                        "ok": False,
                        "resolved": None,
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}},
                        "value": None,
                    })
                if call_count == 4:
                    return _FakeResponse({
                        "ok": True,
                        "resolved": {"tier": "javafx", "targetRef": "node-login-new", "matchedAttributes": {"fxId": "submitButton"}, "confidence": None},
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                        "value": None,
                    })
            raise AssertionError(f"Unexpected request: {req.full_url}")

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        loc = client.locator(id="loginButton")

        first = loc.click()
        self.assertTrue(first.ok)

        result = loc.click()

        self.assertTrue(result.ok)
        self.assertEqual(captured_selectors[3], {"text": "Login"})
        self.assertEqual(result.trace.attempted_tiers, ["javafx", "refresh", "self_heal:text"])
        self.assertEqual(result.trace.details["self_heal"]["used"], "text")

    @patch("urllib.request.urlopen")
    def test_self_heals_id_selector_to_type_index_when_text_is_unavailable(self, mock_urlopen) -> None:
        captured_selectors: list[dict[str, object]] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]})
            if req.full_url.endswith("/discover"):
                call_count = getattr(fake_urlopen, "_discover_calls", 0)
                fake_urlopen._discover_calls = call_count + 1
                if call_count == 0:
                    return _FakeResponse({"nodes": [
                        {"handle": "node-cancel", "fxId": "cancelButton", "nodeType": "Button", "text": "", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True},
                        {"handle": "node-save-old", "fxId": "saveButton", "nodeType": "Button", "text": "", "hierarchyPath": "/Scene/VBox/Button[2]", "visible": True, "enabled": True},
                    ]})
                return _FakeResponse({"nodes": [
                    {"handle": "node-cancel", "fxId": "cancelButton", "nodeType": "Button", "text": "", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True},
                    {"handle": "node-save-new", "fxId": "primaryAction", "nodeType": "Button", "text": "", "hierarchyPath": "/Scene/VBox/Button[2]", "visible": True, "enabled": True},
                ]})
            if req.full_url.endswith("/actions"):
                body = json.loads(req.data.decode("utf-8"))
                captured_selectors.append(body.get("selector", {}))
                call_count = len(captured_selectors)
                if call_count == 1:
                    return _FakeResponse({
                        "ok": True,
                        "resolved": {"tier": "javafx", "targetRef": "node-save-old", "matchedAttributes": {"fxId": "saveButton"}, "confidence": None},
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                        "value": None,
                    })
                if call_count in (2, 3):
                    return _FakeResponse({
                        "ok": False,
                        "resolved": None,
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}},
                        "value": None,
                    })
                if call_count == 4:
                    return _FakeResponse({
                        "ok": True,
                        "resolved": {"tier": "javafx", "targetRef": "node-save-new", "matchedAttributes": {"fxId": "primaryAction"}, "confidence": None},
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                        "value": None,
                    })
            raise AssertionError(f"Unexpected request: {req.full_url}")

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        loc = client.locator(id="saveButton")

        first = loc.click()
        self.assertTrue(first.ok)

        result = loc.click()

        self.assertTrue(result.ok)
        self.assertEqual(captured_selectors[3], {"type": "Button", "index": 1})
        self.assertEqual(result.trace.attempted_tiers, ["javafx", "refresh", "self_heal:type_index"])
        self.assertEqual(result.trace.details["self_heal"]["used"], "type_index")

    @patch("urllib.request.urlopen")
    def test_self_heal_hint_survives_navigation_after_successful_click(self, mock_urlopen) -> None:
        captured_selectors: list[dict[str, object]] = []

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]})
            if req.full_url.endswith("/discover"):
                call_count = getattr(fake_urlopen, "_discover_calls", 0)
                fake_urlopen._discover_calls = call_count + 1
                if call_count == 0:
                    return _FakeResponse({"nodes": [
                        {"handle": "node-login-old", "fxId": "loginButton", "nodeType": "Button", "text": "Login", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True}
                    ]})
                return _FakeResponse({"nodes": []})
            if req.full_url.endswith("/actions"):
                body = json.loads(req.data.decode("utf-8"))
                captured_selectors.append(body.get("selector", {}))
                call_count = len(captured_selectors)
                if call_count == 1:
                    return _FakeResponse({
                        "ok": True,
                        "resolved": {"tier": "javafx", "targetRef": "node-login-old", "matchedAttributes": {"fxId": "loginButton"}, "confidence": None},
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                        "value": None,
                    })
                if call_count in (2, 3):
                    return _FakeResponse({
                        "ok": False,
                        "resolved": None,
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}},
                        "value": None,
                    })
                if call_count == 4:
                    return _FakeResponse({
                        "ok": True,
                        "resolved": {"tier": "javafx", "targetRef": "node-login-new", "matchedAttributes": {"fxId": "submitButton"}, "confidence": None},
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                        "value": None,
                    })
            raise AssertionError(f"Unexpected request: {req.full_url}")

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        loc = client.locator(id="loginButton")

        self.assertTrue(loc.click().ok)
        result = loc.click()

        self.assertTrue(result.ok)
        self.assertEqual(captured_selectors[3], {"text": "Login"})

    @patch("urllib.request.urlopen")
    def test_self_heal_records_failed_fallbacks_when_no_strategy_matches(self, mock_urlopen) -> None:
        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]})
            if req.full_url.endswith("/discover"):
                call_count = getattr(fake_urlopen, "_discover_calls", 0)
                fake_urlopen._discover_calls = call_count + 1
                if call_count == 0:
                    return _FakeResponse({"nodes": [
                        {"handle": "node-login-old", "fxId": "loginButton", "nodeType": "Button", "text": "Login", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True}
                    ]})
                return _FakeResponse({"nodes": []})
            if req.full_url.endswith("/actions"):
                body = json.loads(req.data.decode("utf-8"))
                selector = body.get("selector", {})
                if selector == {"id": "loginButton"}:
                    return _FakeResponse({
                        "ok": False,
                        "resolved": None,
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}},
                        "value": None,
                    })
                if selector == {"text": "Login"} or selector == {"type": "Button", "index": 0}:
                    return _FakeResponse({
                        "ok": False,
                        "resolved": None,
                        "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None, "details": {"reason": "selector_not_found"}},
                        "value": None,
                    })
            if req.full_url.endswith("/screenshot"):
                return _FakeResponse({"contentType": "image/png", "encoding": "base64", "data": ""})
            raise AssertionError(f"Unexpected request: {req.full_url}")

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        loc = client.locator(id="loginButton")
        client._selector_heal_hints[client._selector_cache_key({"id": "loginButton", "_self_heal": True})] = {
            "captured_fx_id": "loginButton",
            "text": "Login",
            "type": "Button",
            "index": 0,
        }

        result = loc.click()

        self.assertFalse(result.ok)
        self.assertEqual(
            result.trace.attempted_tiers,
            ["javafx", "refresh", "self_heal:text", "self_heal:type_index", "ocr", "vision"],
        )
        self.assertIsNone(result.trace.details["self_heal"]["used"])


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
    @patch("omniui.client.OmniUI.find_free_port", return_value=48100)
    def test_launch_starts_process_and_connects(
        self, mock_find_port, mock_urlopen, mock_popen
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


class PaginationTests(unittest.TestCase):
    """Tests for get_page, set_page, next_page, prev_page."""

    def _make_client_and_capture(self, mock_urlopen, page=0, page_count=5):
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
                "value": {"page": page, "page_count": page_count},
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_get_page_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        result = client.get_page(id="demoPagination")
        self.assertTrue(result.ok)
        self.assertEqual(captured[0]["action"], "get_page")
        self.assertEqual(captured[0]["selector"]["id"], "demoPagination")

    @patch("urllib.request.urlopen")
    def test_get_page_returns_dict(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen, page=2, page_count=5)
        result = client.get_page(id="demoPagination")
        self.assertEqual(result.value["page"], 2)
        self.assertEqual(result.value["page_count"], 5)

    @patch("urllib.request.urlopen")
    def test_set_page_sends_payload(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.set_page(id="demoPagination", page=3)
        self.assertEqual(captured[0]["action"], "set_page")
        self.assertEqual(captured[0]["payload"]["page"], 3)

    @patch("urllib.request.urlopen")
    def test_next_page_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.next_page(id="demoPagination")
        self.assertEqual(captured[0]["action"], "next_page")
        self.assertEqual(captured[0]["selector"]["id"], "demoPagination")

    @patch("urllib.request.urlopen")
    def test_prev_page_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.prev_page(id="demoPagination")
        self.assertEqual(captured[0]["action"], "prev_page")

    @patch("urllib.request.urlopen")
    def test_pagination_methods_present(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        for m in ("get_page", "set_page", "next_page", "prev_page"):
            self.assertTrue(callable(getattr(client, m, None)), f"Missing: {m}")


class WindowTests(unittest.TestCase):
    """Tests for Window / Stage management APIs."""

    def _make_client_and_capture(self, mock_urlopen, value=None):
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
                "value": value,
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_get_windows_action(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen, value=["Main Window"])
        result = client.get_windows()
        self.assertTrue(result.ok)
        self.assertEqual(captured[0]["action"], "get_windows")

    @patch("urllib.request.urlopen")
    def test_focus_window_sends_title(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.focus_window(title="Main Window")
        self.assertEqual(captured[0]["action"], "focus_window")
        self.assertEqual(captured[0]["payload"]["title"], "Main Window")

    @patch("urllib.request.urlopen")
    def test_maximize_restore_window(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.maximize_window(title="Main Window")
        self.assertEqual(captured[0]["action"], "maximize_window")
        client.restore_window(title="Main Window")
        self.assertEqual(captured[1]["action"], "restore_window")

    @patch("urllib.request.urlopen")
    def test_set_window_size_payload(self, mock_urlopen):
        client, captured = self._make_client_and_capture(mock_urlopen)
        client.set_window_size(title="Main Window", width=800, height=600)
        self.assertEqual(captured[0]["action"], "set_window_size")
        self.assertEqual(captured[0]["payload"]["width"], 800)
        self.assertEqual(captured[0]["payload"]["height"], 600)

    @patch("urllib.request.urlopen")
    def test_get_window_size_returns_dict(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen, value={"width": 720.0, "height": 760.0})
        result = client.get_window_size(title="Main Window")
        self.assertEqual(result.value["width"], 720.0)

    @patch("urllib.request.urlopen")
    def test_window_methods_present(self, mock_urlopen):
        client, _ = self._make_client_and_capture(mock_urlopen)
        for m in ("get_windows", "focus_window", "maximize_window", "minimize_window",
                  "restore_window", "is_maximized", "is_minimized",
                  "set_window_size", "set_window_position",
                  "get_window_size", "get_window_position"):
            self.assertTrue(callable(getattr(client, m, None)), f"Missing: {m}")


class WithinTests(unittest.TestCase):
    """Tests for client.within() scoped selector context manager."""

    def _make_client(self, mock_urlopen):
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
                "value": "ok",
            })

        mock_urlopen.side_effect = fake_urlopen
        client = OmniUI.connect(port=48100)
        captured.clear()
        return client, captured

    @patch("urllib.request.urlopen")
    def test_within_injects_scope_into_selector(self, mock_urlopen):
        client, captured = self._make_client(mock_urlopen)
        with client.within(id="leftPanel"):
            client.click(id="panelOkBtn")
        sel = captured[0]["selector"]
        self.assertEqual(sel.get("id"), "panelOkBtn")
        self.assertEqual(sel.get("scope"), {"id": "leftPanel"})

    @patch("urllib.request.urlopen")
    def test_within_scope_cleared_after_context(self, mock_urlopen):
        client, _ = self._make_client(mock_urlopen)
        with client.within(id="leftPanel"):
            pass
        self.assertIsNone(client._scope)

    @patch("urllib.request.urlopen")
    def test_without_within_no_scope_field(self, mock_urlopen):
        client, captured = self._make_client(mock_urlopen)
        client.click(id="panelOkBtn")
        sel = captured[0]["selector"]
        self.assertNotIn("scope", sel)

    @patch("urllib.request.urlopen")
    def test_within_multiple_actions_same_scope(self, mock_urlopen):
        client, captured = self._make_client(mock_urlopen)
        with client.within(id="myContainer"):
            client.click(id="btn1")
            client.get_text(id="lbl1")
        self.assertEqual(captured[0]["selector"]["scope"], {"id": "myContainer"})
        self.assertEqual(captured[1]["selector"]["scope"], {"id": "myContainer"})

    @patch("urllib.request.urlopen")
    def test_within_scope_cleared_on_exception(self, mock_urlopen):
        client, _ = self._make_client(mock_urlopen)
        try:
            with client.within(id="leftPanel"):
                raise ValueError("test error")
        except ValueError:
            pass
        self.assertIsNone(client._scope)


class LocatorTests(unittest.TestCase):
    """Tests for OmniUIClient.locator() and the Locator class."""

    def _make_client(self, mock_urlopen):
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "LoginDemo", "platform": "javafx", "capabilities": []}),
        ]
        return OmniUI.connect(port=48100)

    def _make_locator_with_mock_client(self, **selector):
        from omniui.locator import Locator
        client = unittest.mock.Mock()
        return client, Locator(client, **selector)

    def _assert_locator_delegate(self, locator_method: str, client_method: str, *args, selector=None, **kwargs) -> None:
        selector = selector or {"id": "loginBtn"}
        client, loc = self._make_locator_with_mock_client(**selector)
        expected = object()
        getattr(client, client_method).return_value = expected

        result = getattr(loc, locator_method)(*args, **kwargs)

        self.assertIs(result, expected)
        getattr(client, client_method).assert_called_once_with(
            *args,
            **{**selector, "_self_heal": True, **kwargs},
        )

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
            _FakeResponse({"nodes": [{"handle": "btn", "fxId": "loginBtn", "nodeType": "Button", "text": "Login"}]}),
            click_resp,
        ]
        client = OmniUI.connect(port=48100)
        loc = client.locator(id="loginBtn")
        result = loc.click()
        self.assertTrue(result.ok)

    @patch("urllib.request.urlopen")
    def test_find_preserves_index_selector(self, mock_urlopen) -> None:
        client = self._make_client(mock_urlopen)
        self.assertEqual(client.find(type="Button", index=2), {"type": "Button", "index": 2})

    @patch("urllib.request.urlopen")
    def test_find_rejects_bool_index_selector(self, mock_urlopen) -> None:
        client = self._make_client(mock_urlopen)
        with self.assertRaises(ValueError):
            client.find(type="Button", index=True)

    def test_locator_action_methods_delegate_to_client(self) -> None:
        cases = [
            ("click", "click", (), {}),
            ("double_click", "double_click", (), {}),
            ("right_click", "right_click", (), {}),
            ("press_key", "press_key", ("Enter",), {}),
            ("type", "type", ("hello",), {}),
            ("get_text", "get_text", (), {}),
            ("verify_text", "verify_text", ("Hello",), {"match": "contains"}),
            ("get_tooltip", "get_tooltip", (), {}),
            ("get_style", "get_style", (), {}),
            ("get_style_class", "get_style_class", (), {}),
            ("get_value", "get_value", (), {}),
            ("get_progress", "get_progress", (), {}),
            ("get_image_url", "get_image_url", (), {}),
            ("open_colorpicker", "open_colorpicker", (), {}),
            ("set_color", "set_color", ("#ff0000",), {}),
            ("get_color", "get_color", (), {}),
            ("get_selected", "get_selected", (), {}),
            ("get_selected_items", "get_selected_items", (), {}),
            ("select", "select", ("Admin",), {}),
            ("select_multiple", "select_multiple", (["A", "B"],), {}),
            ("set_selected", "set_selected", (True,), {}),
            ("set_slider", "set_slider", (0.75,), {}),
            ("set_spinner", "set_spinner", ("42",), {}),
            ("step_spinner", "step_spinner", (2,), {}),
            ("get_tabs", "get_tabs", (), {}),
            ("select_tab", "select_tab", ("Settings",), {}),
            ("scroll_to", "scroll_to", (), {}),
            ("scroll_by", "scroll_by", (0.25, 0.5), {}),
            ("expand_pane", "expand_pane", (), {}),
            ("collapse_pane", "collapse_pane", (), {}),
            ("get_expanded", "get_expanded", (), {}),
        ]

        for locator_method, client_method, args, kwargs in cases:
            with self.subTest(locator_method=locator_method):
                self._assert_locator_delegate(locator_method, client_method, *args, **kwargs)

    def test_locator_boolean_methods_delegate_to_client(self) -> None:
        cases = [
            ("is_visible", "is_visible"),
            ("is_enabled", "is_enabled"),
            ("is_visited", "is_visited"),
            ("is_image_loaded", "is_image_loaded"),
        ]

        for locator_method, client_method in cases:
            with self.subTest(locator_method=locator_method):
                self._assert_locator_delegate(locator_method, client_method)

    def test_locator_wait_methods_delegate_to_client_with_id(self) -> None:
        client, loc = self._make_locator_with_mock_client(id="field")

        loc.wait_for_visible(timeout=1.5)
        loc.wait_for_enabled(timeout=2.0)
        loc.wait_for_node(timeout=2.5)
        loc.wait_for_text("Ready", timeout=3.0)
        loc.wait_for_value("42", timeout=3.5)

        client.wait_for_visible.assert_called_once_with("field", timeout=1.5)
        client.wait_for_enabled.assert_called_once_with("field", timeout=2.0)
        client.wait_for_node.assert_called_once_with("field", timeout=2.5)
        client.wait_for_text.assert_called_once_with("field", "Ready", timeout=3.0)
        client.wait_for_value.assert_called_once_with("field", "42", timeout=3.5)


class StepDelayTests(unittest.TestCase):
    def _make_client(self, mock_urlopen, step_delay: float = 0.0):
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []}),
        ]
        return OmniUI.connect(port=48100, step_delay=step_delay)

    def _click_response(self):
        return _FakeResponse({
            "ok": True,
            "resolved": {"tier": "javafx", "targetRef": "btn", "matchedAttributes": {"fxId": "btn"}, "confidence": None},
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
            "value": None,
        })

    @patch("urllib.request.urlopen")
    def test_step_delay_default_is_zero(self, mock_urlopen) -> None:
        client = self._make_client(mock_urlopen)
        self.assertEqual(client.step_delay, 0.0)

    @patch("urllib.request.urlopen")
    def test_connect_sets_step_delay(self, mock_urlopen) -> None:
        client = self._make_client(mock_urlopen, step_delay=0.5)
        self.assertEqual(client.step_delay, 0.5)

    @patch("time.sleep")
    @patch("urllib.request.urlopen")
    def test_global_step_delay_sleeps_after_action(self, mock_urlopen, mock_sleep) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []}),
            self._click_response(),
        ]
        client = OmniUI.connect(port=48100, step_delay=0.5)
        client.click(id="btn")
        mock_sleep.assert_called_with(0.5)

    @patch("time.sleep")
    @patch("urllib.request.urlopen")
    def test_per_call_delay_overrides_global(self, mock_urlopen, mock_sleep) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []}),
            self._click_response(),
        ]
        client = OmniUI.connect(port=48100, step_delay=0.5)
        client.click(id="btn", delay=1.5)
        mock_sleep.assert_called_with(1.5)

    @patch("time.sleep")
    @patch("urllib.request.urlopen")
    def test_no_sleep_when_step_delay_is_zero(self, mock_urlopen, mock_sleep) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []}),
            self._click_response(),
        ]
        client = OmniUI.connect(port=48100, step_delay=0.0)
        client.click(id="btn")
        mock_sleep.assert_not_called()


    @patch("urllib.request.urlopen")
    def test_locator_verify_text_delegates_to_client(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "s1", "appName": "LoginDemo", "platform": "javafx", "capabilities": []}),
            _FakeResponse({"nodes": [{"handle": "lbl", "fxId": "label", "nodeType": "Label", "text": "Hello"}]}),
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


class SnapshotDiffTests(unittest.TestCase):
    """Tests for UISnapshot / UIDiff dataclasses and OmniUIClient.snapshot() / diff()."""

    _NODES_A = [
        {"fxId": "loginBtn", "nodeType": "Button", "text": "Login", "enabled": True, "visible": True, "value": None},
        {"fxId": "username",  "nodeType": "TextField", "text": "", "enabled": True, "visible": True, "value": ""},
        {"fxId": "status",    "nodeType": "Label",   "text": "idle", "enabled": True, "visible": True, "value": None},
    ]

    _NODES_B = [
        {"fxId": "loginBtn", "nodeType": "Button", "text": "Login", "enabled": True, "visible": True, "value": None},
        {"fxId": "username",  "nodeType": "TextField", "text": "alice", "enabled": True, "visible": True, "value": "alice"},
        {"fxId": "status",    "nodeType": "Label",   "text": "logged in", "enabled": True, "visible": True, "value": None},
        {"fxId": "newWidget", "nodeType": "Label",   "text": "hi", "enabled": True, "visible": True, "value": None},
    ]

    def _make_client(self, mock_urlopen):
        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            if req.full_url.endswith("/discover"):
                call_count = getattr(fake_urlopen, "_discover_calls", 0)
                fake_urlopen._discover_calls = call_count + 1
                if call_count == 0:
                    return _FakeResponse({"nodes": self._NODES_A})
                return _FakeResponse({"nodes": self._NODES_B})
            return _FakeResponse({
                "ok": True, "resolved": None,
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": "ok",
            })

        mock_urlopen.side_effect = fake_urlopen
        return OmniUI.connect(port=48100)

    @patch("urllib.request.urlopen")
    def test_snapshot_returns_nodes(self, mock_urlopen):
        client = self._make_client(mock_urlopen)
        snap = client.snapshot()
        self.assertEqual(len(snap.nodes), 3)
        self.assertGreater(snap.timestamp, 0)

    @patch("urllib.request.urlopen")
    def test_diff_added_removed_changed(self, mock_urlopen):
        client = self._make_client(mock_urlopen)
        before = client.snapshot()
        after  = client.snapshot()
        d = client.diff(before, after)
        self.assertEqual(len(d.added),   1, "newWidget should be added")
        self.assertEqual(len(d.removed), 0)
        self.assertEqual(len(d.changed), 2, "username and status should be changed")

    @patch("urllib.request.urlopen")
    def test_diff_identical_snapshots_empty(self, mock_urlopen):
        client = self._make_client(mock_urlopen)
        snap = client.snapshot()
        d = client.diff(snap, snap)
        self.assertEqual(d.added,   [])
        self.assertEqual(d.removed, [])
        self.assertEqual(d.changed, [])

    @patch("urllib.request.urlopen")
    def test_diff_changed_contains_before_and_after(self, mock_urlopen):
        client = self._make_client(mock_urlopen)
        before = client.snapshot()
        after  = client.snapshot()
        d = client.diff(before, after)
        for entry in d.changed:
            self.assertIn("before", entry)
            self.assertIn("after",  entry)

    @patch("urllib.request.urlopen")
    def test_snapshot_exported_from_package(self, mock_urlopen):
        from omniui import UISnapshot, UIDiff
        self.assertIsNotNone(UISnapshot)
        self.assertIsNotNone(UIDiff)

    @patch("urllib.request.urlopen")
    def test_snapshot_save_and_load(self, mock_urlopen):
        import tempfile, os
        from omniui.core.models import UISnapshot
        client = self._make_client(mock_urlopen)
        snap = client.snapshot()
        with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as f:
            path = f.name
        try:
            snap.save(path)
            loaded = UISnapshot.load(path)
            self.assertEqual(loaded.nodes, snap.nodes)
            self.assertAlmostEqual(loaded.timestamp, snap.timestamp, places=3)
        finally:
            os.unlink(path)


# ── RecorderTests ────────────────────────────────────────────────────────────

class SelectorInferenceTests(unittest.TestCase):
    """Tests for omniui.recorder.selector_inference.infer_selector."""

    def _event(self, fx_id="", text="", node_type="Button", node_index=0):
        from omniui.core.models import RecordedEvent
        return RecordedEvent(event_type="click", fx_id=fx_id, text=text,
                             node_type=node_type, node_index=node_index, timestamp=1.0)

    def test_infer_fxid(self):
        from omniui.recorder import infer_selector
        sel = infer_selector(self._event(fx_id="loginBtn"))
        self.assertEqual(sel, {"id": "loginBtn"})

    def test_infer_text_fallback(self):
        from omniui.recorder import infer_selector
        sel = infer_selector(self._event(text="Submit"))
        self.assertEqual(sel, {"text": "Submit"})

    def test_infer_type_index_fragile(self):
        from omniui.recorder import infer_selector
        sel = infer_selector(self._event())
        self.assertEqual(sel["type"], "Button")
        self.assertTrue(sel.get("_fragile"))

    def test_long_text_falls_back_to_type_index(self):
        from omniui.recorder import infer_selector
        sel = infer_selector(self._event(text="x" * 41))
        self.assertTrue(sel.get("_fragile"))


class ScriptGenTests(unittest.TestCase):
    """Tests for omniui.recorder.script_gen.generate_script."""

    def _event(self, event_type, fx_id="", text="", node_type="Button", node_index=0):
        from omniui.core.models import RecordedEvent
        return RecordedEvent(event_type=event_type, fx_id=fx_id, text=text,
                             node_type=node_type, node_index=node_index, timestamp=1.0)

    def test_script_header(self):
        from omniui.recorder import generate_script
        script = generate_script([self._event("click", fx_id="btn")])
        self.assertIn("from omniui import OmniUI", script)
        self.assertIn("Generated by OmniUI Recorder", script)

    def test_click_event_output(self):
        from omniui.recorder import generate_script
        script = generate_script([self._event("click", fx_id="loginBtn")])
        self.assertIn('client.click(id="loginBtn")', script)

    def test_type_event_output(self):
        from omniui.recorder import generate_script
        script = generate_script([self._event("type", fx_id="username", text="alice")])
        self.assertIn('client.type(id="username", text="alice")', script)

    def test_fragile_selector_warning(self):
        from omniui.recorder import generate_script
        script = generate_script([self._event("click")])
        self.assertIn("WARN: fragile selector", script)


class RecorderClientTests(unittest.TestCase):
    """Tests for OmniUIClient.start_recording() / stop_recording()."""

    def _make_client(self, mock_urlopen, events=None):
        if events is None:
            events = [{"type": "click", "fxId": "loginBtn", "text": "", "nodeType": "Button", "nodeIndex": 0, "timestamp": 1.0}]

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            if req.full_url.endswith("/events/start"):
                return _FakeResponse({"ok": True})
            if req.full_url.endswith("/events"):
                return _FakeResponse({"ok": True, "events": events})
            return _FakeResponse({"ok": True, "resolved": None,
                                   "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                                   "value": "ok"})

        mock_urlopen.side_effect = fake_urlopen
        return OmniUI.connect(port=48100)

    @patch("urllib.request.urlopen")
    def test_start_recording_sets_flag(self, mock_urlopen):
        client = self._make_client(mock_urlopen)
        client.start_recording()
        self.assertTrue(client._recording)

    @patch("urllib.request.urlopen")
    def test_stop_recording_returns_script(self, mock_urlopen):
        from omniui import RecordedScript
        client = self._make_client(mock_urlopen)
        client.start_recording()
        result = client.stop_recording()
        self.assertIsInstance(result, RecordedScript)
        self.assertEqual(len(result.events), 1)
        self.assertIn('client.click(id="loginBtn")', result.script)
        self.assertFalse(client._recording)

    @patch("urllib.request.urlopen")
    def test_stop_without_start_raises(self, mock_urlopen):
        client = self._make_client(mock_urlopen)
        with self.assertRaises(RuntimeError):
            client.stop_recording()

    @patch("urllib.request.urlopen")
    def test_recorded_script_save(self, mock_urlopen):
        import tempfile, os
        client = self._make_client(mock_urlopen)
        client.start_recording()
        result = client.stop_recording()
        with tempfile.NamedTemporaryFile(suffix=".py", delete=False) as f:
            path = f.name
        try:
            result.save(path)
            content = open(path).read()
            self.assertIn("client.click", content)
        finally:
            os.unlink(path)


class DragAndDropTests(unittest.TestCase):
    """Tests for drag() / drag_to() and _DragBuilder."""

    def _make_client_and_capture(self, mock_urlopen):
        calls = []

        def side_effect(req, timeout=None):
            url = req.full_url
            body = json.loads(req.data.decode()) if req.data else {}
            calls.append({"url": url, "body": body})
            if url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []})
            return _FakeResponse({
                "ok": True,
                "resolved": {"tier": "javafx", "targetRef": "n1", "matchedAttributes": {}, "confidence": None},
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": None,
            })

        mock_urlopen.side_effect = side_effect
        client = OmniUI.connect(port=48100)
        return client, calls

    @patch("urllib.request.urlopen")
    def test_drag_to_sends_correct_action(self, mock_urlopen) -> None:
        client, calls = self._make_client_and_capture(mock_urlopen)
        client.drag_to(id="handle", to_x=300.0, to_y=200.0)
        action_call = calls[-1]["body"]
        self.assertEqual(action_call["action"], "drag_to")
        self.assertEqual(action_call["selector"], {"id": "handle"})
        self.assertEqual(action_call["payload"]["to_x"], 300.0)
        self.assertEqual(action_call["payload"]["to_y"], 200.0)

    @patch("urllib.request.urlopen")
    def test_drag_builder_to_sends_correct_action(self, mock_urlopen) -> None:
        client, calls = self._make_client_and_capture(mock_urlopen)
        client.drag(id="srcItem").to(id="dstItem")
        action_call = calls[-1]["body"]
        self.assertEqual(action_call["action"], "drag")
        self.assertEqual(action_call["selector"], {"id": "srcItem"})
        self.assertEqual(action_call["payload"]["target"], {"id": "dstItem"})

    @patch("urllib.request.urlopen")
    def test_drag_builder_to_coords_sends_correct_action(self, mock_urlopen) -> None:
        client, calls = self._make_client_and_capture(mock_urlopen)
        client.drag(id="handle").to_coords(x=150.0, y=75.0)
        action_call = calls[-1]["body"]
        self.assertEqual(action_call["action"], "drag_to")
        self.assertEqual(action_call["selector"], {"id": "handle"})
        self.assertEqual(action_call["payload"]["to_x"], 150.0)
        self.assertEqual(action_call["payload"]["to_y"], 75.0)

    @patch("time.sleep")
    @patch("urllib.request.urlopen")
    def test_drag_respects_step_delay(self, mock_urlopen, mock_sleep) -> None:
        client, _ = self._make_client_and_capture(mock_urlopen)
        client.step_delay = 0.3
        client.drag(id="a").to(id="b")
        mock_sleep.assert_called_with(0.3)

    @patch("time.sleep")
    @patch("urllib.request.urlopen")
    def test_drag_per_call_delay_override(self, mock_urlopen, mock_sleep) -> None:
        client, _ = self._make_client_and_capture(mock_urlopen)
        client.step_delay = 0.3
        client.drag(id="a", delay=1.0).to(id="b")
        mock_sleep.assert_called_with(1.0)


class DragRecordingTests(unittest.TestCase):
    """Tests for drag event recording in script_gen, models, and wait_injection."""

    def _drag_event(self, fx_id="item_apple", text="Apple",
                    to_fx_id="rightPanel", to_text="Selected"):
        from omniui.core.models import RecordedEvent
        return RecordedEvent(
            event_type="drag",
            fx_id=fx_id, text=text,
            node_type="Label", node_index=0,
            timestamp=1.0,
            to_fx_id=to_fx_id, to_text=to_text,
            to_node_type="VBox", to_node_index=0,
        )

    def test_drag_event_script_gen_by_id(self):
        from omniui.recorder import generate_script
        script = generate_script([self._drag_event()])
        self.assertIn('client.drag(id="item_apple").to(id="rightPanel")', script)

    def test_drag_event_script_gen_by_text_fallback(self):
        from omniui.recorder import generate_script
        # No fx_id → fall back to text
        event = self._drag_event(fx_id="", to_fx_id="")
        script = generate_script([event])
        self.assertIn('client.drag(text="Apple").to(text="Selected")', script)

    def test_drag_event_script_gen_fragile(self):
        from omniui.recorder import generate_script
        # No fx_id, no text → fragile
        event = self._drag_event(fx_id="", text="", to_fx_id="", to_text="")
        script = generate_script([event])
        self.assertIn("WARN: fragile selector", script)

    def test_drag_wait_injection_uses_wait_for_visible(self):
        from omniui.recorder import generate_script
        # Two drag events; wait_injection should insert wait_for_visible before second
        e1 = self._drag_event(fx_id="item_apple")
        e2 = self._drag_event(fx_id="item_banana")
        script = generate_script([e1, e2], wait_injection=True)
        self.assertIn('client.wait_for_visible(id="item_banana"', script)

    def test_recorded_event_to_fields_populated(self):
        from omniui.core.models import RecordedEvent
        e = RecordedEvent(
            event_type="drag", fx_id="a", text="A",
            node_type="Label", node_index=0, timestamp=0.0,
            to_fx_id="b", to_text="B", to_node_type="VBox", to_node_index=1,
        )
        self.assertEqual(e.to_fx_id, "b")
        self.assertEqual(e.to_node_index, 1)

    def test_recorder_client_parses_drag_event(self):
        from unittest.mock import patch
        from omniui import OmniUI

        drag_raw = {
            "type": "drag",
            "fxId": "item_apple", "text": "Apple",
            "nodeType": "Label", "nodeIndex": 0, "timestamp": 1.0,
            "toFxId": "rightPanel", "toText": "Selected",
            "toNodeType": "VBox", "toNodeIndex": 0,
        }

        def fake_urlopen(req, **_kw):
            if req.full_url.endswith("/health"):
                return _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"})
            if req.full_url.endswith("/sessions"):
                return _FakeResponse({"sessionId": "s1", "appName": "App",
                                       "platform": "javafx", "capabilities": []})
            if req.full_url.endswith("/events/start"):
                return _FakeResponse({"ok": True})
            if req.full_url.endswith("/events"):
                return _FakeResponse({"ok": True, "events": [drag_raw]})
            return _FakeResponse({"ok": True, "resolved": None,
                                   "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                                   "value": "ok"})

        with patch("urllib.request.urlopen", side_effect=fake_urlopen):
            client = OmniUI.connect(port=48100)
            client.start_recording()
            result = client.stop_recording()

        self.assertEqual(len(result.events), 1)
        self.assertEqual(result.events[0].event_type, "drag")
        self.assertEqual(result.events[0].to_fx_id, "rightPanel")
        self.assertIn('client.drag(id="item_apple").to(id="rightPanel")', result.script)


class ScreenshotModeTests(unittest.TestCase):
    """Tests for screenshot_mode=on_failure / always auto-capture."""

    def _make_client(self, mode: str, tmp_dir: str) -> "OmniUIClient":
        from omniui.core.engine import OmniUIClient
        client = OmniUIClient(
            base_url="http://127.0.0.1:48100",
            session_id="s1",
            screenshot_mode=mode,
            screenshot_dir=tmp_dir,
        )
        return client

    def _fake_urlopen_ok(self, req, **kw):
        url = req.full_url if hasattr(req, "full_url") else str(req)
        if "screenshot" in url:
            import base64, io
            png_1x1 = (
                b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01'
                b'\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00'
                b'\x00\x00\x0cIDATx\x9cc\xf8\x0f\x00\x00\x01\x01\x00'
                b'\x05\x18\xd8N\x00\x00\x00\x00IEND\xaeB`\x82'
            )
            return _FakeResponse({"data": base64.b64encode(png_1x1).decode(), "contentType": "image/png"})
        return _FakeResponse({
            "ok": True,
            "resolved": {"tier": "javafx", "targetRef": "btn", "fxId": "btn", "nodeType": "Button", "text": "OK"},
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
            "value": "ok",
        })

    def _fake_urlopen_fail(self, req, **kw):
        url = req.full_url if hasattr(req, "full_url") else str(req)
        if "screenshot" in url:
            return self._fake_urlopen_ok(req, **kw)
        return _FakeResponse({
            "ok": False,
            "resolved": None,
            "trace": {"attemptedTiers": ["javafx"], "resolvedTier": None},
            "value": None,
        })

    def test_off_mode_no_screenshot(self):
        import tempfile, os
        with tempfile.TemporaryDirectory() as tmp:
            client = self._make_client("off", tmp)
            with patch("urllib.request.urlopen", side_effect=self._fake_urlopen_fail):
                client._perform("click", {"id": "btn"})
            self.assertEqual(os.listdir(tmp), [])

    def test_on_failure_saves_on_fail(self):
        import tempfile, os
        with tempfile.TemporaryDirectory() as tmp:
            client = self._make_client("on_failure", tmp)
            with patch("urllib.request.urlopen", side_effect=self._fake_urlopen_fail):
                client._perform("click", {"id": "btn"})
            files = os.listdir(tmp)
            self.assertEqual(len(files), 1)
            self.assertTrue(files[0].endswith(".png"))
            self.assertIn("click", files[0])

    def test_on_failure_no_screenshot_on_success(self):
        import tempfile, os
        with tempfile.TemporaryDirectory() as tmp:
            client = self._make_client("on_failure", tmp)
            with patch("urllib.request.urlopen", side_effect=self._fake_urlopen_ok):
                client._perform("click", {"id": "btn"})
            self.assertEqual(os.listdir(tmp), [])

    def test_always_saves_on_success(self):
        import tempfile, os
        with tempfile.TemporaryDirectory() as tmp:
            client = self._make_client("always", tmp)
            with patch("urllib.request.urlopen", side_effect=self._fake_urlopen_ok):
                client._perform("click", {"id": "btn"})
            files = os.listdir(tmp)
            self.assertEqual(len(files), 1)

    def test_save_screenshot_explicit_path(self):
        import tempfile, os
        with tempfile.TemporaryDirectory() as tmp:
            client = self._make_client("off", tmp)
            out_path = os.path.join(tmp, "manual.png")
            with patch("urllib.request.urlopen", side_effect=self._fake_urlopen_ok):
                saved = client.save_screenshot(out_path)
            self.assertEqual(saved, out_path)
            self.assertTrue(os.path.exists(out_path))


