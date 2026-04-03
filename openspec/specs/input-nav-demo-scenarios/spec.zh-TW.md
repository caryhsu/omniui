## Purpose

定義參考應用程式中輸入與導航控制項的 demo 場景。

## Requirements

### Requirement: Input and navigation demo scenes
demo 應用程式 SHALL 為每個新控制項類型包含專屬的 demo 區塊，且對應的 Python demo script SHALL 完整覆蓋每個控制項的 automation API。

#### Scenario: RadioButton and ToggleButton demo section visible
- **WHEN** demo 應用程式正在執行
- **THEN** 可見一個 id 為 `radioToggleSection` 的區塊，包含至少兩個在同一 ToggleGroup 中的 RadioButton 及一個 ToggleButton

#### Scenario: Slider and Spinner demo section visible
- **WHEN** demo 應用程式正在執行
- **THEN** 可見一個 id 為 `sliderSpinnerSection` 的區塊，包含一個 Slider（`id="demoSlider"`）及一個 Spinner（`id="demoSpinner"`）

#### Scenario: ProgressBar demo section visible
- **WHEN** demo 應用程式正在執行
- **THEN** 可見一個 id 為 `progressSection` 的區塊，包含一個 ProgressBar（`id="demoProgressBar"`）及一個 ProgressIndicator（`id="demoProgressIndicator"`）

#### Scenario: TabPane demo section visible
- **WHEN** demo 應用程式正在執行
- **THEN** 可見一個 id 為 `tabSection` 的區塊，包含一個 TabPane（`id="demoTabPane"`），其中至少有三個 tab

#### Scenario: Python demo scripts pass for all new controls
- **WHEN** 對 demo 應用程式執行每個新的 Python demo script
- **THEN** script 以 exit code 0 結束，並輸出成功訊息