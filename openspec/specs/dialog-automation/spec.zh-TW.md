## Purpose

定義 JavaFX Dialog 控制項的 automation 行為。

## Requirements

### Requirement: Dialog detection
系統 SHALL 透過辨識 scene root 為 `DialogPane` 的可見 Stage 來偵測已開啟的 JavaFX Dialog，並回傳包含 dialog 標題、header text（若存在）、content text 及可用按鈕標籤清單的描述物件。

#### Scenario: Detect an open modal Dialog
- **WHEN** 一個 JavaFX Dialog 已顯示，且 Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳包含 dialog 標題、header text（若存在）、content text 及可用按鈕標籤的 dialog 描述物件

#### Scenario: Return empty result when no Dialog is open
- **WHEN** 沒有 JavaFX Dialog 可見，且 Python client 呼叫 `get_dialog()`
- **THEN** 系統回傳空結果，不拋出錯誤

### Requirement: Dialog content reading
系統 SHALL 從 `DialogPane` node 樹讀取已開啟 Dialog 的標題、header text 與 content text。

#### Scenario: Read Dialog title and content text
- **WHEN** 一個 modal Dialog 開啟，標題為 "Confirm Action"，content text 為 "Are you sure you want to delete this file?"
- **THEN** 呼叫 `get_dialog()` 回傳的描述物件中，`title` 為 "Confirm Action"，`content_text` 為 "Are you sure you want to delete this file?"

#### Scenario: Read Dialog with header text
- **WHEN** 一個 modal Dialog 開啟，header text 為 "Unsaved Changes"，content text 為 "Save before closing?"
- **THEN** 呼叫 `get_dialog()` 回傳的描述物件中，`header_text` 為 "Unsaved Changes"，`content_text` 為 "Save before closing?"

### Requirement: Dialog button interaction
系統 SHALL 支援透過比對標籤文字點擊已開啟 Dialog 中的指定按鈕，並以 `ButtonData` 語意類型作為備選比對策略。

#### Scenario: Click a Dialog button by label text
- **WHEN** 一個 modal Dialog 開啟，且 Python client 呼叫 `dismiss_dialog(button="OK")`
- **THEN** 系統在 `DialogPane` 的 `ButtonBar` 中定位標籤為 "OK" 的按鈕並分派點擊事件

#### Scenario: Click a Dialog button by ButtonData type
- **WHEN** 一個 modal Dialog 開啟，且 Python client 呼叫 `dismiss_dialog(button_type="CANCEL_CLOSE")`
- **THEN** 系統定位 `ButtonData` 符合 `CANCEL_CLOSE` 的按鈕並分派點擊事件

#### Scenario: Report error when button is not found
- **WHEN** 一個 modal Dialog 開啟，且 Python client 呼叫 `dismiss_dialog(button="Apply")`，但不存在 "Apply" 按鈕
- **THEN** 系統回報錯誤並列出可用按鈕標籤，不關閉 Dialog