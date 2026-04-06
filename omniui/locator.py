"""Reusable node locator for OmniUI.

A ``Locator`` stores a node selector and delegates all interaction methods to
an ``OmniUIClient`` instance without requiring the caller to repeat the
selector on every call.

Usage::

    btn = client.locator(id="loginBtn")
    btn.wait_for_visible()
    btn.click()
    btn.verify_text("Login")
"""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

if TYPE_CHECKING:
    from omniui.core.engine import OmniUIClient
    from omniui.core.models import ActionResult


class Locator:
    """Reusable handle for a single node identified by a selector.

    Obtain via :meth:`OmniUIClient.locator`::

        loc = client.locator(id="submitBtn")
        loc.click()
        loc.verify_text("Submit")

    All interaction methods forward to the underlying client with the stored
    selector merged in automatically.
    """

    def __init__(self, client: OmniUIClient, **selector: Any) -> None:
        if not selector:
            raise ValueError("Locator requires at least one selector keyword (id, text, type, ...)")
        self._client = client
        self._sel: dict[str, Any] = selector

    # ---- Core interaction --------------------------------------------------

    def click(self) -> ActionResult:
        return self._client.click(**self._sel)

    def double_click(self) -> ActionResult:
        return self._client.double_click(**self._sel)

    def right_click(self) -> ActionResult:
        return self._client.right_click(**self._sel)

    def press_key(self, key: str) -> ActionResult:
        return self._client.press_key(key, **self._sel)

    def type(self, text: str) -> ActionResult:
        return self._client.type(text, **self._sel)

    # ---- Text / content ----------------------------------------------------

    def get_text(self) -> ActionResult:
        return self._client.get_text(**self._sel)

    def verify_text(self, expected: str, *, match: str = "exact") -> ActionResult:
        return self._client.verify_text(expected, match=match, **self._sel)

    def get_tooltip(self) -> ActionResult:
        return self._client.get_tooltip(**self._sel)

    # ---- Style -------------------------------------------------------------

    def get_style(self) -> ActionResult:
        return self._client.get_style(**self._sel)

    def get_style_class(self) -> ActionResult:
        return self._client.get_style_class(**self._sel)

    # ---- State queries -----------------------------------------------------

    def is_visible(self) -> bool:
        return self._client.is_visible(**self._sel)

    def is_enabled(self) -> bool:
        return self._client.is_enabled(**self._sel)

    def is_visited(self) -> bool:
        return self._client.is_visited(**self._sel)

    # ---- Value / selection -------------------------------------------------

    def get_value(self) -> ActionResult:
        return self._client.get_value(**self._sel)

    def get_progress(self) -> ActionResult:
        return self._client.get_progress(**self._sel)

    def get_image_url(self) -> ActionResult:
        return self._client.get_image_url(**self._sel)

    def is_image_loaded(self) -> bool:
        return self._client.is_image_loaded(**self._sel)

    def open_colorpicker(self) -> ActionResult:
        return self._client.open_colorpicker(**self._sel)

    def set_color(self, color: str) -> ActionResult:
        return self._client.set_color(color, **self._sel)

    def get_color(self) -> ActionResult:
        return self._client.get_color(**self._sel)

    def get_selected(self) -> ActionResult:
        return self._client.get_selected(**self._sel)

    def get_selected_items(self) -> ActionResult:
        return self._client.get_selected_items(**self._sel)

    def select(self, value: str) -> ActionResult:
        return self._client.select(value, **self._sel)

    def select_multiple(self, values: list[str]) -> ActionResult:
        return self._client.select_multiple(values, **self._sel)

    def set_selected(self, value: bool) -> ActionResult:
        return self._client.set_selected(value, **self._sel)

    def set_slider(self, value: float) -> ActionResult:
        return self._client.set_slider(value, **self._sel)

    def set_spinner(self, value: str) -> ActionResult:
        return self._client.set_spinner(value, **self._sel)

    def step_spinner(self, steps: int) -> ActionResult:
        return self._client.step_spinner(steps, **self._sel)

    # ---- Tabs --------------------------------------------------------------

    def get_tabs(self) -> ActionResult:
        return self._client.get_tabs(**self._sel)

    def select_tab(self, tab: str) -> ActionResult:
        return self._client.select_tab(tab, **self._sel)

    # ---- Scroll ------------------------------------------------------------

    def scroll_to(self) -> ActionResult:
        return self._client.scroll_to(**self._sel)

    def scroll_by(self, delta_x: float, delta_y: float) -> ActionResult:
        return self._client.scroll_by(delta_x, delta_y, **self._sel)

    # ---- Wait conditions ---------------------------------------------------

    def _require_id(self, method: str) -> str:
        node_id = self._sel.get("id")
        if not node_id:
            raise ValueError(
                f"Locator.{method}() requires the locator to be created with id=..."
            )
        return node_id

    def wait_for_visible(self, timeout: float = 5.0) -> None:
        self._client.wait_for_visible(self._require_id("wait_for_visible"), timeout=timeout)

    def wait_for_enabled(self, timeout: float = 5.0) -> None:
        self._client.wait_for_enabled(self._require_id("wait_for_enabled"), timeout=timeout)

    def wait_for_node(self, timeout: float = 5.0) -> None:
        self._client.wait_for_node(self._require_id("wait_for_node"), timeout=timeout)

    def wait_for_text(self, expected: str, timeout: float = 5.0) -> None:
        self._client.wait_for_text(self._require_id("wait_for_text"), expected, timeout=timeout)

    def wait_for_value(self, expected: str, timeout: float = 5.0) -> None:
        self._client.wait_for_value(self._require_id("wait_for_value"), expected, timeout=timeout)

    # ---- Accordion / Pane --------------------------------------------------

    def expand_pane(self) -> ActionResult:
        return self._client.expand_pane(**self._sel)

    def collapse_pane(self) -> ActionResult:
        return self._client.collapse_pane(**self._sel)

    def get_expanded(self) -> ActionResult:
        return self._client.get_expanded(**self._sel)

    # ---- Dunder ------------------------------------------------------------

    def __repr__(self) -> str:
        sel = ", ".join(f"{k}={v!r}" for k, v in self._sel.items())
        return f"Locator({sel})"
