## ADDED Requirements

### Requirement: ContextMenu trigger
系統必須支援對目標 JavaFX node 觸發 ContextMenu，透過 JavaFX runtime dispatch right-click event，不得使用 OS-level pointer movement。

#### Scenario: 對目標 node 右鍵開啟 ContextMenu
- **WHEN** Python client 對已註冊 ContextMenu 的 node 呼叫 `right_click(selector)`
- **THEN** 系統對 resolved node dispatch right-click event，並等待 ContextMenu popup window 可見後才回傳

#### Scenario: 右鍵目標 node 無 ContextMenu 時回報錯誤
- **WHEN** Python client 對未註冊 ContextMenu 的 node 呼叫 `right_click(selector)`，且 popup 在設定的 timeout 內未出現
- **THEN** 系統回報 timeout error，不得回傳部分結果

### Requirement: ContextMenu 單層 item 選取
ContextMenu 開啟後，系統必須支援以 label text 或 `fx:id` 匹配並選取可見的 ContextMenu item。

#### Scenario: 以 label text 點擊單層 MenuItem
- **WHEN** ContextMenu 已開啟，Python client 呼叫 `click_menu_item(text="Delete")`
- **THEN** 系統在可見的 ContextMenu overlay 中找到匹配的 MenuItem，並對它 dispatch click event

#### Scenario: 以 fx:id 點擊單層 MenuItem
- **WHEN** ContextMenu 已開啟，Python client 呼叫 `click_menu_item(id="deleteItem")`
- **THEN** 系統在可見的 ContextMenu overlay 中找到 `fx:id` 匹配的 MenuItem，並對它 dispatch click event

### Requirement: ContextMenu 多層 item 導航
系統必須支援透過接受 item label path，逐步以 hover dispatch 啟動每個中間層，最後點擊目標 item，以導航含巢狀 submenu 的 ContextMenu。

#### Scenario: 導航並點擊二層 submenu item
- **WHEN** ContextMenu 已開啟，Python client 呼叫 `click_menu_item(path=["Edit", "Copy As"])`
- **THEN** 系統 hover "Edit" item 展開其 submenu，等待 submenu popup 可見後，再對 "Copy As" dispatch click event

#### Scenario: 導航並點擊三層 submenu item
- **WHEN** ContextMenu 已開啟，Python client 呼叫 `click_menu_item(path=["Format", "Text", "Bold"])`
- **THEN** 系統依序 hover 每個中間 item 以展開下一層，並對最終的 "Bold" item dispatch click event

### Requirement: ContextMenu 關閉
系統必須支援在不選取任何 item 的情況下關閉已開啟的 ContextMenu。

#### Scenario: 按 Escape 關閉 ContextMenu
- **WHEN** ContextMenu 已開啟，Python client 呼叫 `dismiss_menu()`
- **THEN** 系統 dispatch Escape key event，並等待 ContextMenu popup window 不再可見
