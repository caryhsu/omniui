from __future__ import annotations

from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
DEMO_APP_DIR = ROOT / "demo" / "javafx-login-app"


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
        for name in (
            "run-with-agent.bat",
            "run-with-agent.ps1",
            "run-with-agent.sh",
            "run-dev-with-agent.bat",
            "run-dev-with-agent.ps1",
            "run-dev-with-agent.sh",
        ):
            content = (DEMO_APP_DIR / name).read_text(encoding="utf-8")
            self.assertIn("-javaagent", content, msg=name)

    def test_plain_launchers_do_not_inject_javaagent(self) -> None:
        for name in (
            "run-plain.bat",
            "run-plain.ps1",
            "run-plain.sh",
            "run-dev-plain.bat",
            "run-dev-plain.ps1",
            "run-dev-plain.sh",
        ):
            content = (DEMO_APP_DIR / name).read_text(encoding="utf-8")
            self.assertNotIn("-javaagent", content, msg=name)

    def test_demo_readme_describes_with_agent_and_plain_modes(self) -> None:
        content = (DEMO_APP_DIR / "README.md").read_text(encoding="utf-8")
        self.assertIn("Agent-enabled development mode", content)
        self.assertIn("Plain development mode", content)
        self.assertIn("Mode A: With OmniUI agent", content)
        self.assertIn("Mode B: Plain app", content)


if __name__ == "__main__":
    unittest.main()
