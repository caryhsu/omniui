## ADDED Requirements

### Requirement: Alert 偵測與類型識別
系統必須偵測已開啟的 JavaFX Alert 並識別其 `AlertType`，回傳包含 alert 類型、title、header text（若有）、content text，以及可用 button label 的 descriptor。

#### Scenario: 偵測已開啟的 CONFIRMATION Alert
- **WHEN** type 為 CONFIRMATION 的 JavaFX Alert 已顯示，Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳 descriptor，其中 `alert_type` 為 "CONFIRMATION"，並包含 title 與 content text

#### Scenario: 偵測 ERROR Alert
- **WHEN** type 為 ERROR 的 JavaFX Alert 已顯示，Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳 `alert_type` 為 "ERROR" 的 descriptor，並包含 alert 的 content text

#### Scenario: 偵測 INFORMATION Alert
- **WHEN** type 為 INFORMATION 的 JavaFX Alert 已顯示，Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳 `alert_type` 為 "INFORMATION" 的 descriptor，並包含 alert 的 content text

#### Scenario: 偵測 WARNING Alert
- **WHEN** type 為 WARNING 的 JavaFX Alert 已顯示，Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳 `alert_type` 為 "WARNING" 的 descriptor，並包含 alert 的 content text

### Requirement: Alert 訊息讀取
系統必須讀取已開啟 Alert 的 header text 與 content text。

#### Scenario: 讀取 Alert content text
- **WHEN** Alert 已開啟，content text 為 "File not found"
- **THEN** 呼叫 `get_dialog()` 回傳 descriptor，其中 `content_text` 為 "File not found"

#### Scenario: 同時讀取 Alert header text 與 content text
- **WHEN** Alert 已開啟，header text 為 "Save failed"，content text 為 "The disk is full"
- **THEN** 呼叫 `get_dialog()` 回傳 descriptor，其中 `header_text` 為 "Save failed"，`content_text` 為 "The disk is full"

### Requirement: Alert 按鈕互動
系統必須支援以 label text 匹配，點擊已開啟 Alert 中的指定按鈕，並支援以 `ButtonData` 語意類型作為替代匹配策略。

#### Scenario: 點擊 INFORMATION Alert 的 OK 按鈕
- **WHEN** INFORMATION Alert 已開啟，Python client 呼叫 `dismiss_dialog(button="OK")`
- **THEN** 系統找到 Alert 的 `ButtonBar` 中的 OK 按鈕，並對它 dispatch click

#### Scenario: 點擊 CONFIRMATION Alert 的 Cancel 按鈕
- **WHEN** 含有 OK 與 Cancel 按鈕的 CONFIRMATION Alert 已開啟，Python client 呼叫 `dismiss_dialog(button="Cancel")`
- **THEN** 系統找到 Cancel 按鈕並 dispatch click，Alert 被關閉

#### Scenario: 以 ButtonData type 點擊 Alert 按鈕
- **WHEN** CONFIRMATION Alert 已開啟，Python client 呼叫 `dismiss_dialog(button_type="OK_DONE")`
- **THEN** 系統找到 `ButtonData` 匹配 `OK_DONE` 的按鈕，並對它 dispatch click event
