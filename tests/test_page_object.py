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

    def test_locator_shorthand_delegates_selector_kwargs_to_client(self):
        client = MagicMock()
        sentinel = object()
        client.locator.return_value = sentinel
        page = OmniPage(client)

        result = page.locator(id="saveBtn", text="Save", type="Button", index=2)

        self.assertIs(result, sentinel)
        client.locator.assert_called_once_with(
            id="saveBtn",
            text="Save",
            type="Button",
            index=2,
        )

    def test_page_methods_can_compose_locator_and_client_calls_without_http(self):
        client = MagicMock()
        username_locator = MagicMock()
        password_locator = MagicMock()
        submit_locator = MagicMock()
        client.locator.side_effect = [
            username_locator,
            password_locator,
            submit_locator,
        ]

        class LoginPage(OmniPage):
            def login(self, username: str, password: str) -> None:
                self.locator(id="username").type(username)
                self.locator(id="password").type(password)
                self.locator(id="submit").click()

        page = LoginPage(client)
        page.login("admin", "secret")

        self.assertEqual(
            client.locator.call_args_list,
            [
                unittest.mock.call(id="username"),
                unittest.mock.call(id="password"),
                unittest.mock.call(id="submit"),
            ],
        )
        username_locator.type.assert_called_once_with("admin")
        password_locator.type.assert_called_once_with("secret")
        submit_locator.click.assert_called_once_with()

    def test_page_subclass_can_expose_locator_backed_component_property(self):
        client = MagicMock()
        status_locator = MagicMock()
        client.locator.return_value = status_locator

        class DashboardPage(OmniPage):
            @property
            def status_badge(self):
                return self.locator(id="statusBadge")

        page = DashboardPage(client)

        self.assertIs(page.status_badge, status_locator)
        client.locator.assert_called_once_with(id="statusBadge")

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
