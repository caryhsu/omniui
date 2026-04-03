from __future__ import annotations

import unittest
from unittest.mock import patch

import scripts.benchmark_phase1 as benchmark_phase1


class _FakeClient:
    def get_nodes(self):
        return [{"fxId": "loginButton"}]

    def screenshot(self):
        return b"Login|0.99|10|20|100|24"

    def ocr(self, image: bytes):
        return [{"text": "Login", "confidence": 0.99, "bounds": {"x": 10, "y": 20, "width": 100, "height": 24}}]


class BenchmarkTests(unittest.TestCase):
    @patch("scripts.benchmark_phase1.OmniUI.connect")
    def test_benchmark_reports_phase1_targets(self, mock_connect) -> None:
        mock_connect.return_value = _FakeClient()

        result = benchmark_phase1.run_benchmark()

        self.assertIn("javafx_node_query_ms", result)
        self.assertIn("ocr_fallback_ms", result)
        self.assertTrue(result["javafx_node_query_ms"]["pass"])
        self.assertTrue(result["ocr_fallback_ms"]["pass"])


if __name__ == "__main__":
    unittest.main()
