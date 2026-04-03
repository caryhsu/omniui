## 1. Java agent — TreeTableView actions

- [ ] 1.1 在 `ReflectiveJavaFxTarget.java` 新增 `select_tree_table_row` case：呼叫 `selectTreeTableRow(node, value, column)`
- [ ] 1.2 新增 `get_tree_table_cell` case：呼叫 `getTreeTableCell(node, rowValue, column)`，回傳 String
- [ ] 1.3 新增 `expand_tree_table_item` case：找到 TreeItem 後呼叫 `safeInvoke(item, "setExpanded", true)`
- [ ] 1.4 新增 `collapse_tree_table_item` case：找到 TreeItem 後呼叫 `safeInvoke(item, "setExpanded", false)`
- [ ] 1.5 新增 `get_tree_table_expanded` case：找到 TreeItem 後呼叫 `safeInvoke(item, "isExpanded")`
- [ ] 1.6 實作 `selectTreeTableRow(node, value, column)` private 方法：遞迴走訪 TreeItem 樹，比對 cell value，選中該列
- [ ] 1.7 實作 `getTreeTableCell(node, rowValue, column)` private 方法：找到對應列後讀取指定欄的值
- [ ] 1.8 實作 `findTreeTableItem(root, value)` private helper：遞迴尋找 TreeItem，展開父節點

## 2. Python engine — new methods

- [ ] 2.1 在 `omniui/core/engine.py` 新增 `select_tree_table_row(value, column=None, **selector)` 方法
- [ ] 2.2 新增 `get_tree_table_cell(row, column, **selector)` 方法，回傳 str
- [ ] 2.3 新增 `expand_tree_table_item(value, **selector)` 方法
- [ ] 2.4 新增 `collapse_tree_table_item(value, **selector)` 方法
- [ ] 2.5 新增 `get_tree_table_expanded(value, **selector)` 方法，回傳 bool

## 3. Demo app — TreeTableView section

- [ ] 3.1 在 `LoginDemoApp.java` 新增 `TreeTableView` / `TreeTableColumn` / `TreeTableRow` import
- [ ] 3.2 新增 `treeTableSection`：建立 `demoTreeTable`，內含 Name / Department 兩欄，及至少兩層 hierarchy（部門 → 員工）
- [ ] 3.3 將 `treeTableSection` 加入 root VBox

## 4. Python demo script

- [ ] 4.1 建立 `demo/python/treetableview_demo.py`：驗證 row 選取、cell 值讀取、展開/收合
- [ ] 4.2 將 `treetableview_demo` 加入 `demo/python/run_all.py`

## 5. Spec zh-TW translations

- [ ] 5.1 建立 `specs/treetableview-automation/spec.zh-TW.md`
- [ ] 5.2 建立 `proposal.zh-TW.md`
- [ ] 5.3 建立 `design.zh-TW.md`
- [ ] 5.4 建立 `tasks.zh-TW.md`

## 6. Validation

- [ ] 6.1 執行 `mvn package -f java-agent/pom.xml`，確認編譯無誤
- [ ] 6.2 執行 `treetableview_demo.py`，確認輸出 `treetableview_demo succeeded ✓`
- [ ] 6.3 git commit + push
