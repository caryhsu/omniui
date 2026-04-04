"""High-level Python client bound to the local OmniUI agent."""

from __future__ import annotations

import re
import json
import time
import base64
import subprocess
from dataclasses import asdict
from typing import Any, Callable
from urllib.error import HTTPError, URLError
from urllib import request

from omniui.ocr_module import OcrMatch, SimpleOcrEngine
from omniui.selector_engine.resolver import normalize_selector
from omniui.vision_module import SimpleVisionEngine

from .models import ActionLogEntry, ActionResult, ActionTrace, ResolvedElement, Selector


class OmniUIClient:
    """Thin client wrapper around the Phase 1 local agent protocol."""

    def __init__(
        self,
        base_url: str,
        session_id: str,
        ocr_engine: SimpleOcrEngine | None = None,
        vision_engine: SimpleVisionEngine | None = None,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.session_id = session_id
        self.ocr_engine = ocr_engine or SimpleOcrEngine()
        self.vision_engine = vision_engine or SimpleVisionEngine()
        self._action_log: list[ActionLogEntry] = []

    @classmethod
    def connect(
        cls,
        base_url: str,
        app_name: str,
        pid: int | None = None,
        ocr_engine: SimpleOcrEngine | None = None,
        vision_engine: SimpleVisionEngine | None = None,
    ) -> "OmniUIClient":
        root = base_url.rstrip("/")
        health = cls._request_json("GET", f"{root}/health")
        if health["status"] != "ok":
            raise RuntimeError("OmniUI agent is not healthy")

        session_payload: dict[str, Any] = {"target": {"appName": app_name}}
        if pid is not None:
            session_payload["target"]["pid"] = pid
        session = cls._request_json("POST", f"{root}/sessions", session_payload)
        return cls(
            base_url=root,
            session_id=session["sessionId"],
            ocr_engine=ocr_engine,
            vision_engine=vision_engine,
        )

    def get_nodes(self) -> list[dict[str, Any]]:
        response = self._request_json("POST", f"{self.base_url}/sessions/{self.session_id}/discover", {"includeHidden": False})
        return response["nodes"]

    def action_history(self) -> list[ActionLogEntry]:
        return list(self._action_log)

    def format_trace(self) -> str:
        """Return the action history as a human-readable timeline string.

        Example output::

            ─── OmniUI Action Trace ──────────────────────────────
              +0.00s  click        id=loginBtn          ✓  javafx
              +0.13s  type         id=username           ✓  javafx
              +0.31s  verify_text  id=status             ✗  javafx
            ──────────────────────────────────────────────────────
        """
        if not self._action_log:
            return "─── OmniUI Action Trace (empty) ───"

        width = 54
        bar = "─" * width
        lines = [f"─── OmniUI Action Trace {'─' * (width - 23)}"]

        t0 = self._action_log[0].timestamp
        for entry in self._action_log:
            elapsed = (entry.timestamp - t0).total_seconds()
            trace = entry.result.trace

            # Build compact selector string
            sel_parts = []
            if trace.selector.id:
                sel_parts.append(f"id={trace.selector.id}")
            if trace.selector.text:
                sel_parts.append(f"text={trace.selector.text!r}")
            if trace.selector.type:
                sel_parts.append(f"type={trace.selector.type}")
            sel_str = ", ".join(sel_parts) if sel_parts else "–"

            tier = trace.resolved_tier or "–"
            status = "✓" if entry.result.ok else "✗"

            lines.append(
                f"  +{elapsed:5.2f}s  {entry.action:<14} {sel_str:<24} {status}  {tier}"
            )

        lines.append(bar)
        return "\n".join(lines)

    def print_trace(self) -> None:
        """Print the action trace timeline to stdout."""
        print(self.format_trace())

    def disconnect(self) -> None:
        """Release this client session.

        The agent has no DELETE /sessions endpoint yet, so this is a
        client-side marker only.  Call it in test teardown so code is
        forward-compatible when server-side cleanup is added.
        """
        self.session_id = ""

    def find(self, **selector: Any) -> dict[str, Any]:
        normalized = normalize_selector(
            id=selector.get("id"),
            text=selector.get("text"),
            type=selector.get("type"),
        )
        return {key: value for key, value in asdict(normalized).items() if value is not None}

    def click(self, **selector: Any) -> ActionResult:
        return self._perform("click", selector)

    def double_click(self, **selector: Any) -> ActionResult:
        """Fire a double-click (MouseEvent clickCount=2) on the target node.

        Use for interactions such as expanding TreeView items, opening detail
        views from ListView/TableView rows, or any custom double-click handler.
        """
        return self._perform("double_click", selector)

    def press_key(self, key: str, **selector: Any) -> ActionResult:
        """Fire KEY_PRESSED + KEY_RELEASED for the given key string.

        Format: Playwright-style "[Modifier+]*Key" — case-insensitive.
        Modifiers: Control (or Ctrl), Shift, Alt, Meta (or Win).
        Examples: "Escape", "Enter", "Tab", "Control+C", "Shift+Tab",
                  "ctrl+z", "Control+Shift+Z".

        If selector is provided, fire on that node.
        If omitted, fire on the scene's current focus owner.
        """
        return self._perform("press_key", selector, {"key": key})

    def select(self, value: str, **selector: Any) -> ActionResult:
        payload: dict[str, Any] = {"value": value}
        if "column" in selector:
            payload["column"] = selector.pop("column")
        return self._perform("select", selector, payload)

    def type(self, text: str, **selector: Any) -> ActionResult:
        return self._perform("type", selector, {"input": text})

    def get_text(self, **selector: Any) -> ActionResult:
        return self._perform("get_text", selector)

    def get_tooltip(self, **selector: Any) -> ActionResult:
        return self._perform("get_tooltip", selector)

    def get_style(self, **selector: Any) -> ActionResult:
        return self._perform("get_style", selector)

    def get_style_class(self, **selector: Any) -> ActionResult:
        return self._perform("get_style_class", selector)

    # ---- Scroll ------------------------------------------------------------

    def scroll_to(self, **selector: Any) -> ActionResult:
        """Scroll the nearest enclosing ScrollPane so the target node is visible."""
        return self._perform("scroll_to", selector)

    def scroll_by(self, delta_x: float, delta_y: float, **selector: Any) -> ActionResult:
        """Scroll a ScrollPane by a relative offset in the 0.0–1.0 range.

        delta_x / delta_y are added to the current hvalue / vvalue and clamped
        to [0.0, 1.0].  Positive delta_y scrolls down; negative scrolls up.

        If no selector is given the first ScrollPane found in the scene is used.
        """
        return self._perform("scroll_by", selector, {"delta_x": delta_x, "delta_y": delta_y})

    # ---- Multi-select ------------------------------------------------------

    def select_multiple(self, values: list[str], **selector: Any) -> ActionResult:
        """Select multiple items by value in a MULTIPLE-mode ListView or TableView.

        Clears the existing selection then selects each item whose toString()
        matches a string in *values*.  Items not found are skipped silently.
        """
        return self._perform("select_multiple", selector, {"values": values})

    def get_selected_items(self, **selector: Any) -> ActionResult:
        """Return all currently selected items as a list of strings."""
        return self._perform("get_selected_items", selector)

    def verify_text(self, expected: str, *, match: str = "exact", **selector: Any) -> ActionResult:
        result = self.get_text(**selector)
        actual = result.value or ""
        if match == "exact":
            matched = actual == expected
        elif match == "contains":
            matched = expected in actual
        elif match == "starts_with":
            matched = actual.startswith(expected)
        elif match == "regex":
            matched = re.search(expected, actual) is not None
        else:
            raise ValueError(f"Unknown match mode: {match!r}. Use 'exact', 'contains', 'starts_with', or 'regex'.")
        result.value = {"actual": actual, "expected": expected, "match": match, "matches": matched}
        result.ok = bool(matched)
        return result

    def screenshot(self) -> bytes:
        payload = self._request_json("POST", f"{self.base_url}/sessions/{self.session_id}/screenshot", {"format": "png"})
        return base64.b64decode(payload["data"])

    def ocr(self, image: bytes) -> list[dict[str, Any]]:
        return [
            {
                "text": match.text,
                "confidence": match.confidence,
                "bounds": match.bounds,
            }
            for match in self.ocr_engine.read(image)
        ]

    def vision_match(self, template: bytes) -> dict[str, Any]:
        image = self.screenshot()
        match = self.vision_engine.match(image, template)
        return {
            "matched": match.matched,
            "confidence": match.confidence,
            "bounds": match.bounds,
        }

    # ---- ContextMenu -------------------------------------------------------

    def right_click(self, **selector: Any) -> ActionResult:
        """Show the ContextMenu registered on a node."""
        return self._perform("right_click", selector)

    def click_menu_item(self, text: str | None = None, *, id: str | None = None, path: str | None = None) -> ActionResult:
        """Fire a menu item in the currently visible overlay by text, id, or slash-separated path."""
        payload: dict[str, Any] = {}
        if text is not None:
            payload["text"] = text
        if id is not None:
            payload["id"] = id
        if path is not None:
            payload["path"] = path
        return self._direct_action("click_menu_item", payload)

    def dismiss_menu(self) -> ActionResult:
        """Hide all visible popup/context menus."""
        return self._direct_action("dismiss_menu", {})

    # ---- MenuBar -----------------------------------------------------------

    def open_menu(self, menu: str, **selector: Any) -> ActionResult:
        """Open a top-level menu in a MenuBar (shows the drop-down)."""
        return self._perform("open_menu", selector, {"menu": menu})

    def navigate_menu(self, path: str, **selector: Any) -> ActionResult:
        """Navigate a MenuBar path (e.g. 'File/Save As') and fire the final item."""
        return self._perform("navigate_menu", selector, {"path": path})

    # ---- DatePicker --------------------------------------------------------

    def open_datepicker(self, **selector: Any) -> ActionResult:
        """Open a DatePicker popup calendar."""
        return self._perform("open_datepicker", selector)

    def navigate_month(self, direction: str = "next") -> ActionResult:
        """Click the forward (next) or backward (prev) button in the DatePicker popup."""
        return self._direct_action("navigate_month", {"direction": direction})

    def pick_date(self, date: str) -> ActionResult:
        """Select a date (ISO-8601 'YYYY-MM-DD') in the open DatePicker popup."""
        return self._direct_action("pick_date", {"date": date})

    def set_date(self, date: str, **selector: Any) -> ActionResult:
        """Set a DatePicker value directly (ISO-8601 'YYYY-MM-DD') without using the popup."""
        return self._perform("set_date", selector, {"date": date})

    # ---- ColorPicker -------------------------------------------------------

    def open_colorpicker(self, **selector: Any) -> ActionResult:
        """Open a ColorPicker popup palette."""
        return self._perform("open_colorpicker", selector)

    def set_color(self, color: str, **selector: Any) -> ActionResult:
        """Set a ColorPicker value directly using a CSS hex color string (e.g. '#ff0000')."""
        return self._perform("set_color", selector, {"color": color})

    def get_color(self, **selector: Any) -> ActionResult:
        """Return the current color of a ColorPicker as a lowercase '#rrggbb' hex string."""
        return self._perform("get_color", selector)

    def dismiss_colorpicker(self) -> ActionResult:
        """Close an open ColorPicker popup palette."""
        return self._direct_action("dismiss_colorpicker", {})

    # ---- SplitPane ---------------------------------------------------------

    def get_divider_positions(self, **selector: Any) -> ActionResult:
        """Return the divider positions of a SplitPane as a list string (e.g. '[0.5]')."""
        return self._perform("get_divider_positions", selector)

    def set_divider_position(self, index: int, position: float, **selector: Any) -> ActionResult:
        """Set the divider at the given index to the specified position (0.0–1.0) in a SplitPane."""
        return self._perform("set_divider_position", selector, {"index": index, "position": position})

    # ---- Dialog / Alert ----------------------------------------------------

    def get_dialog(self) -> ActionResult:
        """Return a descriptor of the currently visible Dialog or Alert."""
        return self._direct_action("get_dialog", {})

    def dismiss_dialog(self, button: str | None = None) -> ActionResult:
        """Click a button in the visible Dialog/Alert to close it."""
        payload: dict[str, Any] = {}
        if button is not None:
            payload["button"] = button
        return self._direct_action("dismiss_dialog", payload)

    def close_app(self) -> ActionResult:
        """Trigger graceful shutdown of the JavaFX application via Platform.exit().

        This should be the last call in a session — subsequent calls will
        raise connection errors as the JVM exits.
        """
        return self._direct_action("close_app", {})

    # ---- RadioButton / ToggleButton ----------------------------------------

    def get_selected(self, **selector: Any) -> ActionResult:
        """Return the selected (boolean) state of a RadioButton or ToggleButton."""
        return self._perform("get_selected", selector)

    def set_selected(self, value: bool, **selector: Any) -> ActionResult:
        """Set the selected state of a RadioButton or ToggleButton."""
        return self._perform("set_selected", selector, {"value": value})

    # ---- Slider ------------------------------------------------------------

    def set_slider(self, value: float, **selector: Any) -> ActionResult:
        """Set the value of a Slider node."""
        return self._perform("set_slider", selector, {"value": value})

    # ---- Spinner -----------------------------------------------------------

    def set_spinner(self, value: str, **selector: Any) -> ActionResult:
        """Set the value of a Spinner node (passed as string, converted by Spinner's converter)."""
        return self._perform("set_spinner", selector, {"value": str(value)})

    def step_spinner(self, steps: int, **selector: Any) -> ActionResult:
        """Increment (positive steps) or decrement (negative steps) a Spinner."""
        return self._perform("step_spinner", selector, {"steps": steps})

    # ---- ProgressBar / ProgressIndicator -----------------------------------

    def get_progress(self, **selector: Any) -> ActionResult:
        """Return the current progress (0.0–1.0, or -1.0 if indeterminate) of a ProgressBar."""
        return self._perform("get_progress", selector)

    def get_value(self, **selector: Any) -> ActionResult:
        """Return the current value of a Slider, Spinner, or ChoiceBox node."""
        return self._perform("get_value", selector)

    # ---- TabPane -----------------------------------------------------------

    def get_tabs(self, **selector: Any) -> ActionResult:
        """Return a list of tab descriptors ({text, disabled}) for a TabPane."""
        return self._perform("get_tabs", selector)

    def select_tab(self, tab: str, **selector: Any) -> ActionResult:
        """Select a tab by its title text in a TabPane."""
        return self._perform("select_tab", selector, {"tab": tab})

    def is_visited(self, **selector: Any) -> bool:
        """Return True if a Hyperlink node has been visited (clicked)."""
        result = self._perform("get_visited", selector, None)
        if not result.ok:
            return False
        v = result.value
        return v is True or str(v).lower() == "true"

    def is_visible(self, **selector: Any) -> bool:
        """Return True if the matched node is currently visible.

        Returns False if no node matches the selector.
        Consistent with Playwright conventions — does not raise on missing node.
        """
        try:
            nodes = self.get_nodes()
            fx_id = selector.get("id")
            text = selector.get("text")
            for node in nodes:
                if fx_id and node.get("fxId") == fx_id:
                    return bool(node.get("visible", False))
                if text and node.get("text") == text:
                    return bool(node.get("visible", False))
        except Exception:
            pass
        return False

    def is_enabled(self, **selector: Any) -> bool:
        """Return True if the matched node is currently enabled (not disabled).

        Returns False if no node matches the selector.
        Consistent with Playwright conventions — does not raise on missing node.
        """
        try:
            nodes = self.get_nodes()
            fx_id = selector.get("id")
            text = selector.get("text")
            for node in nodes:
                if fx_id and node.get("fxId") == fx_id:
                    return bool(node.get("enabled", False))
                if text and node.get("text") == text:
                    return bool(node.get("enabled", False))
        except Exception:
            pass
        return False

    def set_visible(self, visible: bool = True, **selector: Any) -> ActionResult:
        """Set the visibility of a node (setVisible).

        ``visible=True`` (default) makes the node visible; ``False`` hides it.
        The node still occupies layout space when hidden — use this to test
        ``is_visible`` round-trips.
        """
        return self._perform("set_visible", selector, {"visible": visible})

    def set_disabled(self, disabled: bool = True, **selector: Any) -> ActionResult:
        """Enable or disable a node (setDisable).

        ``disabled=True`` disables the node; ``False`` re-enables it.
        """
        return self._perform("set_disabled", selector, {"disabled": disabled})

    # ---- Wait conditions ---------------------------------------------------

    def wait_for_text(self, id: str, expected: str, timeout: float = 5.0, interval: float = 0.2) -> None:
        """Block until the text of node ``id`` equals ``expected``.

        Polls every ``interval`` seconds. Raises ``TimeoutError`` if the
        condition is not met within ``timeout`` seconds.
        """
        deadline = time.monotonic() + timeout
        while True:
            try:
                result = self.get_text(id=id)
                if result.ok and result.value == expected:
                    return
            except Exception:
                pass
            if time.monotonic() >= deadline:
                raise TimeoutError(
                    f"wait_for_text: node '{id}' text did not become {expected!r} within {timeout}s"
                )
            time.sleep(interval)

    def wait_for_visible(self, id: str, timeout: float = 5.0, interval: float = 0.2) -> None:
        """Block until node ``id`` is visible.

        Raises ``TimeoutError`` if the condition is not met within ``timeout`` seconds.
        """
        deadline = time.monotonic() + timeout
        while True:
            if self.is_visible(id=id):
                return
            if time.monotonic() >= deadline:
                raise TimeoutError(
                    f"wait_for_visible: node '{id}' did not become visible within {timeout}s"
                )
            time.sleep(interval)

    def wait_for_enabled(self, id: str, timeout: float = 5.0, interval: float = 0.2) -> None:
        """Block until node ``id`` is enabled.

        Raises ``TimeoutError`` if the condition is not met within ``timeout`` seconds.
        """
        deadline = time.monotonic() + timeout
        while True:
            if self.is_enabled(id=id):
                return
            if time.monotonic() >= deadline:
                raise TimeoutError(
                    f"wait_for_enabled: node '{id}' did not become enabled within {timeout}s"
                )
            time.sleep(interval)

    def wait_for_node(self, id: str, timeout: float = 5.0, interval: float = 0.2) -> None:
        """Block until a node with ``fxId == id`` appears in discovery.

        Raises ``TimeoutError`` if the node does not appear within ``timeout`` seconds.
        """
        deadline = time.monotonic() + timeout
        while True:
            try:
                nodes = self.get_nodes()
                if any(n.get("fxId") == id for n in nodes):
                    return
            except Exception:
                pass
            if time.monotonic() >= deadline:
                raise TimeoutError(
                    f"wait_for_node: node '{id}' did not appear within {timeout}s"
                )
            time.sleep(interval)

    def wait_for_value(self, id: str, expected: str, timeout: float = 5.0, interval: float = 0.2) -> None:
        """Alias for :meth:`wait_for_text`."""
        self.wait_for_text(id=id, expected=expected, timeout=timeout, interval=interval)

    def wait_until(
        self,
        condition: Callable[[], bool],
        timeout: float = 5.0,
        interval: float = 0.2,
        message: str = "condition not met",
    ) -> None:
        """Block until ``condition()`` returns truthy.

        Polls every ``interval`` seconds. Raises ``TimeoutError`` if the
        condition is not met within ``timeout`` seconds.

        Examples::

            client.wait_until(lambda: client.get_text(id="status").value == "Done")
            client.wait_until(lambda: len(client.get_nodes()) > 5, timeout=10)
        """
        deadline = time.monotonic() + timeout
        while True:
            try:
                if condition():
                    return
            except Exception:
                pass
            if time.monotonic() >= deadline:
                raise TimeoutError(f"wait_until: {message} (timeout={timeout}s)")
            time.sleep(interval)

    # ---- Accordion / TitledPane --------------------------------------------

    def expand_pane(self, **selector: Any) -> ActionResult:
        """Expand a TitledPane node (setExpanded=true)."""
        return self._perform("expand_pane", selector)

    def collapse_pane(self, **selector: Any) -> ActionResult:
        """Collapse a TitledPane node (setExpanded=false)."""
        return self._perform("collapse_pane", selector)

    def get_expanded(self, **selector: Any) -> bool:
        """Return True if a TitledPane node is currently expanded."""
        result = self._perform("get_expanded", selector)
        if not result.ok:
            return False
        v = result.value
        return v is True or str(v).lower() == "true"

    # ---- TreeTableView -----------------------------------------------------

    def select_tree_table_row(self, value: str, column: str | None = None, **selector: Any) -> ActionResult:
        """Select a row in a TreeTableView by matching a cell value. Optional column narrows search."""
        payload: dict[str, Any] = {"value": value}
        if column is not None:
            payload["column"] = column
        return self._perform("select_tree_table_row", selector, payload)

    def get_tree_table_cell(self, row: str, column: str, **selector: Any) -> ActionResult:
        """Read the string value of a cell in a TreeTableView by row identifier and column header."""
        return self._perform("get_tree_table_cell", selector, {"row": row, "column": column})

    def expand_tree_table_item(self, value: str, **selector: Any) -> ActionResult:
        """Expand a TreeItem inside a TreeTableView by matching its value string."""
        return self._perform("expand_tree_table_item", selector, {"value": value})

    def collapse_tree_table_item(self, value: str, **selector: Any) -> ActionResult:
        """Collapse a TreeItem inside a TreeTableView by matching its value string."""
        return self._perform("collapse_tree_table_item", selector, {"value": value})

    def get_tree_table_expanded(self, value: str, **selector: Any) -> bool:
        """Return True if the TreeItem matching value inside a TreeTableView is expanded."""
        result = self._perform("get_tree_table_expanded", selector, {"value": value})
        if not result.ok:
            return False
        v = result.value
        return v is True or str(v).lower() == "true"

    def _direct_action(self, action: str, payload: dict[str, Any]) -> ActionResult:
        """Send an action with no selector normalization or OCR/vision fallback."""
        response = self._request_json(
            "POST",
            f"{self.base_url}/sessions/{self.session_id}/actions",
            {"action": action, "payload": payload},
        )
        result = self._action_result({}, response)
        self._record_action(action, result)
        return result

    def _perform(self, action: str, selector_payload: dict[str, Any], payload: dict[str, Any] | None = None) -> ActionResult:
        selector = self.find(**selector_payload)
        result = self._perform_request(action, selector_payload, selector, payload or {})
        if not result.ok and self._should_retry(result):
            try:
                self.get_nodes()
                retried = self._perform_request(action, selector_payload, selector, payload or {})
            except Exception:
                retried = None
            if retried is not None:
                retried.trace.attempted_tiers = self._merge_attempted_tiers(
                    result.trace.attempted_tiers,
                    retried.trace.attempted_tiers,
                )
                retried.trace.details = {
                    **retried.trace.details,
                    "retried_after_refresh": True,
                }
                result = retried
        if result.ok or action != "click":
            self._record_action(action, result)
            return result
        fallback = self._fallback_click(selector_payload, result)
        self._record_action(action, fallback)
        return fallback

    def _perform_request(
        self,
        action: str,
        selector_payload: dict[str, Any],
        selector: dict[str, Any],
        payload: dict[str, Any],
    ) -> ActionResult:
        response = self._request_json(
            "POST",
            f"{self.base_url}/sessions/{self.session_id}/actions",
            {"action": action, "selector": selector, "payload": payload},
        )
        return self._action_result(selector_payload, response)

    def _fallback_click(self, selector_payload: dict[str, Any], base_result: ActionResult) -> ActionResult:
        selector = normalize_selector(
            id=selector_payload.get("id"),
            text=selector_payload.get("text"),
            type=selector_payload.get("type"),
        )
        screenshot = self.screenshot()
        attempted_tiers = list(base_result.trace.attempted_tiers or ["javafx"])

        ocr_match = self._resolve_via_ocr(selector, screenshot)
        attempted_tiers.append("ocr")
        if ocr_match is not None:
            return self._fallback_result(selector, attempted_tiers, "ocr", ocr_match.bounds, ocr_match.confidence)

        attempted_tiers.append("vision")
        vision_match = self._resolve_via_vision(selector_payload, screenshot)
        if vision_match is not None:
            return self._fallback_result(selector, attempted_tiers, "vision", vision_match["bounds"], vision_match["confidence"])

        base_result.trace.attempted_tiers = attempted_tiers
        base_result.trace.details = {
            **base_result.trace.details,
            "fallback": "unresolved",
        }
        return base_result

    def _resolve_via_ocr(self, selector: Selector, screenshot: bytes) -> OcrMatch | None:
        if not selector.text:
            return None
        for match in self.ocr_engine.read(screenshot):
            if match.text == selector.text:
                return match
        return None

    def _resolve_via_vision(self, selector_payload: dict[str, Any], screenshot: bytes) -> dict[str, Any] | None:
        template = selector_payload.get("template")
        if template is None:
            return None
        if isinstance(template, str):
            template = template.encode("utf-8")
        match = self.vision_engine.match(screenshot, template)
        if not match.matched:
            return None
        return {
            "bounds": match.bounds,
            "confidence": match.confidence,
        }

    def _fallback_result(
        self,
        selector: Selector,
        attempted_tiers: list[str],
        tier: str,
        bounds: dict[str, int] | None,
        confidence: float | None,
    ) -> ActionResult:
        trace = ActionTrace(
            selector=selector,
            attempted_tiers=attempted_tiers,
            resolved_tier=tier,
            confidence=confidence,
            details={"bounds": bounds, "fallback": "resolved"},
        )
        resolved = ResolvedElement(
            tier=tier,
            target_ref=json.dumps(bounds or {}, sort_keys=True),
            selector_used=selector,
            matched_attributes={"bounds": bounds or {}},
            confidence=confidence,
            debug_context=trace.details,
        )
        return ActionResult(ok=True, trace=trace, resolved=resolved, value=None)

    def _action_result(self, selector_payload: dict[str, Any], response: dict[str, Any]) -> ActionResult:
        selector = normalize_selector(
            id=selector_payload.get("id"),
            text=selector_payload.get("text"),
            type=selector_payload.get("type"),
        )
        trace_payload = response.get("trace", {})
        resolved_payload = response.get("resolved")
        trace = ActionTrace(
            selector=selector,
            attempted_tiers=trace_payload.get("attemptedTiers", []),
            resolved_tier=trace_payload.get("resolvedTier"),
            confidence=(resolved_payload or {}).get("confidence"),
            details=trace_payload.get("details", {}),
        )
        resolved = None
        if resolved_payload:
            resolved = ResolvedElement(
                tier=resolved_payload["tier"],
                target_ref=resolved_payload["targetRef"],
                selector_used=selector,
                matched_attributes=resolved_payload.get("matchedAttributes", {}),
                confidence=resolved_payload.get("confidence"),
                debug_context=trace_payload.get("details", {}),
            )
        return ActionResult(ok=response.get("ok", False), trace=trace, resolved=resolved, value=response.get("value"))

    def _record_action(self, action: str, result: ActionResult) -> None:
        self._action_log.append(ActionLogEntry.from_result(action, result))

    def _should_retry(self, result: ActionResult) -> bool:
        return result.trace.details.get("reason") == "selector_not_found"

    def _merge_attempted_tiers(self, initial: list[str], retried: list[str]) -> list[str]:
        merged: list[str] = []
        for tier in [*initial, "refresh", *retried]:
            if tier not in merged:
                merged.append(tier)
        return merged

    @staticmethod
    def _request_json(method: str, url: str, payload: dict[str, Any] | None = None) -> dict[str, Any]:
        headers = {"Content-Type": "application/json"}
        data = None if payload is None else json.dumps(payload).encode("utf-8")
        req = request.Request(url, data=data, headers=headers, method=method)
        try:
            with request.urlopen(req) as response:
                return json.loads(response.read().decode("utf-8"))
        except HTTPError as exc:
            body = exc.read().decode("utf-8", errors="replace")
            try:
                error_payload = json.loads(body)
                message = error_payload.get("error", body)
            except json.JSONDecodeError:
                message = body or exc.reason
            raise RuntimeError(f"OmniUI request failed: {exc.code} {message}") from exc
        except URLError as exc:
            raise RuntimeError(f"OmniUI request failed: {exc.reason}") from exc


class OmniUIProcess(OmniUIClient):
    """An ``OmniUIClient`` that owns the underlying JavaFX app process.

    Obtained via :meth:`OmniUI.launch` — do not instantiate directly.
    Supports use as a context manager::

        with OmniUI.launch(jar="app.jar", agent_jar="agent.jar") as ui:
            ui.click(id="loginBtn")
    """

    def __init__(self, process: subprocess.Popen, **kwargs: Any) -> None:
        super().__init__(**kwargs)
        self._process = process

    def disconnect(self) -> None:
        """Disconnect the client session and terminate the app process."""
        super().disconnect()
        if self._process.poll() is None:
            self._process.terminate()
            try:
                self._process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                self._process.kill()

    def __enter__(self) -> "OmniUIProcess":
        return self

    def __exit__(self, *_: Any) -> None:
        self.disconnect()

