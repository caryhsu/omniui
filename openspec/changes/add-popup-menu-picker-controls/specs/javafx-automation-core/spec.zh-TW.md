## ADDED Requirements

### Requirement: Overlay window node discovery
系統必須在 overlay 可見時，除了主要 scene graph 之外，還列舉所有開啟中 overlay window 的 node——包含 `PopupWindow` instance（ContextMenu、MenuBar popup、DatePicker popup）以及 Dialog 或 Alert 的 `Stage` instance。

#### Scenario: 偵測可見 ContextMenu overlay 中的 node
- **WHEN** ContextMenu 已開啟，Python client 呼叫 `get_nodes()`
- **THEN** 系統除了主要 scene node 外，還回傳 overlay window 中 ContextMenu 的 `MenuItem` node record

#### Scenario: 偵測已開啟 Dialog 中的 node
- **WHEN** modal Dialog 可見，Python client 呼叫 `get_nodes()`
- **THEN** 系統回傳 `DialogPane` scene 中的 node record，包含 title node、content node 與 button node

#### Scenario: 偵測已開啟 DatePicker popup 中的 node
- **WHEN** DatePicker calendar popup 已開啟，Python client 呼叫 `get_nodes()`
- **THEN** 系統除了主要 scene node 外，還回傳 calendar popup 的日期格與導航按鈕的 node record

### Requirement: Wait-for-overlay readiness
系統必須在 popup 觸發動作後，提供輪詢 open window 清單的機制，直到符合預期類型的新 overlay window 出現，再將控制權回傳給 Python client。

#### Scenario: 右鍵後等待 ContextMenu popup 出現
- **WHEN** Python client dispatch 觸發 ContextMenu 的 right-click
- **THEN** 系統等到 ContextMenu `PopupWindow` 出現在 open window 清單後，才回報成功

#### Scenario: Overlay 未出現時回報 timeout
- **WHEN** Python client 對無 ContextMenu 的 node dispatch right-click，並等待 overlay popup 出現
- **THEN** 系統在設定的等待時間後回報 timeout error，不得回傳部分結果
