## 新增需求

### 需求：TitledPane 展開與收合
系統**應**支援透過將 `expanded` 屬性設為 `true` 或 `false`，展開或收合 `TitledPane` 節點。

#### 情境：展開已收合的 TitledPane
- **當** automation 用戶端對已收合的 TitledPane 呼叫 `expand_pane(id="<titledPaneId>")`
- **則** TitledPane 的 `expanded` 屬性變為 `true`，其內容區域變為可見

#### 情境：收合已展開的 TitledPane
- **當** automation 用戶端對已展開的 TitledPane 呼叫 `collapse_pane(id="<titledPaneId>")`
- **則** TitledPane 的 `expanded` 屬性變為 `false`，其內容區域隱藏

### 需求：讀取 TitledPane 展開狀態
系統**應**支援讀取 `TitledPane` 節點目前的展開狀態。

#### 情境：讀取已展開的 TitledPane 狀態
- **當** automation 用戶端對已展開的 TitledPane 呼叫 `get_expanded(id="<titledPaneId>")`
- **則** 系統回傳 `true`

#### 情境：讀取已收合的 TitledPane 狀態
- **當** automation 用戶端對已收合的 TitledPane 呼叫 `get_expanded(id="<titledPaneId>")`
- **則** 系統回傳 `false`

### 需求：Accordion 互斥展開
系統**應**遵守 Accordion 的單一面板展開行為：當 Accordion 內的某個 TitledPane 被展開時，JavaFX 會自動收合同一 Accordion 中的其他所有 TitledPane。

#### 情境：展開一個面板自動收合其他面板
- **當** automation 用戶端對 Accordion 內的 TitledPane 呼叫 `expand_pane`
- **則** 該 TitledPane 變為展開，同一 Accordion 中的所有其他 TitledPane 自動收合
