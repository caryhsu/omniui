## ADDED Requirements

### Requirement: Python client connection API
系統必須提供 Python client entry point，使其可與 target JavaFX application 所對應的本機 OmniUI automation engine 建立 session。

#### Scenario: 從 Python script 建立連線
- **WHEN** 使用者在受支援的本機環境中呼叫 `OmniUI.connect()`
- **THEN** client 必須回傳一個 session object，能執行 discovery、action、screenshot、OCR 與 vision operation

### Requirement: High-level action API
系統必須提供高階 Python method：`click`、`type`、`get_text`、`verify_text`，且這些 method 接受的 selector 必須與 selector resolution engine 一致。

#### Scenario: 透過 Python client 驗證 login status
- **WHEN** script 呼叫 `verify_text(id="status", expected="Success")`
- **THEN** client 必須透過 automation engine resolve target，若取得文字與 expected value 不相等則使 action fail

### Requirement: Low-level troubleshooting API
系統必須提供低階 Python method：`find`、`screenshot`、`ocr`、`vision_match`，以支援 debug 與進階 automation flow。

#### Scenario: 顯式檢查 OCR 結果
- **WHEN** 使用者透過 Python client 呼叫 `ocr(image)`
- **THEN** 系統必須回傳辨識出的文字結果與位置 metadata，以便手動除錯

### Requirement: Login-flow script compatibility
系統必須支援對受支援的 JavaFX application，完全以 Python automation script 執行 Phase 1 demo login flow。

#### Scenario: 執行完整 login script
- **WHEN** script 依序點擊 username field、輸入 credential、點擊 login button，並驗證 success text
- **THEN** 整個流程必須成功執行，且使用者不需要在中途切換 API 或工具

### Requirement: Stable selector argument model
系統必須允許高階 Python action 使用 `id`、`text`、`type` 等 selector field，而不在 script surface 中暴露 backend-specific transport 或 adapter 細節。

#### Scenario: 在 click API 中使用 structural selector argument
- **WHEN** 使用者呼叫 `click(text="Login", type="Button")`
- **THEN** Python API 必須接受此 selector，並由 backend 自行完成 resolution，而不是要求 caller 顯式指定 OCR 或 vision flag
