## ADDED Requirements

### Requirement: Dialog 偵測
系統必須透過識別 scene root 為 `DialogPane` 的可見 Stage 來偵測已開啟的 JavaFX Dialog，並回傳包含 dialog 標題、header text（若有）、content text，以及可用 button label 清單的 descriptor。

#### Scenario: 偵測已開啟的 modal Dialog
- **WHEN** JavaFX Dialog 已顯示，Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳 dialog descriptor，包含 dialog 標題、header text（若有）、content text，以及可用 button label

#### Scenario: 無 Dialog 開啟時回傳空結果
- **WHEN** 沒有可見的 JavaFX Dialog，Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳空結果，不得拋出 error

### Requirement: Dialog 內容讀取
系統必須從 `DialogPane` node tree 讀取已開啟 Dialog 的 title、header text 與 content text。

#### Scenario: 讀取 Dialog 的 title 與 content text
- **WHEN** modal Dialog 已開啟，title 為 "Confirm Action"，content text 為 "Are you sure you want to delete this file?"
- **THEN** 呼叫 `get_dialog()` 回傳 descriptor，其中 `title` 為 "Confirm Action"，`content_text` 為 "Are you sure you want to delete this file?"

#### Scenario: 讀取含 header text 的 Dialog
- **WHEN** modal Dialog 已開啟，header text 為 "Unsaved Changes"，content text 為 "Save before closing?"
- **THEN** 呼叫 `get_dialog()` 回傳 descriptor，其中 `header_text` 為 "Unsaved Changes"，`content_text` 為 "Save before closing?"

### Requirement: Dialog 按鈕互動
系統必須支援以 label text 匹配，點擊已開啟 Dialog 中的指定按鈕，並支援以 `ButtonData` 語意類型作為替代匹配策略。

#### Scenario: 以 label text 點擊 Dialog 按鈕
- **WHEN** modal Dialog 已開啟，Python client 呼叫 `dismiss_dialog(button="OK")`
- **THEN** 系統在 `DialogPane` 的 `ButtonBar` 中找到 label 為 "OK" 的按鈕，並對它 dispatch click event

#### Scenario: 以 ButtonData type 點擊 Dialog 按鈕
- **WHEN** modal Dialog 已開啟，Python client 呼叫 `dismiss_dialog(button_type="CANCEL_CLOSE")`
- **THEN** 系統找到 `ButtonData` 匹配 `CANCEL_CLOSE` 的按鈕，並對它 dispatch click event

#### Scenario: 找不到按鈕時回報錯誤
- **WHEN** modal Dialog 已開啟，Python client 呼叫 `dismiss_dialog(button="Apply")`，但不存在 "Apply" 按鈕
- **THEN** 系統回報 error 並列出可用 button label，不得關閉 Dialog
