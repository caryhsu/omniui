from __future__ import annotations

import inspect
import re
from types import ModuleType
import unittest
from unittest.mock import MagicMock, patch

import demo.python.run_all as run_all


def _iter_called_demo_modules() -> list[ModuleType]:
    source = inspect.getsource(run_all)
    called_names = {
        match.group(1)
        for match in re.finditer(r"^\s*(\w+)\.main\(\)", source, flags=re.MULTILINE)
    }

    modules: list[ModuleType] = []
    for name, value in vars(run_all).items():
        if not isinstance(value, ModuleType):
            continue
        if name not in called_names:
            continue
        if not callable(getattr(value, "main", None)):
            continue
        modules.append(value)
    return modules


class RunAllTests(unittest.TestCase):
    def test_headless_jvm_args_toggle(self) -> None:
        with patch.object(run_all, "_HEADLESS", False):
            self.assertEqual(run_all._headless_jvm_args(), [])

        with patch.object(run_all, "_HEADLESS", True):
            self.assertEqual(
                run_all._headless_jvm_args(),
                [
                    "-Dglass.platform=Headless",
                    "--enable-native-access=javafx.graphics",
                ],
            )

    def test_main_without_auto_launch_invokes_every_demo_main(self) -> None:
        modules = _iter_called_demo_modules()
        section_titles: list[str] = []

        with (
            patch.object(run_all, "_section", side_effect=section_titles.append),
            patch.object(run_all.OmniUI, "find_free_port") as mock_find_free_port,
            patch.object(run_all.OmniUI, "launch") as mock_launch,
        ):
            mocks = {module.__name__: MagicMock(name=f"{module.__name__}.main") for module in modules}
            patches = [
                patch.object(module, "main", mocks[module.__name__])
                for module in modules
            ]
            for patcher in patches:
                patcher.start()
            try:
                run_all.main(auto_launch=False, verbose=True, headless=False)
            finally:
                for patcher in reversed(patches):
                    patcher.stop()

        self.assertGreater(len(section_titles), 0)
        self.assertEqual(section_titles[0], "Core App demos (port 48100+)")
        self.assertIn("User Search Demo", section_titles)
        mock_find_free_port.assert_not_called()
        mock_launch.assert_not_called()

        for module in modules:
            mocks[module.__name__].assert_called_once_with()


if __name__ == "__main__":
    unittest.main()
