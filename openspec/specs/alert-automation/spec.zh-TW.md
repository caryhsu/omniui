## Purpose

定義 JavaFX Alert 控制項的 automation 行為。

## Requirements

### Requirement: Alert detection and type identification
系統 SHALL 偵測一個已開啟的 JavaFX Alert 並辨識其 `AlertType`，回傳包含 alert 類型、標題、header text（若存在）、內容文字及可用按鈕標籤的描述物件。

#### Scenario: Detect an open CONFIRMATION Alert
- **WHEN** 一個類型為 CONFIRMATION 的 JavaFX Alert 顯示，且 Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳包含 `alert_type` 為 "CONFIRMATION" 的描述物件，以及標題與內容文字

#### Scenario: Detect an ERROR Alert
- **WHEN** 一個類型為 ERROR 的 JavaFX Alert 顯示，且 Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳包含 `alert_type` 為 "ERROR" 及 alert 內容文字的描述物件

#### Scenario: Detect an INFORMATION Alert
- **WHEN** 一個類型為 INFORMATION 的 JavaFX Alert 顯示，且 Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳包含 `alert_type` 為 "INFORMATION" 及 alert 內容文字的描述物件

#### Scenario: Detect a WARNING Alert
- **WHEN** 一個類型為 WARNING 的 JavaFX Alert 顯示，且 Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳包含 `alert_type` 為 "WARNING" 及 alert 內容文字的描述物件

### Requirement: Alert message reading
系統 SHALL 讀取已開啟 Alert 的 header text 與 content text。

#### Scenario: Read Alert content text
- **WHEN** 一個 Alert 開啟，內容文字為 "File not found"
- **THEN** 呼叫 `get_dialog()` 回傳的描述物件中，`content_text` 為 "File not found"

#### Scenario: Read Alert with both header text and content text
- **WHEN** 一個 Alert 開啟，header text 為 "Save failed"，content text 為 "The disk is full"
- **THEN** 呼叫 `get_dialog()` 回傳的描述物件中，`header_text` 為 "Save failed"，`content_text` 為 "The disk is full"

### Requirement: Alert button interaction
系統 SHALL 支援透過比對標籤文字點擊已開啟 Alert 中的指定按鈕，並以 `ButtonData` 語意類型作為備選比對策略。

#### Scenario: Click the OK button in an INFORMATION Alert
- **WHEN** 一個 INFORMATION Alert 開啟，且 Python client 呼叫 `dismiss_dialog(button="OK")`
- **THEN** 系統在 Alert 的 `ButtonBar` 中定位 OK 按鈕並分派點擊事件

#### Scenario: Click Cancel in a CONFIRMATION Alert
- **WHEN** 一個包含 OK 與 Cancel 按鈕的 CONFIRMATION Alert 開啟，且 Python client 呼叫 `dismiss_dialog(button="Cancel")`
- **THEN** 系統定位 Cancel 按鈕並分派點擊，Alert 隨之關閉

#### Scenario: Click a button by ButtonData type in an Alert
- **WHEN** 一個 CONFIRMATION Alert 開啟，且 Python client 呼叫 `dismiss_dialog(button_type="OK_DONE")`
- **THEN** 系統定位 `ButtonData` 符合 `OK_DONE` 的按鈕並分派點擊事件