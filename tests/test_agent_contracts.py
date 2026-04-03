"""Contract fixtures for the Phase 1 local Java agent protocol."""

from __future__ import annotations

import json
from pathlib import Path
import unittest


CONTRACTS_DIR = Path(__file__).resolve().parents[1] / "contracts" / "agent"


def _load_json(name: str) -> dict:
    return json.loads((CONTRACTS_DIR / name).read_text(encoding="utf-8"))


class AgentContractTests(unittest.TestCase):
    def test_health_response_contract(self) -> None:
        payload = _load_json("health.response.json")

        self.assertEqual(payload["status"], "ok")
        self.assertEqual(payload["transport"], "http-json")
        self.assertIsInstance(payload["version"], str)

    def test_session_contract_pair(self) -> None:
        request_payload = _load_json("session.create.request.json")
        response_payload = _load_json("session.create.response.json")

        self.assertIn("target", request_payload)
        self.assertIsInstance(request_payload["target"]["pid"], int)
        self.assertEqual(response_payload["platform"], "javafx")
        self.assertIn("discover", response_payload["capabilities"])

    def test_discover_response_contains_required_node_fields(self) -> None:
        payload = _load_json("discover.response.json")

        self.assertIn("nodes", payload)
        self.assertTrue(payload["nodes"])
        node = payload["nodes"][0]
        self.assertTrue(
            {"handle", "nodeType", "hierarchyPath", "visible", "enabled"} <= node.keys()
        )

    def test_action_contract_captures_resolution_trace(self) -> None:
        request_payload = _load_json("action.request.json")
        response_payload = _load_json("action.response.json")

        self.assertEqual(request_payload["action"], "click")
        self.assertIn("selector", request_payload)
        self.assertTrue(response_payload["ok"])
        self.assertEqual(response_payload["resolved"]["tier"], "javafx")
        self.assertEqual(response_payload["trace"]["attemptedTiers"], ["javafx"])

    def test_screenshot_contract_uses_base64_payload(self) -> None:
        request_payload = _load_json("screenshot.request.json")
        response_payload = _load_json("screenshot.response.json")

        self.assertEqual(request_payload["format"], "png")
        self.assertEqual(response_payload["encoding"], "base64")
        self.assertIsInstance(response_payload["data"], str)


if __name__ == "__main__":
    unittest.main()
