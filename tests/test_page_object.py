"""Tests for OmniPage base class (Page Object Model)."""
from __future__ import annotations

import json
import unittest
from unittest.mock import MagicMock, patch

from omniui import OmniUI, OmniPage
from omniui.locator import Locator


def _fake_urlopen_factory(action_value=None):
    """Return a fake urlopen side_effect that answers health/sessions/actions."""
    def fake_urlopen(req, **_kw):
        class FakeResp:
            def read(self_):
                return json.dumps(body).encode()
            def __enter__(self_): return self_
            def __exit__(self_, *_): pass

        url = req.full_url
        if url.endswith("/health"):
            body = {"status": "ok", "version": "0.1.0", "transport": "http-json"}
        elif url.endswith("/sessions"):
            body = {"sessionId": "s1", "appName": "App", "platform": "javafx", "capabilities": []}
        else:
            body = {
                "ok": True, "resolved": None,
                "trace": {"attemptedTiers": ["javafx"], "resolvedTier": "javafx"},
                "value": action_value,
            }
        return FakeResp()
    return fake_urlopen


class OmniPageTests(unittest.TestCase):
    """Tests for OmniPage base class."""

    @patch("urllib.request.urlopen")
    def _make_client(self, mock_urlopen):
        mock_urlopen.side_effect = _fake_urlopen_factory()
        return OmniUI.connect(port=48100)

    def test_omnipage_importable(self):
        self.assertTrue(callable(OmniPage))

    @patch("urllib.request.urlopen")
    def test_subclass_stores_client(self, mock_urlopen):
        mock_urlopen.side_effect = _fake_urlopen_factory()
        client = OmniUI.connect(port=48100)

        class MyPage(OmniPage):
            pass

        page = MyPage(client)
        self.assertIs(page.client, client)

    @patch("urllib.request.urlopen")
    def test_subclass_can_call_client_methods(self, mock_urlopen):
        mock_urlopen.side_effect = _fake_urlopen_factory(action_value=None)
        client = OmniUI.connect(port=48100)

        class LoginPage(OmniPage):
            def click_login(self):
                return self.client.click(id="loginButton")

        page = LoginPage(client)
        result = page.click_login()
        self.assertTrue(result.ok)

    @patch("urllib.request.urlopen")
    def test_locator_shorthand_returns_locator(self, mock_urlopen):
        mock_urlopen.side_effect = _fake_urlopen_factory()
        client = OmniUI.connect(port=48100)
        page = OmniPage(client)
        loc = page.locator(id="myBtn")
        self.assertIsInstance(loc, Locator)

    @patch("urllib.request.urlopen")
    def test_multiple_page_objects_share_client(self, mock_urlopen):
        mock_urlopen.side_effect = _fake_urlopen_factory()
        client = OmniUI.connect(port=48100)

        class PageA(OmniPage):
            pass

        class PageB(OmniPage):
            pass

        a, b = PageA(client), PageB(client)
        self.assertIs(a.client, b.client)


if __name__ == "__main__":
    unittest.main()
