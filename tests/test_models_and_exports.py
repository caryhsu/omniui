"""Tests for core data models and public package exports."""

from __future__ import annotations

import os
import unittest
from datetime import UTC
from unittest.mock import patch

from omniui import (
    Locator,
    OmniPage,
    OmniUI,
    OmniUIClient,
    OmniUIProcess,
    RecordedEvent,
    RecordedScript,
    SoftAssertContext,
    UIDiff,
    UISnapshot,
    retry,
    soft_assert,
)
from omniui.core.models import ActionLogEntry, ActionResult, ActionTrace, NodeRecord, ResolvedElement, Selector


class CoreModelsTests(unittest.TestCase):
    def test_selector_defaults_and_index_are_preserved(self) -> None:
        selector = Selector(id="loginBtn", text="Login", type="Button", index=1)

        self.assertEqual(selector.id, "loginBtn")
        self.assertEqual(selector.text, "Login")
        self.assertEqual(selector.type, "Button")
        self.assertEqual(selector.index, 1)

    def test_node_record_defaults(self) -> None:
        record = NodeRecord(handle="node-1", node_type="Button", hierarchy_path="/Scene/Button[1]")

        self.assertIsNone(record.fx_id)
        self.assertIsNone(record.text)
        self.assertTrue(record.visible)
        self.assertTrue(record.enabled)
        self.assertIsNone(record.bounds)

    def test_resolved_element_defaults(self) -> None:
        resolved = ResolvedElement(
            tier="javafx",
            target_ref="node-1",
            selector_used=Selector(id="loginBtn"),
        )

        self.assertEqual(resolved.matched_attributes, {})
        self.assertIsNone(resolved.confidence)
        self.assertEqual(resolved.debug_context, {})

    def test_action_trace_defaults(self) -> None:
        trace = ActionTrace(
            selector=Selector(id="loginBtn"),
            attempted_tiers=["javafx"],
            resolved_tier="javafx",
        )

        self.assertIsNone(trace.confidence)
        self.assertEqual(trace.details, {})

    def test_action_log_entry_from_result_sets_timestamp_and_result(self) -> None:
        result = ActionResult(
            ok=True,
            trace=ActionTrace(
                selector=Selector(id="loginBtn"),
                attempted_tiers=["javafx"],
                resolved_tier="javafx",
            ),
        )

        entry = ActionLogEntry.from_result("click", result)

        self.assertEqual(entry.action, "click")
        self.assertIs(entry.result, result)
        self.assertEqual(entry.timestamp.tzinfo, UTC)

    def test_ui_snapshot_save_and_load_round_trip(self) -> None:
        snapshot = UISnapshot(
            nodes=[{"fxId": "status", "text": "Ready"}],
            timestamp=123.45,
        )

        written: dict[str, str] = {}
        path = os.path.join(os.getcwd(), "snapshot.json")

        def fake_write_text(self, data, encoding=None, **_kwargs):
            written[str(self)] = data
            return len(data)

        def fake_read_text(self, encoding=None, **_kwargs):
            return written[str(self)]

        with patch("pathlib.Path.write_text", fake_write_text), patch("pathlib.Path.read_text", fake_read_text):
            snapshot.save(path)
            loaded = UISnapshot.load(path)

        self.assertEqual(loaded, snapshot)

    def test_ui_diff_stores_added_removed_and_changed(self) -> None:
        diff = UIDiff(
            added=[{"fxId": "new"}],
            removed=[{"fxId": "old"}],
            changed=[{"before": {"fxId": "status"}, "after": {"fxId": "status", "text": "Done"}}],
        )

        self.assertEqual(diff.added[0]["fxId"], "new")
        self.assertEqual(diff.removed[0]["fxId"], "old")
        self.assertEqual(diff.changed[0]["after"]["text"], "Done")

    def test_recorded_event_defaults(self) -> None:
        event = RecordedEvent(
            event_type="click",
            fx_id="loginBtn",
            text="Login",
            node_type="Button",
            node_index=0,
            timestamp=1.5,
        )

        self.assertEqual(event.to_fx_id, "")
        self.assertEqual(event.to_text, "")
        self.assertEqual(event.to_node_type, "")
        self.assertEqual(event.to_node_index, 0)
        self.assertEqual(event.color, "")
        self.assertEqual(event.assertion_type, "")
        self.assertEqual(event.expected, "")

    def test_recorded_script_save_writes_script_text(self) -> None:
        script = RecordedScript(events=[], script="print('ok')\n")

        written: dict[str, str] = {}
        path = os.path.join(os.getcwd(), "recorded.py")

        def fake_write_text(self, data, encoding=None, **_kwargs):
            written[str(self)] = data
            return len(data)

        with patch("pathlib.Path.write_text", fake_write_text):
            script.save(path)

        self.assertEqual(written[path], "print('ok')\n")


class PublicExportsTests(unittest.TestCase):
    def test_package_exports_expected_symbols(self) -> None:
        import omniui

        expected = {
            "OmniUI",
            "OmniUIClient",
            "OmniUIProcess",
            "Locator",
            "OmniPage",
            "UISnapshot",
            "UIDiff",
            "RecordedEvent",
            "RecordedScript",
            "retry",
            "soft_assert",
            "SoftAssertContext",
        }

        self.assertEqual(set(omniui.__all__), expected)

    def test_package_exports_are_importable(self) -> None:
        self.assertIsNotNone(OmniUI)
        self.assertIsNotNone(OmniUIClient)
        self.assertIsNotNone(OmniUIProcess)
        self.assertIsNotNone(Locator)
        self.assertIsNotNone(OmniPage)
        self.assertIsNotNone(UISnapshot)
        self.assertIsNotNone(UIDiff)
        self.assertIsNotNone(RecordedEvent)
        self.assertIsNotNone(RecordedScript)
        self.assertIs(retry, __import__("omniui").retry)
        self.assertIs(soft_assert, __import__("omniui").soft_assert)
        self.assertIs(SoftAssertContext, __import__("omniui").SoftAssertContext)
