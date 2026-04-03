"""Tests for the Python client protocol integration."""

from __future__ import annotations

import json
import io
import unittest
from urllib.error import HTTPError
from urllib.error import URLError
from unittest.mock import patch

from omniui import OmniUI
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
            OmniUI.connect()

        self.assertIn("Target app not available", str(context.exception))

    @patch("urllib.request.urlopen")
    def test_connect_raises_runtime_error_when_agent_is_unavailable(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = URLError("Connection refused")

        with self.assertRaises(RuntimeError) as context:
            OmniUI.connect()

        self.assertIn("Connection refused", str(context.exception))

    @patch("urllib.request.urlopen")
    def test_connect_get_nodes_and_click_flow(self, mock_urlopen) -> None:
        mock_urlopen.side_effect = [
            _FakeResponse({"status": "ok", "version": "0.1.0", "transport": "http-json"}),
            _FakeResponse({"sessionId": "session-1", "appName": "LoginDemo", "platform": "javafx", "capabilities": ["discover", "action"]}),
            _FakeResponse({"nodes": [{"handle": "node-login", "fxId": "loginButton", "nodeType": "Button", "text": "Login", "hierarchyPath": "/Scene/VBox/Button[1]", "visible": True, "enabled": True}]}),
            _FakeResponse({"ok": True, "resolved": {"tier": "javafx", "targetRef": "node-login", "matchedAttributes": {"fxId": "loginButton"}, "confidence": None}, "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"}, "value": None}),
        ]

        client = OmniUI.connect()
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

        client = OmniUI.connect()
        result = client.verify_text(id="status", expected="Success")

        self.assertTrue(result.ok)
        self.assertEqual(result.value["actual"], "Success")
        self.assertTrue(result.value["matches"])

    @patch("urllib.request.urlopen")
    def test_select_sends_selection_payload(self, mock_urlopen) -> None:
        captured_requests: list[dict[str, object]] = []

        def fake_urlopen(req):
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

        client = OmniUI.connect()
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

        client = OmniUI.connect(ocr_engine=SimpleOcrEngine())
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

        client = OmniUI.connect(ocr_engine=SimpleOcrEngine(), vision_engine=SimpleVisionEngine())
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

        client = OmniUI.connect()
        result = client.click(id="loginButton")

        self.assertTrue(result.ok)
        self.assertEqual(result.trace.attempted_tiers, ["javafx", "refresh"])
        self.assertTrue(result.trace.details["retried_after_refresh"])


if __name__ == "__main__":
    unittest.main()
