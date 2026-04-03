## MODIFIED Requirements

### Requirement: Java agent connectivity
系統 SHALL 提供可注入至本機受支援 JavaFX JVM 的 Java agent，並在不修改 target app source 的前提下，對 Python automation engine 暴露 node discovery 與 action execution endpoints。

#### Scenario: Connect Python client to injected JavaFX target
- **WHEN** 一個受支援的 JavaFX app 在本機以 OmniUI Java agent 啟動
- **THEN** Python client 可以建立 session，並透過該 agent 查詢 nodes 與執行 actions

#### Scenario: Fail clearly when app is not agent-enabled
- **WHEN** JavaFX app 在未注入 OmniUI Java agent 的情況下啟動
- **THEN** Python client 無法建立 session，且會收到清楚的 connection failure，而不是默默綁到 built-in demo target

### Requirement: JavaFX node discovery
系統 SHALL 從 agent 發現的 live JavaFX runtime state 列舉 active scene graph，並回傳包含 `fx:id`、node type、text content、hierarchy path 等資料的 node records。

#### Scenario: Enumerate visible login form nodes from injected agent
- **WHEN** Python client 對 agent-enabled JavaFX app 呼叫 `get_nodes()`
- **THEN** 系統會從 live JavaFX runtime 回傳 node records，而不要求 app 主動把 stage 註冊給 OmniUI
