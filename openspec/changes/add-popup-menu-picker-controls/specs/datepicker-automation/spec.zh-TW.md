## ADDED Requirements

### Requirement: DatePicker popup 開啟
系統必須支援透過對 DatePicker 的 calendar toggle button dispatch click，開啟 DatePicker 的 calendar popup。

#### Scenario: 透過 selector 開啟 DatePicker popup
- **WHEN** Python client 對 DatePicker node 呼叫 `open_datepicker(selector)`
- **THEN** 系統對 DatePicker 的 calendar toggle button dispatch click，並等待 calendar popup window 可見後才回傳

### Requirement: DatePicker 日期選取
系統必須支援在已開啟的 DatePicker popup 中，透過比對目標日期格的 `LocalDate` item property 值來選取特定日期。

#### Scenario: 選取當前顯示月份中的日期
- **WHEN** DatePicker popup 已開啟，目標日期在目前顯示的月份內，Python client 呼叫 `pick_date(date="2025-03-15")`
- **THEN** 系統找到 `LocalDate` item 為 2025 年 3 月 15 日的日期格，並對它 dispatch click event

#### Scenario: 選取需向前導航月份的日期
- **WHEN** DatePicker popup 已開啟，目標日期在目前未顯示的未來月份，Python client 呼叫 `pick_date(date="2025-06-20")`
- **THEN** 系統多次點擊下個月按鈕導航到 2025 年 6 月，再對符合 2025 年 6 月 20 日的格 dispatch click

#### Scenario: 選取需向後導航月份的日期
- **WHEN** DatePicker popup 已開啟，目標日期在目前未顯示的過去月份，Python client 呼叫 `pick_date(date="2025-01-05")`
- **THEN** 系統多次點擊上個月按鈕導航到 2025 年 1 月，再對符合 2025 年 1 月 5 日的格 dispatch click

### Requirement: DatePicker 月份導航
系統必須支援透過對導航按鈕 dispatch click，每次將 DatePicker calendar 向前或向後導航一個月。

#### Scenario: 導航到下個月
- **WHEN** DatePicker popup 已開啟，Python client 呼叫 `navigate_month(direction="forward")`
- **THEN** 系統對下個月導航按鈕 dispatch click，並等待 calendar grid 更新為下一個月

#### Scenario: 導航到上個月
- **WHEN** DatePicker popup 已開啟，Python client 呼叫 `navigate_month(direction="backward")`
- **THEN** 系統對上個月導航按鈕 dispatch click，並等待 calendar grid 更新為前一個月
