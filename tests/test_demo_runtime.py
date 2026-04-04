from __future__ import annotations

import unittest
from unittest.mock import patch

from demo.python import _runtime
from demo.python.core import _runtime as core_runtime


class DemoRuntimeTests(unittest.TestCase):
    @patch("demo.python._runtime.OmniUI.connect")
    def test_connect_or_exit_returns_client_when_agent_is_available(self, mock_connect) -> None:
        client = object()
        mock_connect.return_value = client

        self.assertIs(_runtime.connect_or_exit(), client)
        mock_connect.assert_called_once_with(app_name="LoginDemo")

    @patch("demo.python._runtime.OmniUI.connect")
    def test_connect_or_exit_raises_clear_hint_when_agent_is_unavailable(self, mock_connect) -> None:
        mock_connect.side_effect = RuntimeError("Connection refused")

        with self.assertRaises(SystemExit) as context:
            _runtime.connect_or_exit()

        message = str(context.exception)
        self.assertIn("with-agent mode", message)
        self.assertIn("run-dev-with-agent.bat", message)
        self.assertIn("Connection refused", message)

    @patch("demo.python.core._runtime.OmniUI.connect")
    def test_core_connect_or_exit_returns_client(self, mock_connect) -> None:
        client = object()
        mock_connect.return_value = client

        self.assertIs(core_runtime.connect_or_exit(), client)
        mock_connect.assert_called_once_with(base_url="http://127.0.0.1:48100")

    @patch("demo.python.core._runtime.OmniUI.connect")
    def test_core_connect_or_exit_raises_clear_hint(self, mock_connect) -> None:
        mock_connect.side_effect = RuntimeError("Connection refused")

        with self.assertRaises(SystemExit) as context:
            core_runtime.connect_or_exit()

        message = str(context.exception)
        self.assertIn("run-dev-with-agent.bat", message)
        self.assertIn("Connection refused", message)


if __name__ == "__main__":
    unittest.main()
