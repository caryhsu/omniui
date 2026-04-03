## Purpose

定義 JavaFX ContextMenu 控制項的 automation 行為。

## Requirements

### Requirement: ContextMenu trigger
系統 SHALL 支援透過向 JavaFX runtime 分派右鍵點擊事件，在目標 JavaFX node 上觸發 ContextMenu，且不需要 OS 層級的滑鼠移動。

#### Scenario: Open ContextMenu via right-click on target node
- **WHEN** Python client 對已登錄 ContextMenu 的 node 呼叫 `right_click(selector)`
- **THEN** 系統對解析後的 node 分派右鍵點擊事件，並等到 ContextMenu popup 視窗可見後才回傳

#### Scenario: Report error when right-click target has no ContextMenu
- **WHEN** Python client 對未登錄 ContextMenu 的 node 呼叫 `right_click(selector)`，且 popup 在設定的 timeout 內未出現
- **THEN** 系統回報 timeout 錯誤，不回傳部分結果

### Requirement: ContextMenu single-level item selection
系統 SHALL 支援在 ContextMenu 開啟後，透過比對標籤文字或 `fx:id` 選取可見的 ContextMenu 項目。

#### Scenario: Click a single-level MenuItem by label text
- **WHEN** ContextMenu 已開啟，且 Python client 呼叫 `click_menu_item(text="Delete")`
- **THEN** 系統在可見的 ContextMenu overlay 中定位符合的 MenuItem 並分派點擊事件

#### Scenario: Click a single-level MenuItem by fx:id
- **WHEN** ContextMenu 已開啟，且 Python client 呼叫 `click_menu_item(id="deleteItem")`
- **THEN** 系統在可見的 ContextMenu overlay 中定位符合 `fx:id` 的 MenuItem 並分派點擊事件

### Requirement: ContextMenu multi-level item navigation
系統 SHALL 支援透過接受項目標籤路徑，在含有巢狀子選單的 ContextMenu 中導航，並依序以 hover dispatch 啟動每個中間層級，最後點擊終端項目。

#### Scenario: Navigate and click a two-level submenu item
- **WHEN** ContextMenu 已開啟，且 Python client 呼叫 `click_menu_item(path=["Edit", "Copy As"])`
- **THEN** 系統 hover "Edit" 項目以展開子選單，等子選單 popup 可見後，再分派點擊 "Copy As"

#### Scenario: Navigate and click a three-level submenu item
- **WHEN** ContextMenu 已開啟，且 Python client 呼叫 `click_menu_item(path=["Format", "Text", "Bold"])`
- **THEN** 系統依序 hover 每個中間項目以展開下一層，並分派點擊最終的 "Bold" 項目

### Requirement: ContextMenu dismissal
系統 SHALL 支援在不選取任何項目的情況下關閉已開啟的 ContextMenu。

#### Scenario: Dismiss ContextMenu by pressing Escape
- **WHEN** ContextMenu 已開啟，且 Python client 呼叫 `dismiss_menu()`
- **THEN** 系統分派 Escape 按鍵事件，並等到 ContextMenu popup 視窗不再可見