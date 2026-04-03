## Purpose

定義 JavaFX ProgressBar 與 ProgressIndicator 控制項的 automation 行為。

## Requirements

### Requirement: ProgressBar and ProgressIndicator value read
系統 SHALL 支援讀取 `ProgressBar` 或 `ProgressIndicator` node 的目前進度值。
進度值為 0.0–1.0 範圍內的 double，或以 -1.0 表示不確定（indeterminate）狀態。

#### Scenario: Read ProgressBar value
- **WHEN** automation client 呼叫 `get_progress(id="<progressBarId>")`
- **THEN** 系統以 double（0.0–1.0）回傳目前進度

#### Scenario: Read indeterminate progress
- **WHEN** automation client 對處於 indeterminate 狀態的 ProgressBar 或 ProgressIndicator 呼叫 `get_progress`
- **THEN** 系統回傳 `-1.0`

#### Scenario: Read ProgressIndicator value
- **WHEN** automation client 呼叫 `get_progress(id="<progressIndicatorId>")`
- **THEN** 系統以 double（0.0–1.0）回傳目前進度