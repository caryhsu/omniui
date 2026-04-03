"""High-level Python client bound to the local OmniUI agent."""

from __future__ import annotations

import base64
import json
from dataclasses import asdict
from typing import Any
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

    def find(self, **selector: Any) -> dict[str, Any]:
        normalized = normalize_selector(
            id=selector.get("id"),
            text=selector.get("text"),
            type=selector.get("type"),
        )
        return {key: value for key, value in asdict(normalized).items() if value is not None}

    def click(self, **selector: Any) -> ActionResult:
        return self._perform("click", selector)

    def select(self, value: str, **selector: Any) -> ActionResult:
        payload: dict[str, Any] = {"value": value}
        if "column" in selector:
            payload["column"] = selector.pop("column")
        return self._perform("select", selector, payload)

    def type(self, text: str, **selector: Any) -> ActionResult:
        return self._perform("type", selector, {"input": text})

    def get_text(self, **selector: Any) -> ActionResult:
        return self._perform("get_text", selector)

    def verify_text(self, expected: str, **selector: Any) -> ActionResult:
        result = self.get_text(**selector)
        result.value = {"actual": result.value, "expected": expected, "matches": result.value == expected}
        result.ok = bool(result.value["matches"])
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
