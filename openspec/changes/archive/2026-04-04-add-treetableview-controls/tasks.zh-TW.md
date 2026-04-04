## 1. Java agent — TreeTableView actions

- [x] 1.1 在 `ReflectiveJavaFxTarget.java` 新增 `select_tree_table_row` case：呼叫 `selectTreeTableRow(node, value, column)`
- [x] 1.2 新增 `get_tree_table_cell` case：呼叫 `getTreeTableCell(node, rowValue, column)`，回傳 String
- [x] 1.3 新增 `expand_tree_table_item` case：找到 TreeItem 後呼叫 `safeInvoke(item, "setExpanded", true)`
- [x] 1.4 新增 `collapse_tree_table_item` case：找到 TreeItem 後呼叫 `safeInvoke(item, "setExpanded", false)`
- [x] 1.5 新增 `get_tree_table_expanded` case：找到 TreeItem 後呼叫 `safeInvoke(item, "isExpanded")`
- [x] 1.6 實作 `selectTreeTableRow(node, value, column)` private 方法：遞迴走訪 TreeItem 樹，比對 cell value，選中該列
- [x] 1.7 實作 `getTreeTableCell(node, rowValue, column)` private 方法：找到對應列後讀取指定欄的值
- [x] 1.8 實作 `findTreeTableItem(root, value)` private helper：遞迴尋找 TreeItem，展開父節點

## 2. Python engine — 新增方法

- [x] 2.1 在 `omniui/core/engine.py` 新增 `select_tree_table_row(value, column=None, **selector)` 方法
- [x] 2.2 新增 `get_tree_table_cell(row, column, **selector)` 方法，回傳 str
- [x] 2.3 新增 `expand_tree_table_item(value, **selector)` 方法
- [x] 2.4 新增 `collapse_tree_table_item(value, **selector)` 方法
- [x] 2.5 新增 `get_tree_table_expanded(value, **selector)` 方法，回傳 bool

## 3. Demo app — 新增 UI 區塊

- [x] 3.1 在 `LoginDemoApp.java` 新增 `TreeTableView` / `TreeTableColumn` / `TreeTableRow` import
- [x] 3.2 新增 `treeTableSection`：建立 `demoTreeTable`，內含 Name / Department 兩欄，及至少兩層 hierarchy（部門 → 員工）
- [x] 3.3 將 `treeTableSection` 加入 root VBox

## 4. Python demo script

- [x] 4.1 建立 `demo/python/treetableview_demo.py`：驗證 row 選取、cell 值讀取、展開/收合
- [x] 4.2 將 `treetableview_demo` 加入 `demo/python/run_all.py`

## 5. 規格繁體中文翻譯

- [x] 5.1 建立 `specs/treetableview-automation/spec.zh-TW.md`
- [x] 5.2 建立 `proposal.zh-TW.md`
- [x] 5.3 建立 `design.zh-TW.md`
- [x] 5.4 建立 `tasks.zh-TW.md`

## 6. 驗證

- [ ] 6.1 執行 `mvn package -f java-agent/pom.xml`，確認編譯無誤
- [ ] 6.2 執行 `treetableview_demo.py`，確認輸出 `treetableview_demo succeeded ✓`
- [ ] 6.3 git commit + push
