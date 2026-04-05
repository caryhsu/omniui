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
- `UISnapshot`
- `UIDiff`
- `RecordedEvent`
- `RecordedScript`

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

## UI Recorder

錄製使用者與 JavaFX app 的互動，並產生可重播的 Python 腳本。

### `client.start_recording() -> None`

在 JavaFX Scene 上掛 `EventFilter`，開始擷取 `MOUSE_CLICKED` 和 `KEY_TYPED` 事件。設定 `client._recording = True`。

```python
client.start_recording()
```

### `client.stop_recording() -> RecordedScript`

移除 EventFilter，從 agent flush 緩衝事件，對每個事件執行 selector inference，並產生 Python 測試腳本。設定 `client._recording = False`。

```python
script = client.stop_recording()
print(f"{len(script.events)} 個事件，{len(script.script)} 字元")
script.save("recorded_test.py")
```

回傳 `RecordedScript` 實例。

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

---

## TableView 操作

### `client.get_cell(row, col, **selector) -> ActionResult`

讀取儲存格文字，`row`、`col` 為 0-based 整數。

### `client.click_cell(row, col, **selector) -> ActionResult`

點擊 TableView 的儲存格。

### `client.edit_cell(row, col, value, **selector) -> ActionResult`

雙擊進入編輯模式，輸入 `value` 後按 Enter 確認。

### `client.sort_column(col, direction=None, **selector) -> ActionResult`

排序欄位。`direction`: `"asc"` | `"desc"` | `None`（切換）。

---

## ToolBar 操作

### `client.get_toolbar_items(*, id) -> ActionResult`

取得 `ToolBar` 內所有項目的描述清單。

`result.value`：每項為含 `fxId`、`text`、`type`、`disabled` 的 dict。

---

## ScrollBar 操作

### `client.get_scroll_position(*, id) -> ActionResult`

讀取獨立 `ScrollBar` 的目前值。

`result.value`：含 `value`、`min`、`max`（均為 float）的 dict。

### `client.set_scroll_position(*, id, value) -> ActionResult`

設定捲動位置，超出 `[min, max]` 自動 clamp。

---

## Pagination 操作

### `client.get_page(*, id) -> ActionResult`

讀取 `Pagination` 控件的目前頁碼與總頁數。

`result.value`：含 `page`（0-based int）與 `page_count`（int）的 dict。

### `client.set_page(*, id, page) -> ActionResult`

跳到指定頁（0-based），超出範圍自動 clamp。

### `client.next_page(*, id) -> ActionResult`

前進一頁，在最後一頁時為 no-op。

### `client.prev_page(*, id) -> ActionResult`

後退一頁，在第一頁時為 no-op。

---

## Locator

### `client.locator(**selector) -> Locator`

回傳與此 client 及指定 selector 綁定的可重複使用 `Locator` 物件。

Locator 會儲存 selector，並在後續每次呼叫時自動代入，無需在每個 action 上重複寫 `id=...`。

```python
btn = client.locator(id="loginBtn")
btn.wait_for_visible()
btn.click()
btn.verify_text("Login")
```

若未傳入任何 selector 關鍵字，會拋出 `ValueError`。

### Locator 方法

以下所有方法等同於呼叫對應的 `client.*` 方法並自動帶入已儲存的 selector。

**互動操作**
- `loc.click()`
- `loc.double_click()`
- `loc.right_click()`
- `loc.press_key(key: str)`
- `loc.type(text: str)`

**文字 / 內容**
- `loc.get_text() -> ActionResult`
- `loc.verify_text(expected: str, *, match: str = "exact") -> ActionResult`
- `loc.get_tooltip() -> ActionResult`

**樣式**
- `loc.get_style() -> ActionResult`
- `loc.get_style_class() -> ActionResult`

**狀態查詢**
- `loc.is_visible() -> bool`
- `loc.is_enabled() -> bool`
- `loc.is_visited() -> bool`

**值 / 選取**
- `loc.get_value() -> ActionResult`
- `loc.get_progress() -> ActionResult`
- `loc.get_selected() -> ActionResult`
- `loc.get_selected_items() -> ActionResult`
- `loc.select(value: str) -> ActionResult`
- `loc.select_multiple(values: list[str]) -> ActionResult`
- `loc.set_selected(value: bool) -> ActionResult`
- `loc.set_slider(value: float) -> ActionResult`
- `loc.set_spinner(value: str) -> ActionResult`
- `loc.step_spinner(steps: int) -> ActionResult`

**Tab 頁籤**
- `loc.get_tabs() -> ActionResult`
- `loc.select_tab(tab: str) -> ActionResult`

**捲動**
- `loc.scroll_to() -> ActionResult`
- `loc.scroll_by(delta_x: float, delta_y: float) -> ActionResult`

**Accordion / Pane**
- `loc.expand_pane() -> ActionResult`
- `loc.collapse_pane() -> ActionResult`
- `loc.get_expanded() -> ActionResult`

**等待條件** — 需要 Locator 以 `id=` 建立

- `loc.wait_for_visible(timeout: float = 5.0)`
- `loc.wait_for_enabled(timeout: float = 5.0)`
- `loc.wait_for_node(timeout: float = 5.0)`
- `loc.wait_for_text(expected: str, timeout: float = 5.0)`
- `loc.wait_for_value(expected: str, timeout: float = 5.0)`

若 Locator 未以 `id=` 建立，會拋出 `ValueError`。


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

## `UISnapshot`

由 `client.snapshot()` 擷取的 scene graph 快照。

欄位：
- `nodes: list[dict[str, Any]]` — 擷取時的完整節點列表
- `timestamp: float` — Unix timestamp

Method：
- `save(path: str | Path) -> None` — 寫入 JSON 檔案
- `UISnapshot.load(path: str | Path) -> UISnapshot` — 從 JSON 還原

## `UIDiff`

比較兩個 `UISnapshot` 的結果，由 `client.diff(before, after)` 回傳。

欄位：
- `added: list[dict]`
- `removed: list[dict]`
- `changed: list[dict]`

## `RecordedEvent`

錄製 session 中擷取的單一互動事件。

欄位：
- `event_type: str` — `"click"` 或 `"type"`
- `fx_id: str` — 目標節點的 `fx:id`（未知時為空字串）
- `text: str` — 輸入文字（`"type"` 事件專用）
- `node_type: str` — JavaFX class 簡稱（如 `"Button"`）
- `node_index: int` — 同類型節點中的零起始索引
- `timestamp: float` — Unix timestamp

## `RecordedScript`

`client.stop_recording()` 的回傳值。

欄位：
- `events: list[RecordedEvent]`
- `script: str` — 產生的 Python 測試腳本字串

Method：
- `save(path: str | Path) -> None` — 將腳本寫入檔案

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


---

## OmniPage — Page Object Model

### class OmniPage(client)

Page Object Model 的基礎類別。繼承後加入方法，將同一個畫面或元件的 UI 操作封裝在一起。

`python
from omniui import OmniUI, OmniPage

class LoginPage(OmniPage):
    def login(self, username: str, password: str) -> None:
        self.client.input_text(id="username", text=username)
        self.client.input_text(id="password", text=password)
        self.client.click(id="loginButton")

    def get_status(self) -> str:
        return self.client.get_text(id="statusLabel").value

client = OmniUI.connect(port=48100)
page = LoginPage(client)
page.login("admin", "secret")
assert page.get_status() == "Welcome"
`

### page.locator(**selector) -> Locator

self.client.locator(**selector) 的簡寫。
