## Context

`handleClick()` 目前呼叫 `node.fire()`，這個路徑不帶任何修飾鍵資訊。
`handleDoubleClick()` 已示範透過反射建構 `MouseEvent.MOUSE_CLICKED`，並帶入
`shiftDown/controlDown/altDown/metaDown` 布林值。
`parseKeyString()` 已支援大小寫不分、`Ctrl`=`Control`、`Win`=`Meta` 別名。

本次變更讓 `click()` 也能接收 `modifiers` 陣列，在有修飾鍵時改走 MouseEvent 路徑。

## Goals / Non-Goals

**Goals:**
- `click(id=..., modifiers=["Ctrl"])` 模擬 Ctrl+click（加選）
- `click(id=..., modifiers=["Shift"])` 模擬 Shift+click（範圍選取）
- modifier 字串規則與 `press_key` 一致：大小寫不分、`Ctrl`=`Control`、`Win`=`Meta`
- 無 modifiers 時行為完全不變（backward compatible）

**Non-Goals:**
- 不支援 modifier+double_click
- 不支援 modifier+right_click
- 不模擬 MOUSE_PRESSED / MOUSE_RELEASED，只發 MOUSE_CLICKED（與 double_click 行為一致）

## Decisions

### 1. 有無修飾鍵走不同路徑

- **無 modifiers**：繼續呼叫 `node.fire()`，最相容、不影響現有測試
- **有 modifiers**：改走 MouseEvent 反射路徑（同 `handleDoubleClick`），將解析出的布林值填入 `shiftDown/controlDown/altDown/metaDown`

替代方案考慮：永遠走 MouseEvent 路徑。拒絕原因：部分控制項（Button）對 `fire()` 更可靠，不必破壞現有行為。

### 2. modifier 字串解析重用 `parseKeyString()`

對每個 modifier 字串呼叫 `parseKeyString(s)`，取回 `parts[0]` 後 switch 設定布林值。
這確保 `"ctrl"`、`"Ctrl"`、`"Control"` 三者等價，與 `press_key` 行為一致。

### 3. Python API：`modifiers` 為可選 list[str]，預設 None

`click(id=..., modifiers=["Ctrl", "Shift"])` — 加入 `modifiers` 關鍵字參數，
payload 帶 `"modifiers": ["Ctrl", "Shift"]`；無 modifiers 時不帶此欄位，Java 端視為空。

### 4. `handleClick` 簽章加入 payload 參數

現在是 `handleClick(node, fxId, handle)`，改為 `handleClick(node, fxId, handle, payload)`，
與 `handleSelect`、`handleType` 等統一。

## Risks / Trade-offs

- **MouseEvent 反射脆弱性**：JavaFX 版本升級若改變建構子簽章會 break。
  → Mitigation：與 `handleDoubleClick` 使用同一段反射程式碼，已在現有測試中驗證。
- **Ctrl+click 在不同控制項行為不同**：ListView MULTIPLE 模式可加選；SINGLE 模式行為未定義。
  → Mitigation：spec 中說明行為由 JavaFX 控制項決定，agent 只負責發送事件。
