## ADDED Requirements

### Requirement: 輸入與導覽示範區段
Demo 應用程式 SHALL 為每種新控制元件加入專屬示範區段，
並提供對應的 Python demo 腳本完整執行自動化 API。

#### Scenario: RadioButton 與 ToggleButton 示範區段可見
- **WHEN** demo 應用程式啟動
- **THEN** id 為 `radioToggleSection` 的區段可見，包含至少兩個同一 ToggleGroup 的
  RadioButton 與一個 ToggleButton

#### Scenario: Slider 與 Spinner 示範區段可見
- **WHEN** demo 應用程式啟動
- **THEN** id 為 `sliderSpinnerSection` 的區段可見，包含
  `id="demoSlider"` 的 Slider 與 `id="demoSpinner"` 的 Spinner

#### Scenario: ProgressBar 示範區段可見
- **WHEN** demo 應用程式啟動
- **THEN** id 為 `progressSection` 的區段可見，包含
  `id="demoProgressBar"` 的 ProgressBar 與 `id="demoProgressIndicator"` 的 ProgressIndicator

#### Scenario: TabPane 示範區段可見
- **WHEN** demo 應用程式啟動
- **THEN** id 為 `tabSection` 的區段可見，包含 `id="demoTabPane"` 的 TabPane，
  至少三個分頁

#### Scenario: 所有新控制元件的 Python demo 腳本通過
- **WHEN** 每支新 Python demo 腳本對 demo 應用程式執行
- **THEN** 腳本以 exit code 0 結束，並印出成功訊息
