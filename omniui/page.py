"""OmniPage — base class for the Page Object Model pattern.

Example::

    from omniui import OmniUI, OmniPage

    class LoginPage(OmniPage):
        def login(self, username: str, password: str) -> None:
            self.client.input_text(id="username", text=username)
            self.client.input_text(id="password", text=password)
            self.client.click(id="loginButton")

        def get_status(self) -> str:
            return self.client.get_text(id="statusLabel").value

    client = OmniUI.connect(port=48100)
    page = LoginPage(client)
    page.login("admin", "secret")
    assert page.get_status() == "Welcome"
"""
from __future__ import annotations

from typing import TYPE_CHECKING, Any

if TYPE_CHECKING:
    from .core.engine import OmniUIClient
    from .locator import Locator


class OmniPage:
    """Base class for Page Object Model page/component wrappers.

    Subclass this and add methods that group related UI actions for a
    single screen or component.  All OmniUI client methods are available
    via ``self.client``.
    """

    def __init__(self, client: "OmniUIClient") -> None:
        self.client = client

    def locator(self, **selector: Any) -> "Locator":
        """Return a reusable :class:`~omniui.Locator` for the given selector.

        Shorthand for ``self.client.locator(**selector)``.
        """
        return self.client.locator(**selector)
