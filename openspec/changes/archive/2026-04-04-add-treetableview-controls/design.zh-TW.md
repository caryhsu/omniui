## 背景

`TreeTableView<S>` 是 JavaFX 控制項，結合左欄樹狀結構與右側資料欄。每一列是一個 `TreeItem<S>`，每一欄是一個 `TreeTableColumn<S, T>`。存取 cell 值需要遞迴走訪 `TreeItem` 樹，讀取特定欄資料則需要迭代 `getColumns()`。

目前框架支援 `TreeView`（階層、每節點單一值）與 `TableView`（平面列、多欄）。兩者皆不涵蓋 `TreeTableView`，後者在表格模型上加入了垂直樹軸。

## 目標／非目標

**目標：**
- 支援依 cell 值選取 `TreeTableView` 列（任意欄或指定欄）
- 支援依列索引 + 欄名稱讀取 `TreeTableView` cell 值
- 支援展開與收合 `TreeTableView` 內的 `TreeItem` 節點
- 支援讀取 `TreeItem` 列的展開狀態
- 提供 Python engine 方法、demo 區塊及 script

**非目標：**
- 多選（每次 action 只選取單列）
- 就地編輯 cell（`TreeTableCell` commit）
- 透過欄標題排序或篩選
- 拖放列重新排序

## 決策

### 決策 1：action 命名與映射
| Action | Java 方法 | 備註 |
|---|---|---|
| `select_tree_table_row` | `selectTreeTableRow(node, value, column)` | 對應 `selectTableRow` |
| `get_tree_table_cell` | `getTreeTableCell(node, rowValue, column)` | 依列識別讀取 cell |
| `expand_tree_table_item` | `safeInvoke(treeItem, "setExpanded", true)` | 同 Accordion 模式 |
| `collapse_tree_table_item` | `safeInvoke(treeItem, "setExpanded", false)` | 同 Accordion 模式 |
| `get_tree_table_expanded` | `safeInvoke(treeItem, "isExpanded")` | 同 Accordion 模式 |

### 決策 2：列查找策略
遞迴走訪 `TreeItem` 樹，使用 `getChildren()`。對每個 `TreeItem`，透過欄的 `getCellObservableValue(treeItem)` 取得 cell 值。若未指定欄則比對所有欄。

### 決策 3：`get_tree_table_cell` selector
Action 接受 `TreeTableView` 節點（依 id）、`row` 字串（以第一欄值比對）及 `column` 字串（欄標題文字）。回傳 cell 的字串值。

### 決策 4：展開/收合以 TreeTableView 節點為 selector，再定位 TreeItem
`expand_tree_table_item` action 的 selector 解析 `TreeTableView` 節點，payload 中的 `value` 用於識別要展開/收合的 `TreeItem`。

## 風險／取捨

- **[風險] TreeItem 泛型型別** → `getValue()` 回傳 `Object`；依賴 `toString()` 比對，與 `TreeView` select 行為一致
- **[風險] getColumns() 可能包含巢狀欄群組** → Phase 1 只支援平面（非群組）欄；巢狀欄群組為非目標
- **[風險] Cell value factory 可能不是簡單字串** → 使用 `TreeTableColumn.getCellObservableValue(item)` 透過反射取得顯示值
