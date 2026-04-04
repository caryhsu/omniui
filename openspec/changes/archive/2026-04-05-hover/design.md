## Context

`handleDoubleClick()` 已示範以反射建構 `MouseEvent` 並發送的完整流程。
Hover 需要發送 `MOUSE_ENTERED`（進入節點觸發 tooltip）和 `MOUSE_MOVED`（讓 hover state 保持）。
`get_tooltip()` 已能讀取 tooltip 文字，hover 讓測試可以**觸發** tooltip 後再讀取。

## Goals / Non-Goals

**Goals:**
- `hover(id=...)` 發送 `MOUSE_ENTERED` + `MOUSE_MOVED` 到目標節點
- 使用節點中心座標，與 `handleDoubleClick()` 計算方式一致
- headless 環境下 screen 座標回退到 (0, 0) 但仍發送事件

**Non-Goals:**
- 不模擬 `MOUSE_EXITED`（unhover）
- 不等待 tooltip 顯示動畫完成

## Decisions

### 1. 發送兩個事件：MOUSE_ENTERED + MOUSE_MOVED

- `MOUSE_ENTERED` 讓 JavaFX tooltip 機制啟動計時器
- `MOUSE_MOVED` 確保 hover state CSS pseudo-class（`:hover`）被套用
- 替代方案：只發 MOUSE_ENTERED。拒絕原因：部分控制項只對 MOUSE_MOVED 反應 hover state

### 2. 反射路徑與 handleDoubleClick() 共用結構

座標計算（`getBoundsInLocal` → center → `localToScreen`）和 `MouseEvent` 建構子
完全相同，只換 event type 欄位。減少重複程式碼的方式留給後續重構。

### 3. Python API：無額外參數

`hover(**selector)` — 僅 selector，無額外選項。簡單直覺。

## Risks / Trade-offs

- **Tooltip 顯示有延遲**：JavaFX tooltip 預設延遲約 1 秒才顯示，`hover()` 後需搭配 `wait_for_*` 才能可靠驗證。
  → Mitigation：demo 和文件說明需搭配 `wait_for_visible` 或 `time.sleep`。
- **反射脆弱性**：同 double_click，JavaFX 升版若改建構子會 break。
  → Mitigation：現有 double_click 已驗證，風險已知且可接受。
