## Context

CheckBox 與 ChoiceBox 是 JavaFX 標準控制項，在一般 UI 中極為常見。
現有 Java agent 已透過反射路徑實作 `get_selected`/`set_selected`（RadioButton 路徑）以及 `select`/`get_value`（ComboBox 路徑）。
兩個新控制項的底層 Java API 與已支援控制項完全相容：

- `CheckBox` 繼承 `ButtonBase → Labeled`，其 `isSelected()` / `setSelected(boolean)` 與 `ToggleButton`/`RadioButton` 完全相同
- `ChoiceBox<T>` 有 `getSelectionModel()` 返回 `SingleSelectionModel<T>`，與 `ComboBox` 使用相同介面；`getValue()` 同樣可用

## Goals / Non-Goals

**Goals:**
- 驗證 `CheckBox` 可透過現有 `get_selected` / `set_selected` action 運作，無需新增 Java 程式碼
- 驗證 `ChoiceBox` 可透過現有 `select` / `get_value` action 運作，無需新增 Java 程式碼
- 在 demo app 新增 CheckBox（含三態選項）與 ChoiceBox（含固定選項清單）區塊
- 提供可執行的 Python demo scripts

**Non-Goals:**
- `CheckBox` 三態（indeterminate）狀態的讀寫自動化（Phase 1 僅支援二態）
- `ChoiceBox` 動態 item 增刪
- Python engine 新增方法（複用既有 API）

## Decisions

### 決策 1：不新增 Java action，直接複用既有路徑
CheckBox 的 `setSelected(boolean)` 與 RadioButton 完全相同，反射路徑已可命中。
ChoiceBox 的 `SelectionModel.select(int/String)` 與 ComboBox 路徑相同。
→ **不修改 `ReflectiveJavaFxTarget.java`**，僅驗證；若有邊緣案例再局部修補。

### 決策 2：ChoiceBox `select` 策略
ComboBox 的 `handleSelect` 以字串比對 item list 取得 index 再呼叫 `select(int)`。
ChoiceBox 的 `getItems()` 同樣回傳 `ObservableList<T>`，同一路徑可重用。
→ 先以現有字串 `select` 嘗試；若失敗才追加 ChoiceBox 特定分支。

### 決策 3：Demo 區塊設計
- CheckBox 區塊：3 個獨立 CheckBox（不共用 ToggleGroup），驗證各自可獨立切換
- ChoiceBox 區塊：1 個含固定 3 選項的 ChoiceBox，驗證選取與讀值

## Risks / Trade-offs

- **[風險] ChoiceBox `select` 路徑不通** → 若 `handleSelect` 的 item 存取在 ChoiceBox 上失敗，需在 switch 新增 `"choicebox_select"` action；影響範圍極小
- **[風險] CheckBox indeterminate 狀態污染** → Demo 僅使用二態 CheckBox，避免觸發三態；規格明確排除 indeterminate
