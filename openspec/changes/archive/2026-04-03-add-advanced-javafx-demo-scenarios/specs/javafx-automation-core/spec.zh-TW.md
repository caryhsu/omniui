## MODIFIED Requirements

### Requirement: JavaFX node discovery
系統 SHALL 提供 JavaFX discovery capability，從 agent-discovered runtime state 列舉 active scene graph，回傳包含 `fx:id`、node type、text 與 hierarchy path 的 node records，且要涵蓋 popup-backed、hierarchical 與 virtualized controls 的 visible state。

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
