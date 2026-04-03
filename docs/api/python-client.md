# OmniUI Python Client API

This document describes the currently implemented Python API surface for OmniUI Phase 1.

## Overview

Main entry point:

```python
from omniui import OmniUI
```

Session factory:

```python
client = OmniUI.connect(...)
```

Concrete session type:
- `OmniUIClient`

Core model types:
- `Selector`
- `ActionResult`
- `ActionTrace`
- `ResolvedElement`
- `ActionLogEntry`

## Connection

### `OmniUI.connect(base_url="http://127.0.0.1:48100", app_name="LoginDemo", pid=None, ocr_engine=None, vision_engine=None)`

Create a client session against the local OmniUI agent.

Parameters:
- `base_url: str`
  Local OmniUI agent base URL.
- `app_name: str`
  Logical target application name sent to the Java agent session endpoint.
- `pid: int | None`
  Optional process identifier for future attach scenarios.
- `ocr_engine: SimpleOcrEngine | None`
  Optional OCR provider implementation.
- `vision_engine: SimpleVisionEngine | None`
  Optional vision provider implementation.

Returns:
- `OmniUIClient`

Behavior:
- Calls `GET /health`
- Calls `POST /sessions`
- Raises `RuntimeError` if the agent does not report healthy status

## Session API

### `client.get_nodes() -> list[dict[str, Any]]`

Fetch a JavaFX node snapshot from the agent.

Current node fields include:
- `handle`
- `fxId`
- `nodeType`
- `text`
- `hierarchyPath`
- `visible`
- `enabled`

### `client.find(id=None, text=None, type=None) -> dict[str, Any]`

Normalize selector input without executing any action.

Current selector fields:
- `id`
- `text`
- `type`

Normalization rules:
- empty strings are converted to `None`
- values are stripped before use

### `client.click(**selector) -> ActionResult`

Execute a click action.

Resolution order:
1. JavaFX direct resolution through the agent
2. refresh + retry if the initial failure reason is `selector_not_found`
3. OCR fallback for text selectors
4. vision fallback if OCR does not resolve and a `template` is provided

Notes:
- In the current repo, OCR and vision fallback resolve the target and trace it, but do not yet dispatch a real OS-level click.
- For JavaFX-resolved nodes, the Java agent uses direct node-level interaction.

### `client.type(text, **selector) -> ActionResult`

Execute a text input action through the JavaFX path.

Parameters:
- `text: str`
- selector fields such as `id`, `text`, `type`

### `client.get_text(**selector) -> ActionResult`

Resolve an element and fetch its text through the JavaFX path.

### `client.verify_text(expected, **selector) -> ActionResult`

Resolve an element, fetch its text, and compare it to `expected`.

Return semantics:
- `result.ok` is `True` only when the actual text equals `expected`
- `result.value` currently has:

```python
{
    "actual": ...,
    "expected": ...,
    "matches": True | False,
}
```

### `client.screenshot() -> bytes`

Fetch the screenshot payload from the agent.

Current implementation note:
- In the demo/reference path, screenshot bytes may be OCR-friendly text fixture content rather than a real PNG bitmap.

### `client.ocr(image: bytes) -> list[dict[str, Any]]`

Run the configured OCR engine against image bytes.

Current return fields:
- `text`
- `confidence`
- `bounds`

### `client.vision_match(template: bytes) -> dict[str, Any]`

Run the configured vision engine against the latest screenshot.

Current return fields:
- `matched`
- `confidence`
- `bounds`

### `client.action_history() -> list[ActionLogEntry]`

Return the recorded action log for the current client session.

The list contains completed action results only.

## Selectors

The currently supported selector surface is:

```python
{
    "id": "...",
    "text": "...",
    "type": "...",
}
```

Typical usage:

```python
client.click(id="loginButton")
client.click(text="Login", type="Button")
```

Additional current behavior:
- `click(..., template=b"...")` is accepted by the internal fallback pipeline for vision matching

## Result Models

## `Selector`

Fields:
- `id: str | None`
- `text: str | None`
- `type: str | None`

## `ResolvedElement`

Fields:
- `tier: str`
- `target_ref: str`
- `selector_used: Selector`
- `matched_attributes: dict[str, Any]`
- `confidence: float | None`
- `debug_context: dict[str, Any]`

Tier values currently used:
- `javafx`
- `ocr`
- `vision`

## `ActionTrace`

Fields:
- `selector: Selector`
- `attempted_tiers: list[str]`
- `resolved_tier: str | None`
- `confidence: float | None`
- `details: dict[str, Any]`

Typical attempted tier sequences:
- `["javafx"]`
- `["javafx", "refresh"]`
- `["javafx", "refresh", "ocr"]`
- `["javafx", "refresh", "ocr", "vision"]`

## `ActionResult`

Fields:
- `ok: bool`
- `trace: ActionTrace`
- `resolved: ResolvedElement | None`
- `value: Any`

## `ActionLogEntry`

Fields:
- `action: str`
- `timestamp: datetime`
- `result: ActionResult`

## OCR and Vision Provider Interfaces

Current default OCR provider:
- `SimpleOcrEngine`

Method:
- `read(image: bytes) -> list[OcrMatch]`

Current default vision provider:
- `SimpleVisionEngine`

Method:
- `match(image: bytes, template: bytes) -> VisionMatch`

These are deterministic placeholder implementations intended to keep Phase 1 executable before production OCR or vision libraries are integrated.

## Examples

### Direct JavaFX login flow

```python
from omniui import OmniUI

client = OmniUI.connect(app_name="LoginDemo")

client.click(id="username")
client.type("admin", id="username")

client.click(id="password")
client.type("1234", id="password")

client.click(id="loginButton")
client.verify_text(id="status", expected="Success")
```

### OCR fallback click

```python
client.click(text="Login")
```

### Inspect action history

```python
for entry in client.action_history():
    print(entry.action, entry.result.trace.resolved_tier)
```

## Current Limitations

- No formal generated API reference yet; this document is manually maintained.
- Fallback click currently resolves and records bounds, but does not issue a real OS click.
- `type()` currently depends on JavaFX direct interaction and does not have OCR/vision fallback.
- `find()` normalizes selectors only; it does not resolve them against the application.
