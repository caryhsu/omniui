## ADDED Requirements

### Requirement: TabPane 分頁列舉
系統 SHALL 支援列舉 `TabPane` 節點中所有分頁，回傳每個分頁的標題文字與停用狀態。

#### Scenario: 列舉 TabPane 所有分頁
- **WHEN** 自動化客戶端呼叫 `get_tabs(id="<tabPaneId>")`
- **THEN** 系統回傳清單，每個項目包含 `text`（分頁標題）與 `disabled`（布林）

### Requirement: TabPane 分頁選取
系統 SHALL 支援以分頁標題文字選取 `TabPane` 中的指定分頁。

#### Scenario: 以標題選取分頁
- **WHEN** 自動化客戶端呼叫 `select_tab(id="<tabPaneId>", tab="<title>")`，
  且存在該標題的分頁
- **THEN** TabPane 的 selection model 選取該分頁，使其成為當前分頁

#### Scenario: 找不到分頁標題時回傳失敗
- **WHEN** 自動化客戶端呼叫 `select_tab`，指定標題在 TabPane 中不存在
- **THEN** 系統回傳失敗，reason 為 `tab_not_found`
