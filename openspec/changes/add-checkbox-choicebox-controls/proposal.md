## Why

CheckBox 和 ChoiceBox 是兩個非常常見的 JavaFX 控制項，目前尚未有 automation demo 覆蓋。
兩者與已實作的控制項（RadioButton、ComboBox）共用相同的底層介面，預計幾乎不需要新的 Java 程式碼，可快速完成覆蓋。

## What Changes

- 驗證並確認 `CheckBox` 的 `get_selected` / `set_selected` action 可正常運作（與 RadioButton 共用相同路徑）
- 驗證並確認 `ChoiceBox` 的 `select` / `get_value` action 可正常運作（與 ComboBox 共用 SelectionModel 路徑）
- 在 `LoginDemoApp.java` 新增 CheckBox 與 ChoiceBox demo 區塊
- 新增 `checkbox_demo.py` Python demo script
- 新增 `choicebox_demo.py` Python demo script
- 將兩個 demo script 加入 `run_all.py`

## Capabilities

### New Capabilities

- `checkbox-automation`: 定義 JavaFX CheckBox 的 automation 行為，包含讀取與設定 selected 狀態
- `choicebox-automation`: 定義 JavaFX ChoiceBox 的 automation 行為，包含選取項目與讀取目前值

### Modified Capabilities

- `javafx-automation-core`: 新增 CheckBox、ChoiceBox 為受支援的控制項類型
- `advanced-javafx-demo-scenarios`: 新增 CheckBox 與 ChoiceBox 的 demo 場景描述

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java`：預期無需修改（已有共用路徑）；若需修補則改動極小
- `demo/javafx-login-app/.../LoginDemoApp.java`：新增兩個 demo 區塊
- `omniui/core/engine.py`：無需新增方法（複用 `get_selected`、`set_selected`、`select`、`get_value`）
- `demo/python/`：新增兩個 demo script，更新 `run_all.py`
