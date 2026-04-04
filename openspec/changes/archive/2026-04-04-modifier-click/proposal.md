## Why

目前 `click()` 只支援純點擊，無法模擬 Ctrl+click（加選）或 Shift+click（範圍選取）。
這些是 ListView / TableView 多選操作的標準互動方式，補上後可用原生點擊語義替代
`select_multiple()`，更貼近真實使用者操作。

## What Changes

- `click(id=..., modifiers=["Ctrl"])` — Ctrl+click 加選單一項目
- `click(id=..., modifiers=["Shift"])` — Shift+click 範圍選取
- `click(id=..., modifiers=["Ctrl", "Shift"])` — 組合修飾鍵
- Java agent `doClick()` 接收 `modifiers` 陣列，按住修飾鍵再點擊再放開
- modifier 字串重用現有 `parseKeyString()` 邏輯：大小寫不區分，`Ctrl`=`Control`，`Win`=`Meta`

## Capabilities

### New Capabilities

- `modifier-click`: `click()` 加入 `modifiers` 可選參數，支援修飾鍵點擊

### Modified Capabilities

- `click`: Python API 新增 `modifiers: list[str] | None = None` 可選參數（非 breaking）

## Impact

- `omniui/core/engine.py` — `click()` 加 `modifiers` 參數，payload 帶入 `modifiers` 陣列
- `omniui/client.py` — 公開 API `click()` 更新簽章
- Java agent `ReflectiveJavaFxTarget.java` — `doClick()` 解析 `modifiers`，重用 `parseKeyString()`，在 `MouseEvent` 前後模擬修飾鍵按下/放開
- `tests/` — 新增 modifier-click 單元測試
- `demo/python/` — 新增 modifier_click_demo.py 示範
