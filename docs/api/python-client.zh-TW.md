# OmniUI Python Client API

本文件說明 OmniUI Phase 1 目前已實作的 Python API surface。

## Overview

主要入口：

```python
from omniui import OmniUI
```

Session factory：

```python
client = OmniUI.connect(...)
```

實際 session type：
- `OmniUIClient`

核心 model type：
- `Selector`
- `ActionResult`
- `ActionTrace`
- `ResolvedElement`
- `ActionLogEntry`

## Connection

### `OmniUI.connect(base_url="http://127.0.0.1:48100", app_name="LoginDemo", pid=None, ocr_engine=None, vision_engine=None)`

建立一個指向本機 OmniUI agent 的 client session。

參數：
- `base_url: str`
  本機 OmniUI agent 的 base URL
- `app_name: str`
  傳給 Java agent session endpoint 的 target application logical name
- `pid: int | None`
  可選的 process identifier，供未來 attach scenario 使用
- `ocr_engine: SimpleOcrEngine | None`
  可選的 OCR provider implementation
- `vision_engine: SimpleVisionEngine | None`
  可選的 vision provider implementation

回傳：
- `OmniUIClient`

行為：
- 呼叫 `GET /health`
- 呼叫 `POST /sessions`
- 若 agent 沒有回報 healthy status，則丟出 `RuntimeError`

## Session API

### `client.get_nodes() -> list[dict[str, Any]]`

從 agent 取得 JavaFX node snapshot。

目前 node 欄位包含：
- `handle`
- `fxId`
- `nodeType`
- `text`
- `hierarchyPath`
- `visible`
- `enabled`

### `client.find(id=None, text=None, type=None) -> dict[str, Any]`

正規化 selector input，但不執行 action。

目前支援的 selector 欄位：
- `id`
- `text`
- `type`

正規化規則：
- 空字串會轉成 `None`
- 使用前會先做字串 trim

### `client.click(**selector) -> ActionResult`

執行 click action。

解析順序：
1. 先透過 agent 做 JavaFX direct resolution
2. 若初次失敗原因是 `selector_not_found`，先 refresh 再 retry
3. 若仍失敗，對 text selector 啟動 OCR fallback
4. 若 OCR 也失敗，且有提供 `template`，則啟動 vision fallback

注意：
- 目前 repo 中，OCR 與 vision fallback 會 resolve target 並記錄 trace，但尚未發出真正的 OS-level click
- 若 node 由 JavaFX resolve，Java agent 會使用 direct node-level interaction

### `client.type(text, **selector) -> ActionResult`

透過 JavaFX path 執行文字輸入。

參數：
- `text: str`
- selector 欄位，例如 `id`、`text`、`type`

### `client.get_text(**selector) -> ActionResult`

resolve element 並透過 JavaFX path 取得其文字。

### `client.verify_text(expected, **selector) -> ActionResult`

resolve element、取得文字，並與 `expected` 比對。

回傳語意：
- 只有當 actual text 等於 `expected` 時，`result.ok` 才會是 `True`
- 目前 `result.value` 內容為：

```python
{
    "actual": ...,
    "expected": ...,
    "matches": True | False,
}
```

### `client.screenshot() -> bytes`

從 agent 取得 screenshot payload。

目前實作注意事項：
- 在 demo / reference path 中，screenshot bytes 可能是 OCR-friendly 的文字 fixture，而不是真正 PNG bitmap

### `client.ocr(image: bytes) -> list[dict[str, Any]]`

使用設定的 OCR engine 對 image bytes 做辨識。

目前回傳欄位：
- `text`
- `confidence`
- `bounds`

### `client.vision_match(template: bytes) -> dict[str, Any]`

使用設定的 vision engine 對最新 screenshot 做比對。

目前回傳欄位：
- `matched`
- `confidence`
- `bounds`

### `client.action_history() -> list[ActionLogEntry]`

回傳目前 client session 的 action log。

列表內容只包含已完成的 action result。

## Selectors

目前支援的 selector surface：

```python
{
    "id": "...",
    "text": "...",
    "type": "...",
}
```

典型用法：

```python
client.click(id="loginButton")
client.click(text="Login", type="Button")
```

額外的目前行為：
- `click(..., template=b"...")` 目前可供內部 fallback pipeline 做 vision matching

## Result Models

## `Selector`

欄位：
- `id: str | None`
- `text: str | None`
- `type: str | None`

## `ResolvedElement`

欄位：
- `tier: str`
- `target_ref: str`
- `selector_used: Selector`
- `matched_attributes: dict[str, Any]`
- `confidence: float | None`
- `debug_context: dict[str, Any]`

目前使用的 tier 值：
- `javafx`
- `ocr`
- `vision`

## `ActionTrace`

欄位：
- `selector: Selector`
- `attempted_tiers: list[str]`
- `resolved_tier: str | None`
- `confidence: float | None`
- `details: dict[str, Any]`

典型 attempted tier 序列：
- `["javafx"]`
- `["javafx", "refresh"]`
- `["javafx", "refresh", "ocr"]`
- `["javafx", "refresh", "ocr", "vision"]`

## `ActionResult`

欄位：
- `ok: bool`
- `trace: ActionTrace`
- `resolved: ResolvedElement | None`
- `value: Any`

## `ActionLogEntry`

欄位：
- `action: str`
- `timestamp: datetime`
- `result: ActionResult`

## OCR 與 Vision Provider Interface

目前預設 OCR provider：
- `SimpleOcrEngine`

Method：
- `read(image: bytes) -> list[OcrMatch]`

目前預設 vision provider：
- `SimpleVisionEngine`

Method：
- `match(image: bytes, template: bytes) -> VisionMatch`

這些都是 deterministic placeholder implementation，目的是先讓 Phase 1 在正式 OCR / vision library 接入前保持可執行。

## 範例

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

### 檢查 action history

```python
for entry in client.action_history():
    print(entry.action, entry.result.trace.resolved_tier)
```

## Current Limitations

- 尚未提供自動生成的正式 API reference，目前是人工維護文件
- fallback click 目前只 resolve 並記錄 bounds，尚未發出真正 OS click
- `type()` 目前依賴 JavaFX direct interaction，沒有 OCR / vision fallback
- `find()` 只做 selector normalization，不會真正對應用程式做 resolution
