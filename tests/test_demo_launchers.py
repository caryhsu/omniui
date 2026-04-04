from __future__ import annotations

from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
CORE_APP_DIR = ROOT / "demo" / "java" / "core-app"
INPUT_APP_DIR = ROOT / "demo" / "java" / "input-app"
ADVANCED_APP_DIR = ROOT / "demo" / "java" / "advanced-app"


class DemoLauncherTests(unittest.TestCase):
    def test_build_runtime_helpers_print_next_step_launchers(self) -> None:
        scripts_dir = ROOT / "scripts"
        for name in (
            "build_demo_runtime.bat",
            "build_demo_runtime.ps1",
            "build_demo_runtime.sh",
        ):
            content = (scripts_dir / name).read_text(encoding="utf-8")
            self.assertIn("run-with-agent", content, msg=name)
            self.assertIn("run-plain", content, msg=name)

    def test_with_agent_launchers_inject_javaagent(self) -> None:
        for app_dir in (CORE_APP_DIR, INPUT_APP_DIR, ADVANCED_APP_DIR):
            for name in (
                "run-with-agent.bat",
                "run-with-agent.ps1",
                "run-with-agent.sh",
                "run-dev-with-agent.bat",
                "run-dev-with-agent.ps1",
                "run-dev-with-agent.sh",
            ):
                content = (app_dir / name).read_text(encoding="utf-8")
                self.assertIn("-javaagent", content, msg=f"{app_dir.name}/{name}")

    def test_plain_launchers_do_not_inject_javaagent(self) -> None:
        for app_dir in (CORE_APP_DIR, INPUT_APP_DIR, ADVANCED_APP_DIR):
            for name in (
                "run-plain.bat",
                "run-plain.ps1",
                "run-plain.sh",
                "run-dev-plain.bat",
                "run-dev-plain.ps1",
                "run-dev-plain.sh",
            ):
                content = (app_dir / name).read_text(encoding="utf-8")
                self.assertNotIn("-javaagent", content, msg=f"{app_dir.name}/{name}")


if __name__ == "__main__":
    unittest.main()
