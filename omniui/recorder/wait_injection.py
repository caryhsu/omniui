"""Wait injection: insert wait_for_* calls between recorded events for stable playback."""
from __future__ import annotations

from omniui.core.models import RecordedEvent

# Node types that should use wait_for_enabled (interactable controls)
_ENABLED_TYPES = {
    "Button",
    "CheckBox",
    "RadioButton",
    "ToggleButton",
    "ComboBox",
    "ChoiceBox",
    "Spinner",
    "DatePicker",
    "ColorPicker",
    "Hyperlink",
    "MenuItem",
    "MenuButton",
    "SplitMenuButton",
    "TextField",
    "TextArea",
    "PasswordField",
}


def _wait_line(event: RecordedEvent, timeout: float) -> str | None:
    """Return a wait_for_* line for *event*, or None if selector is too fragile."""
    if event.fx_id:
        selector = f'id="{event.fx_id}"'
    elif event.text and len(event.text) <= 40:
        selector = f'text="{event.text}"'
    else:
        return None  # fragile selector — skip wait

    if event.event_type == "drag":
        # For drag, wait for the source to be visible before dragging
        return f"client.wait_for_visible({selector}, timeout={timeout})"
    if event.node_type in _ENABLED_TYPES:
        return f"client.wait_for_enabled({selector}, timeout={timeout})"
    return f"client.wait_for_visible({selector}, timeout={timeout})"


def inject_waits(
    events: list[RecordedEvent],
    lines: list[str],
    timeout: float = 5.0,
) -> list[str]:
    """Interleave wait_for_* lines between action lines.

    *lines* must be parallel to *events* (same length, same order).
    Returns a new list with wait lines inserted before each action
    (except the first one, which has no predecessor to wait after).

    Example::

        events = [click(tbNew), click(tbSave)]
        lines  = ['client.click(id="tbNew")', 'client.click(id="tbSave")']

        result = [
            'client.click(id="tbNew")',
            'client.wait_for_enabled(id="tbSave", timeout=5.0)',
            'client.click(id="tbSave")',
        ]
    """
    if not events:
        return list(lines)

    result: list[str] = []
    for i, (event, line) in enumerate(zip(events, lines)):
        if i > 0:
            wait = _wait_line(event, timeout)
            if wait:
                result.append(wait)
        result.append(line)
    return result
