## Purpose

定義 Python client 在連接 agent-enabled JavaFX app 與執行 automation flow 時的穩定行為。

## Requirements

### Requirement: Python client connection API
系統 SHALL 提供 Python client entry point，且只有在 target JavaFX app 以 OmniUI agent injection 啟動時才可成功建立 session。

#### Scenario: Connect from Python script to agent-enabled app
- **WHEN** 使用者呼叫 `OmniUI.connect()`，且 target JavaFX app 已以 OmniUI Java agent enabled 模式啟動
- **THEN** client 會回傳可進行 discovery、action、screenshot、OCR 與 vision operation 的 session object

#### Scenario: Fail connection to plain app mode
- **WHEN** 使用者呼叫 `OmniUI.connect()`，但 target JavaFX app 是在未注入 OmniUI Java agent 的情況下啟動
- **THEN** client 會明確回報 connection failure，而不是表現得像有可控制 target 存在

### Requirement: Login-flow script compatibility
系統 SHALL 支援 Phase 1 demo login flow 可完整透過 Python automation script 執行，且 target JavaFX app 使用 agent-enabled launch mode。

#### Scenario: Run complete login script against injected app
- **WHEN** demo JavaFX app 以 agent-enabled mode 啟動，且 script 點擊 username、輸入 credentials、點擊 login button，最後驗證 success text
- **THEN** 整個流程可以成功執行，且 target app source 不需要包含 OmniUI-specific bootstrap code

### Requirement: Python client advanced-control compatibility
系統 SHALL 允許 Python automation scripts 在不暴露 backend-specific transport detail 的前提下，操作受支援的進階 JavaFX demo 場景。

#### Scenario: Run advanced scenario from Python
- **WHEN** 使用者對支援中的進階 JavaFX demo 場景執行 Python script
- **THEN** script 可以透過正常的 OmniUI client API 表達所需互動，並檢視 resulting state

### Requirement: High-level action API
系統 SHALL 提供足以表達支援中的進階 JavaFX demo interaction 的高階 Python methods；若 `click` 與 `type` 不足，則可擴充更高階 action。

#### Scenario: Use selection-oriented action for advanced control
- **WHEN** 某個支援中的進階 JavaFX 場景需要較適合用 selection 或 expansion 表達的互動，而不是 raw click
- **THEN** Python client 會提供可表達該互動的高階 action，而不要求使用者直接操作 transport payload

### Requirement: Python client exposes Locator
系統 SHALL 提供 `Locator` 類別，可透過 `client.locator(**selector)` 取得。Locator 儲存 node selector 並在每次後續呼叫時自動代入，使 automation script 與 Page Object Model 不需重複寫 selector。

#### Scenario: 取得 Locator 並呼叫方法而不重複 selector
- **WHEN** script 呼叫 `loc = client.locator(id="loginBtn")`，再呼叫 `loc.click()`、`loc.verify_text("Login")`、`loc.wait_for_visible()`
- **THEN** 每次呼叫都會自動帶入 `id="loginBtn"` 並轉發至底層 client

#### Scenario: 未傳 selector 時拋出 ValueError
- **WHEN** script 呼叫 `client.locator()`（未傳任何引數）
- **THEN** Python client 立即拋出 `ValueError`

#### Scenario: wait_for_* 無 id= 時拋出 ValueError
- **WHEN** script 以 `text=` 建立 Locator 並呼叫 `loc.wait_for_visible()`
- **THEN** Python client 拋出 `ValueError`，說明需要 `id=`

