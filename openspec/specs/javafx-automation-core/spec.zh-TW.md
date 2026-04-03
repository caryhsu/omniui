## Purpose

定義 OmniUI 透過 Java agent 暴露的核心 JavaFX automation 行為。

## Requirements

### Requirement: Java agent connectivity
系統 SHALL 提供可注入本機受支援 JavaFX JVM 的 Java agent，並在不修改 target app source 的前提下，對 Python automation engine 暴露 node discovery 與 action execution endpoint。

#### Scenario: Connect Python client to injected JavaFX target
- **WHEN** 一個受支援的 JavaFX app 在本機以 OmniUI Java agent 啟動
- **THEN** Python client 可以建立 session，並透過 agent 查詢 nodes 與執行 actions

#### Scenario: Fail clearly when app is not agent-enabled
- **WHEN** JavaFX app 未注入 OmniUI Java agent 即啟動
- **THEN** Python client 無法建立 session，且會收到清楚的 connection failure，而不是默默綁到 built-in demo target

### Requirement: JavaFX node discovery
系統 SHALL 提供 JavaFX discovery capability，從 agent-discovered runtime state 列舉 active scene graph，回傳包含 `fx:id`、node type、text 與 hierarchy path 的 node records，且要涵蓋 popup-backed、hierarchical 與 virtualized controls 的 visible state。

#### Scenario: Enumerate visible login form nodes from injected agent
- **WHEN** Python client 對 agent-enabled JavaFX app 呼叫 `get_nodes()`
- **THEN** 系統會回傳從 live JavaFX runtime 發現的 node records，而不需要 app 主動向 OmniUI 註冊 stage

#### Scenario: Discover visible advanced-control state
- **WHEN** Python client 在進階 JavaFX demo 場景可見時呼叫 `get_nodes()`
- **THEN** 系統會回傳目前 materialized 的 control state，包括可見的 `ComboBox`、`ListView`、`TreeView` 與 `TableView` 結構

### Requirement: Advanced JavaFX interaction coverage
系統 SHALL 支援 reference demos 所需的代表性進階 JavaFX control interaction，包括 selection-oriented 與 hierarchy-oriented operations。

#### Scenario: Select visible list item in advanced demo
- **WHEN** 某個支援中的進階 demo 場景需要在 selection-based JavaFX control 中選取可見項目
- **THEN** 系統可透過 JavaFX runtime 執行該互動，並驗證 resulting selected value 或 state

#### Scenario: Expand or select visible tree item in advanced demo
- **WHEN** 某個支援中的進階 demo 場景需要展開或選取可見 tree node
- **THEN** 系統可驅動所需 JavaFX interaction，並觀察 resulting visible state
