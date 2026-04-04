## Why

目前 OmniUI 無法模擬滑鼠懸停（hover），無法觸發依賴 `MOUSE_ENTERED`/`MOUSE_MOVED` 的
tooltip 顯示或 hover 狀態樣式變化。`hover(id=...)` 補上這個缺口，讓測試可以驗證
hover 觸發的 UI 行為。

## What Changes

- `hover(id=...)` — 模擬滑鼠移入節點，發送 `MouseEvent.MOUSE_ENTERED` + `MouseEvent.MOUSE_MOVED`
- Java agent 新增 `doHover()` 方法，以反射建構事件，與 `handleDoubleClick()` 結構一致
- Python `hover()` 方法加入 `OmniUIClient`

## Capabilities

### New Capabilities

- `hover`: `hover(id=...)` 發送 `MOUSE_ENTERED` + `MOUSE_MOVED` 到目標節點，觸發 tooltip 與 hover 狀態

### Modified Capabilities

（無）

## Impact

- `omniui/core/engine.py` — 新增 `hover()` 方法
- `omniui/client.py` — 公開 API 更新 docstring
- Java agent `ReflectiveJavaFxTarget.java` — 新增 `"hover"` case 與 `doHover()` 方法
- `tests/` — 新增 hover 單元測試
- `demo/python/` — 新增 hover_demo.py（使用 advanced-app tooltip target）
