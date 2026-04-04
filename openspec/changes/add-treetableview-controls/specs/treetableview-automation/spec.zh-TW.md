## 新增需求

### 需求：TreeTableView 依 cell 值選取列
系統**應**支援透過比對 cell 值字串選取 `TreeTableView` 中的列。選填的 `column` 參數可將搜尋範圍縮小至特定欄（依標題文字比對）。

#### 情境：以任意欄比對選取列
- **當** automation 用戶端呼叫 `select_tree_table_row(id="<id>", value="X")`，且有列的某欄含有 "X"
- **則** 該列在 TreeTableView 的 SelectionModel 中被選取

#### 情境：以欄提示選取列
- **當** automation 用戶端呼叫 `select_tree_table_row(id="<id>", value="X", column="Name")`，且有列的 Name 欄等於 "X"
- **則** 該列被選取

#### 情境：找不到值時失敗
- **當** automation 用戶端呼叫 `select_tree_table_row`，指定的值不存在於任何列
- **則** 動作回傳失敗，`reason="select_not_supported"`

### 需求：讀取 TreeTableView cell 值
系統**應**支援透過指定列識別值與欄標題讀取 `TreeTableView` 特定 cell 的字串值。

#### 情境：讀取 cell 值
- **當** automation 用戶端呼叫 `get_tree_table_cell(id="<id>", row="X", column="Age")`，且存在以 "X" 識別的列
- **則** 系統回傳該列 "Age" 欄的字串值

#### 情境：列不存在時失敗
- **當** automation 用戶端呼叫 `get_tree_table_cell`，指定的列值不在樹中
- **則** 動作回傳失敗，`reason="selector_not_found"`

### 需求：TreeTableView 樹節點展開與收合
系統**應**支援透過對符合的 `TreeItem` 呼叫 `setExpanded(boolean)` 來展開或收合其節點。

#### 情境：展開樹節點
- **當** automation 用戶端呼叫 `expand_tree_table_item(id="<id>", value="X")`，對已收合的 TreeItem
- **則** `treeItem.setExpanded(true)` 被呼叫，該項目的子節點變為可見

#### 情境：收合樹節點
- **當** automation 用戶端呼叫 `collapse_tree_table_item(id="<id>", value="X")`，對已展開的 TreeItem
- **則** `treeItem.setExpanded(false)` 被呼叫，該項目的子節點隱藏

### 需求：讀取 TreeTableView 樹節點展開狀態
系統**應**支援讀取 `TreeTableView` 中 `TreeItem` 的展開狀態。

#### 情境：讀取樹節點展開狀態
- **當** automation 用戶端呼叫 `get_tree_table_expanded(id="<id>", value="X")`
- **則** 若符合的 TreeItem 已展開則回傳 `true`，已收合則回傳 `false`
