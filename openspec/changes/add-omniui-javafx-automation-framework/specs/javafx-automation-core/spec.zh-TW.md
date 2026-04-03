## ADDED Requirements

### Requirement: JavaFX node discovery
系統必須提供 JavaFX discovery capability，能列舉 target application 的 active scene graph，並回傳 node record，其中在可用時包含 `fx:id`、node type、text content 與 hierarchy path。

#### Scenario: 列舉可見的 login form node
- **WHEN** Python client 對已連線的 JavaFX application 呼叫 `get_nodes()`
- **THEN** 系統回傳一組 node record，其中包含可見的 login form control，以及其可取得的 `fx:id`、node type、text content 與 hierarchy path

### Requirement: Priority-based selector resolution
系統必須依照以下順序 resolve JavaFX action 的 selector：精確 `fx:id`、使用 node type 與 text 的 structural match、OCR text match、vision template match。

#### Scenario: 優先以 fx:id resolve button
- **WHEN** script 呼叫 `click(id="loginButton")`，且存在 `fx:id="loginButton"` 的 JavaFX node
- **THEN** 系統必須透過 JavaFX adapter resolve target，且不得先啟動 OCR 或 vision matching

#### Scenario: fx:id 不存在時改用 type 與 text
- **WHEN** script 呼叫 `click(text="Login", type="Button")`，且未提供可匹配的 `fx:id`
- **THEN** 系統必須 resolve 到 type 與可見 text 都符合 selector 的 JavaFX button

### Requirement: Direct JavaFX interaction
系統必須對 JavaFX-resolved node 以 runtime event dispatch 或等效的 node-level interaction 執行受支援 action，不得依賴 screen coordinates。

#### Scenario: 不使用滑鼠座標點擊 resolved JavaFX button
- **WHEN** click action 已 resolve 到 JavaFX node
- **THEN** 系統必須透過 JavaFX runtime 與 application thread 觸發 click，而不是要求 OS-level pointer movement

### Requirement: JavaFX action surface
系統必須透過 Python API 支援對 JavaFX-resolved element 執行 `click`、`type`、`get_text` 與 `verify_text`。

#### Scenario: 透過 JavaFX selector 執行 login flow
- **WHEN** script 對 username、password、login button 與 status element 發出 click、type 與 verify operation，且這些 element 都由 JavaFX resolve
- **THEN** 系統必須完成整個流程並回報預期的 status text，且不需要 fallback resolution

### Requirement: Java agent connectivity
系統必須提供 Java agent，可 attach 到受支援的本機 JVM，該 JVM 內執行 JavaFX application，並向 Python automation engine 暴露 node discovery 與 action execution endpoint。

#### Scenario: Python client 透過本機 agent 連線 JavaFX target
- **WHEN** target JavaFX application 執行於本機，且符合 Phase 1 attach model
- **THEN** Python client 必須能建立與本機 Java agent 的 session，並透過它查詢 node 與執行受支援 action

### Requirement: Action observability
系統必須對每個已執行 action 回報 diagnostics，至少包含 requested selector、resolution tier、使用到的 fallback tier，以及 OCR / vision path 的 confidence score（若適用）。

#### Scenario: 記錄 structural action diagnostics
- **WHEN** script 執行一個透過 JavaFX structure resolve 的 click
- **THEN** action result 必須包含原始 selector、`javafx` resolution tier，並標示未使用 OCR 或 vision confidence score
