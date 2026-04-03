from __future__ import annotations

import unittest
from unittest.mock import patch

from omniui.core.models import ActionLogEntry, ActionResult, ActionTrace, Selector

import scripts.demo_login_flow as demo_login_flow


class _FakeClient:
    def __init__(self) -> None:
        self._history = [
            ActionLogEntry.from_result(
                "click",
                ActionResult(
                    ok=True,
                    trace=ActionTrace(
                        selector=Selector(id="username"),
                        attempted_tiers=["javafx"],
                        resolved_tier="javafx",
                    ),
                ),
            ),
            ActionLogEntry.from_result(
                "click",
                ActionResult(
                    ok=True,
                    trace=ActionTrace(
                        selector=Selector(text="Login"),
                        attempted_tiers=["javafx", "refresh", "ocr"],
                        resolved_tier="ocr",
                        confidence=0.99,
                    ),
                ),
            ),
        ]

    def click(self, **selector):
        return None

    def type(self, text: str, **selector):
        return None

    def verify_text(self, **selector):
        trace = ActionTrace(
            selector=Selector(id="status"),
            attempted_tiers=["javafx"],
            resolved_tier="javafx",
        )
        return ActionResult(
            ok=True,
            trace=trace,
            value={"actual": "Success", "expected": "Success", "matches": True},
        )

    def action_history(self):
        return self._history


class DemoScriptTests(unittest.TestCase):
    @patch("scripts.demo_login_flow.connect_or_exit")
    def test_demo_login_flow_runs_successfully(self, mock_connect_or_exit) -> None:
        mock_connect_or_exit.return_value = _FakeClient()

        demo_login_flow.main()

        mock_connect_or_exit.assert_called_once_with()


if __name__ == "__main__":
    unittest.main()
