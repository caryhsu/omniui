## Purpose

定義 JavaFX DatePicker 控制項的 automation 行為。

## Requirements

### Requirement: DatePicker popup open
系統 SHALL 支援透過分派點擊 DatePicker 的日曆切換按鈕，開啟 DatePicker 的日曆 popup。

#### Scenario: Open DatePicker popup via selector
- **WHEN** Python client 呼叫 `open_datepicker(selector)` 鎖定一個 DatePicker node
- **THEN** 系統分派點擊 DatePicker 的日曆切換按鈕，並等到日曆 popup 視窗可見後才回傳

### Requirement: DatePicker date selection
系統 SHALL 支援在已開啟的 DatePicker popup 中，透過比對目標日期儲存格的 `LocalDate` item property 值來選取特定日期。

#### Scenario: Select a date visible in the current month
- **WHEN** DatePicker popup 已開啟，目標日期在目前顯示的月份中，且 Python client 呼叫 `pick_date(date="2025-03-15")`
- **THEN** 系統定位 `LocalDate` item 符合 2025 年 3 月 15 日的日期儲存格並分派點擊事件

#### Scenario: Select a date requiring forward month navigation
- **WHEN** DatePicker popup 已開啟，目標日期在目前未顯示的未來月份中，且 Python client 呼叫 `pick_date(date="2025-06-20")`
- **THEN** 系統透過點擊下一月按鈕向前導航所需次數到達 2025 年 6 月，再分派點擊符合 6 月 20 日的儲存格

#### Scenario: Select a date requiring backward month navigation
- **WHEN** DatePicker popup 已開啟，目標日期在目前未顯示的過去月份中，且 Python client 呼叫 `pick_date(date="2025-01-05")`
- **THEN** 系統透過點擊上一月按鈕向後導航所需次數到達 2025 年 1 月，再分派點擊符合 1 月 5 日的儲存格

### Requirement: DatePicker month navigation
系統 SHALL 支援透過分派點擊導航按鈕，在 DatePicker 日曆中每次向前或向後移動一個月。

#### Scenario: Navigate to the next month
- **WHEN** DatePicker popup 已開啟，且 Python client 呼叫 `navigate_month(direction="forward")`
- **THEN** 系統分派點擊下一月導航按鈕，並等待日曆格更新至下一個月

#### Scenario: Navigate to the previous month
- **WHEN** DatePicker popup 已開啟，且 Python client 呼叫 `navigate_month(direction="backward")`
- **THEN** 系統分派點擊上一月導航按鈕，並等待日曆格更新至前一個月