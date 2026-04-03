## ADDED Requirements

### Requirement: MenuBar 頂層選單啟動
系統必須支援對 MenuBar 的頂層 Menu item 以 label text 或 `fx:id` 識別，並透過 click dispatch 開啟它。

#### Scenario: 以 label text 開啟頂層選單
- **WHEN** Python client 在含有 MenuBar 的 scene 上呼叫 `open_menu(text="File")`
- **THEN** 系統對匹配的頂層 Menu item dispatch click，並等待選單 popup 可見

#### Scenario: 以 fx:id 開啟頂層選單
- **WHEN** Python client 在含有 MenuBar 的 scene 上呼叫 `open_menu(id="fileMenu")`
- **THEN** 系統對 `fx:id` 匹配的 Menu item dispatch click，並等待選單 popup 可見

### Requirement: MenuBar 單層 item 選取
頂層 MenuBar 選單開啟後，系統必須支援以 label text 或 `fx:id` 選取可見的 MenuItem。

#### Scenario: 以 text 點擊開啟選單中的單層 MenuItem
- **WHEN** 頂層 MenuBar 選單已開啟，Python client 呼叫 `click_menu_item(text="Open")`
- **THEN** 系統在可見的選單 popup 中找到匹配的 MenuItem，並對它 dispatch click event

#### Scenario: 以 fx:id 點擊開啟選單中的單層 MenuItem
- **WHEN** 頂層 MenuBar 選單已開啟，Python client 呼叫 `click_menu_item(id="openFileItem")`
- **THEN** 系統在可見的選單 popup 中找到 `fx:id` 匹配的 MenuItem，並對它 dispatch click event

### Requirement: MenuBar 多層 submenu 導航
系統必須支援透過接受從頂層 menu header 到最終 item 的完整 path，並逐步以 hover dispatch 啟動每一層，以導航 MenuBar 中巢狀的 submenu。

#### Scenario: 導航二層 MenuBar path
- **WHEN** Python client 呼叫 `navigate_menu(path=["File", "Export", "CSV"])`
- **THEN** 系統開啟 "File" 選單，hover "Export" 展開其 submenu，等待 submenu popup 可見後，再對 "CSV" dispatch click

#### Scenario: 導航三層 MenuBar path
- **WHEN** Python client 呼叫 `navigate_menu(path=["Edit", "Transform", "Case", "Uppercase"])`
- **THEN** 系統依序開啟每個 submenu 層，並對最終的 "Uppercase" item dispatch click

### Requirement: MenuBar 選單關閉
系統必須支援在不選取任何 item 的情況下關閉已開啟的 MenuBar 選單。

#### Scenario: 按 Escape 關閉開啟中的 MenuBar 選單
- **WHEN** MenuBar 選單已開啟，Python client 呼叫 `dismiss_menu()`
- **THEN** 系統 dispatch Escape key event，並等待選單 popup window 不再可見
