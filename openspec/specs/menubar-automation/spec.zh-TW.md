## Purpose

定義 JavaFX MenuBar 控制項的 automation 行為。

## Requirements

### Requirement: MenuBar top-level menu activation
系統 SHALL 支援透過分派點擊以標籤文字或 `fx:id` 識別的 Menu 項目，開啟 MenuBar 的頂層選單。

#### Scenario: Open top-level menu by label text
- **WHEN** Python client 對含有 MenuBar 的場景呼叫 `open_menu(text="File")`
- **THEN** 系統分派點擊符合的頂層 Menu 項目，並等到選單 popup 可見

#### Scenario: Open top-level menu by fx:id
- **WHEN** Python client 對含有 MenuBar 的場景呼叫 `open_menu(id="fileMenu")`
- **THEN** 系統分派點擊符合 `fx:id` 的 Menu 項目，並等到選單 popup 可見

### Requirement: MenuBar single-level item selection
系統 SHALL 支援在已開啟的頂層 MenuBar 選單中，透過標籤文字或 `fx:id` 選取可見的 MenuItem。

#### Scenario: Click a single-level MenuItem by text in an open menu
- **WHEN** 一個頂層 MenuBar 選單已開啟，且 Python client 呼叫 `click_menu_item(text="Open")`
- **THEN** 系統在可見的選單 popup 中定位符合的 MenuItem 並分派點擊事件

#### Scenario: Click a single-level MenuItem by fx:id in an open menu
- **WHEN** 一個頂層 MenuBar 選單已開啟，且 Python client 呼叫 `click_menu_item(id="openFileItem")`
- **THEN** 系統在可見的選單 popup 中定位符合 `fx:id` 的 MenuItem 並分派點擊事件

### Requirement: MenuBar multi-level submenu navigation
系統 SHALL 支援透過接受從頂層選單標題到最終項目的完整路徑，在 MenuBar 的巢狀子選單中導航，並依序以 hover dispatch 啟動每個層級。

#### Scenario: Navigate a two-level MenuBar path
- **WHEN** Python client 呼叫 `navigate_menu(path=["File", "Export", "CSV"])`
- **THEN** 系統開啟 "File" 選單，hover "Export" 以展開子選單，等子選單 popup 可見後，再分派點擊 "CSV"

#### Scenario: Navigate a three-level MenuBar path
- **WHEN** Python client 呼叫 `navigate_menu(path=["Edit", "Transform", "Case", "Uppercase"])`
- **THEN** 系統依序開啟每個子選單層級，並分派點擊最終的 "Uppercase" 項目

### Requirement: MenuBar menu dismissal
系統 SHALL 支援在不選取任何項目的情況下關閉已開啟的 MenuBar 選單。

#### Scenario: Dismiss open MenuBar menu by pressing Escape
- **WHEN** 一個 MenuBar 選單已開啟，且 Python client 呼叫 `dismiss_menu()`
- **THEN** 系統分派 Escape 按鍵事件，並等到選單 popup 視窗不再可見