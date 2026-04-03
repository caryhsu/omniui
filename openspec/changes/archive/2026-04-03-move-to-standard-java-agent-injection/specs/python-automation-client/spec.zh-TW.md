## MODIFIED Requirements

### Requirement: Python client connection API
系統 SHALL 提供 Python client entry point，且只有在 target JavaFX app 以 OmniUI agent injection 啟動時才可成功建立 session。

#### Scenario: Connect from Python script to agent-enabled app
- **WHEN** 使用者呼叫 `OmniUI.connect()`，且 target JavaFX app 已以 OmniUI Java agent enabled 模式啟動
- **THEN** client 會回傳可進行 discovery、action、screenshot、OCR 與 vision operations 的 session object

#### Scenario: Fail connection to plain app mode
- **WHEN** 使用者呼叫 `OmniUI.connect()`，但 target JavaFX app 是在未注入 OmniUI Java agent 的情況下啟動
- **THEN** client 會明確回報 connection failure，而不是表現得像有可控制 target 存在

### Requirement: Login-flow script compatibility
系統 SHALL 支援 Phase 1 demo login flow 完整透過 Python automation script 執行，且 target JavaFX app 使用的是 agent-enabled launch mode。

#### Scenario: Run complete login script against injected app
- **WHEN** demo JavaFX app 以 agent-enabled mode 啟動，且 script 點擊 username、輸入 credentials、點擊 login button，最後驗證 success text
- **THEN** 整個流程可以成功執行，且 target app source 不需要包含 OmniUI-specific bootstrap code
