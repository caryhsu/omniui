## Why

Recorder 在 JavaFX `Dialog` 內點擊 OK/Cancel 等按鈕時，常命中 `DialogPane` 內部的 layout 節點（`ButtonBar`、`Pane`、`HBox`），產生 `click(type="Pane", index=0)` 這類脆弱 selector，錄出的腳本無法穩定重播。修復時機：`todo-app` dialog 已有完整 smoke test 可作 regression 基準。

## What Changes

- Java agent event recorder 新增 **actionable node 過濾**：只有節點屬於可操作類型（`Button`、`TextField`、`PasswordField`、`ComboBox`、`CheckBox`、`RadioButton`、`ToggleButton`、`Slider`、`Spinner`、`DatePicker`、`ColorPicker`、`ListView`、`TreeView`、`TableView`、`TextArea`、`Hyperlink`、`MenuButton`、`MenuItem`）才被錄製；純 layout 節點（`Pane`、`HBox`、`VBox`、`BorderPane`、`StackPane`、`ButtonBar`、`DialogPane`）一律忽略
- 非 actionable 節點被點擊時，向上 traverse parent chain 找最近的 actionable 祖先；若找到則錄製祖先，否則完全不錄製（不產生 `# WARN: fragile selector` 行）
- `script_gen.py` 或 `recorder_lite` 移除 `# WARN: fragile selector` 相關的 fallback 路徑（因 Java 端已過濾）

## Capabilities

### New Capabilities
- `recorder-actionable-filter`: Java agent 事件捕捉時過濾非 actionable layout 節點，並向上 traverse 找可用祖先

### Modified Capabilities
- `event-capture`: 事件捕捉行為新增 actionable 判斷 + parent traverse 邏輯
- `selector-inference`: selector 推導前置條件變更——只對 actionable 節點執行

## Impact

- `java-agent/src/main/java/dev/omniui/agent/EventRecorder.java`（或對應的 event capture class）
- `omniui/recorder_lite/` 或 `omniui/script_gen.py`（移除 WARN fallback）
- `tests/`（unit test 驗證 actionable filter 邏輯）
- `demo/java/todo-app/`（手動錄製驗證）
