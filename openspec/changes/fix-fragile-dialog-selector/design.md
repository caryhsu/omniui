## Context

**目前架構：**

`onMouseClicked` 中，`nearestNode(target)` 回傳任何 `javafx.scene.*` 節點——包含 `Pane`、`HBox`、`VBox`、`ButtonBar` 等純 layout 節點。

接著進行幾個特例 early-return（`isInsideColorPicker`、`isInsideComboBox`、`isInsideDialogButton`），但**沒有通用的 layout node 過濾**。

若 click 命中 layout 節點（`fxId=""`、`text=""`），`selector_inference.py` 最終落到 fallback：
```
{"type": "Pane", "index": 0, "_fragile": True}
```
生成 `click(type="Pane", index=0)  # WARN: fragile selector`，重播時常失敗。

**現有 early-return 模式（可參考）：**
- `isInsideColorPicker` — 向上 traverse，找到 `ColorPicker` → return
- `isInsideDialogButton` — 向上 traverse，找到 ButtonBar 內的 Button → 改錄 `dismiss_dialog`

## Goals / Non-Goals

**Goals:**
- 在 `onMouseClicked` 加入 actionable check：若命中節點為非 actionable layout 節點，向上 traverse 找最近的 actionable 祖先
- 若找到 actionable 祖先且有 `fxId` 或 `text` → 以祖先錄製
- 若找不到任何 actionable 祖先 → 完全不錄製（`return`）
- 消除 `# WARN: fragile selector` 在 dialog 中的出現
- Python `selector_inference.py` 的 `_fragile` fallback 邏輯保留（非 dialog 環境仍可能用到）

**Non-Goals:**
- 不改動 drag、key 等其他事件的錄製路徑
- 不移除 Python 端 `_fragile` 機制（保留向後相容）
- 不處理 dialog 以外的畫面（main scene 的 layout node click 本來就少見）

## Decisions

### D1：修改位置 — Java agent（`ReflectiveJavaFxTarget.java`），而非 Python 端

**選擇**：在 Java `onMouseClicked` 中過濾，緊接 `isInsideDialogButton` check 之後。

**理由**：保持「骯髒資料不進 buffer」原則，與 ColorPicker/ComboBox early-return 模式一致。若改 Python 端，buffer 還是髒的，未來其他消費者也會受影響。

### D2：actionable node 集合 — 以 class `SimpleName` 判斷

```
ACTIONABLE = { Button, ButtonBase, ToggleButton, CheckBox, RadioButton,
               TextField, TextArea, PasswordField, ComboBox, ChoiceBox,
               Slider, Spinner, DatePicker, ColorPicker,
               ListView, TreeView, TableView, TableCell,
               Hyperlink, Label, MenuButton, MenuItem }
```

**理由**：用 `getSimpleName()` 反射，不需要 import JavaFX 類別（agent 設計為 reflective-only）。Label 納入是因為 dialog 內有些可點擊 Label（確認文字）；若 Label 無 fxId/text 就仍會 fallback。

### D3：traverse 上限 — 15 層（與其他 traverse 一致）

```java
private boolean isActionable(Object node) { ... }
private Object nearestActionableAncestor(Object node) {
    // walk up to 15 parents looking for actionable
}
```

若 15 層內無 actionable → 不錄製。

### D4：只在 `onMouseClicked` 加過濾，`onMousePressed` 保留原樣

`onMousePressed` 只記錄座標供 drag 偵測，不直接寫 buffer，不需改。

## Risks / Trade-offs

- **[Risk]** 某些自定義 control 繼承 `Pane` 卻實際可點 → **Mitigation**: 若有 `fxId` 就不會 fragile（selector_inference 第一優先），影響範圍僅限無 id 的自定義 layout
- **[Risk]** traverse 找到更上層的祖先，index 計算可能偏移 → **Mitigation**: `nodeIndexOf` 重新對祖先計算；已有相同模式（`isInsideDialogButton` 錄製祖先 Button）

## Migration Plan

1. 修改 `ReflectiveJavaFxTarget.java` — 加入 `isActionableNode()` + `nearestActionableAncestor()`
2. 在 `onMouseClicked` 的 `isInsideDialogButton` check 之後加入過濾
3. 重新 build agent JAR
4. 手動驗證：用 Recorder 錄製 todo-app dialog 操作，確認無 `# WARN` 輸出
5. 更新 unit test：新增 `test_selector_inference.py` case（layout node fallback 不應出現於 dialog 場景）
