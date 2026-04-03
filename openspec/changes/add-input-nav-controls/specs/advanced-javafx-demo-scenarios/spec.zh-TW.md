## ADDED Requirements

### Requirement: 進階示範應用程式擴充輸入與導覽控制元件
Demo 應用程式 SHALL 擴充五個新示範區段，涵蓋 RadioButton/ToggleButton、
Slider/Spinner、ProgressBar 與 TabPane，並為每個控制元件設定穩定的 fx:id。

#### Scenario: 新示範區段在執行中的應用程式可見
- **WHEN** 此 change 後的 demo 應用程式啟動
- **THEN** `get_nodes()` 回傳包含 `radioToggleSection`、`sliderSpinnerSection`、
  `progressSection`、`tabSection` 及其子控制元件節點的清單

#### Scenario: radio_toggle_demo.py 通過
- **WHEN** 執行 `python demo/python/radio_toggle_demo.py`
- **THEN** 腳本以 exit code 0 結束，已讀取並設定 RadioButton 與 ToggleButton 狀態

#### Scenario: slider_spinner_demo.py 通過
- **WHEN** 執行 `python demo/python/slider_spinner_demo.py`
- **THEN** 腳本以 exit code 0 結束，已設定 Slider 值並使用 Spinner 步進

#### Scenario: progress_demo.py 通過
- **WHEN** 執行 `python demo/python/progress_demo.py`
- **THEN** 腳本以 exit code 0 結束，已讀取 ProgressBar 與 ProgressIndicator 的值

#### Scenario: tab_demo.py 通過
- **WHEN** 執行 `python demo/python/tab_demo.py`
- **THEN** 腳本以 exit code 0 結束，已列舉分頁並以標題選取每個分頁
