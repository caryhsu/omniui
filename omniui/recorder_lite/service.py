"""Recorder-lite script generation."""

from __future__ import annotations

from omniui.core.models import ActionLogEntry, Selector


class RecorderLite:
    """Generate simple automation script lines from stable recorded actions."""

    def capture_click(self, entry: ActionLogEntry) -> str | None:
        if entry.action != "click" or not entry.result.ok:
            return None

        selector = entry.result.trace.selector
        if selector.id:
            return f'click(id="{selector.id}")'
        if selector.type and selector.text:
            return f'click(text="{selector.text}", type="{selector.type}")'
        if entry.result.trace.resolved_tier == "ocr" and selector.text:
            return f'click(text="{selector.text}")'
        return None

    def generate_script(self, history: list[ActionLogEntry]) -> list[str]:
        lines: list[str] = []
        for entry in history:
            line = self.capture_click(entry)
            if line is not None:
                lines.append(line)
        return lines

    def generate_python(self, history: list[ActionLogEntry], client_var: str = "client") -> str:
        body = self.generate_script(history)
        if not body:
            return ""
        return "\n".join(f"{client_var}.{line}" for line in body)
