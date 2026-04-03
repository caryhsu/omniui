## Purpose

定義 JavaFX TabPane 控制項的 automation 行為。

## Requirements

### Requirement: TabPane tab listing
系統 SHALL 支援列出 `TabPane` node 中的所有 tab，並回傳每個 tab 的標題文字與停用狀態。

#### Scenario: List tabs of a TabPane
- **WHEN** automation client 呼叫 `get_tabs(id="<tabPaneId>")`
- **THEN** 系統回傳一個物件清單，每個物件包含 `text`（tab 標題）與 `disabled`（布林值），對應 TabPane 中的每個 tab

### Requirement: TabPane tab selection
系統 SHALL 支援透過標題文字選取 `TabPane` 中的 tab。

#### Scenario: Select tab by title
- **WHEN** automation client 呼叫 `select_tab(id="<tabPaneId>", tab="<title>")`，且存在符合該標題的 tab
- **THEN** TabPane 的 selection model 選取該 tab，使其成為作用中的 tab

#### Scenario: Fail when tab title not found
- **WHEN** automation client 以 TabPane 中不存在的標題呼叫 `select_tab`
- **THEN** 系統回傳失敗結果，reason 為 `tab_not_found`