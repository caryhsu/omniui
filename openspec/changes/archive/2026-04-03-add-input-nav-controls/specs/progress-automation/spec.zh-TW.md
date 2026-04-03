## ADDED Requirements

### Requirement: ProgressBar 與 ProgressIndicator 進度讀取
系統 SHALL 支援讀取 `ProgressBar` 或 `ProgressIndicator` 節點的當前進度值。
進度值為 double，範圍 0.0–1.0；若為不確定狀態則回傳 -1.0。

#### Scenario: 讀取 ProgressBar 進度
- **WHEN** 自動化客戶端呼叫 `get_progress(id="<progressBarId>")`
- **THEN** 系統回傳當前進度（double，0.0–1.0）

#### Scenario: 讀取不確定狀態進度
- **WHEN** 自動化客戶端對處於不確定狀態的 ProgressBar 或 ProgressIndicator
  呼叫 `get_progress`
- **THEN** 系統回傳 `-1.0`

#### Scenario: 讀取 ProgressIndicator 進度
- **WHEN** 自動化客戶端呼叫 `get_progress(id="<progressIndicatorId>")`
- **THEN** 系統回傳當前進度（double，0.0–1.0）
