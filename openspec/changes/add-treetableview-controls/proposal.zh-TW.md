## 動機

`TreeTableView` 結合了左欄的樹狀層次結構與右側多欄的表格資料顯示，廣泛應用於 JavaFX 應用程式中的檔案瀏覽器、組織圖及巢狀資料表格。目前 automation 框架已支援 `TreeView`（單欄階層）與 `TableView`（平面多欄列），但尚未支援 `TreeTableView`，在真實應用程式的 automation 中存在明顯缺口。

## 變更內容

- 在 Java agent 新增 `select_tree_table_row` action，依 cell 值（選填欄位）選取 `TreeTableView` 中的列
- 新增 `get_tree_table_cell` action，讀取特定列欄的 cell 值
- 新增 `expand_tree_table_item` / `collapse_tree_table_item` action，控制樹節點展開
- 新增 `get_tree_table_expanded` action，讀取樹列的展開狀態
- 在 Python engine 公開以上四個 action 的對應方法
- 在 `LoginDemoApp.java` 新增 `TreeTableView` demo 區塊，含範例階層資料
- 新增 `treetableview_demo.py` Python demo script

## 能力

### 新增能力
- `treetableview-automation`：定義 JavaFX TreeTableView 的 automation 行為，包含列選取、cell 值讀取、樹節點展開/收合與展開狀態讀取

### 修改能力
- `javafx-automation-core`：新增 TreeTableView 為受支援的 automation 目標，含五個對應 action
- `advanced-javafx-demo-scenarios`：新增 TreeTableView demo 區塊與 Python script 場景

## 影響範圍

- `java-agent/.../ReflectiveJavaFxTarget.java`：新增 action case 與 helper 方法
- `omniui/core/engine.py`：新增五個 public 方法
- `demo/javafx-login-app/.../LoginDemoApp.java`：新增 TreeTableView demo 區塊
- `demo/python/`：新增 `treetableview_demo.py`，更新 `run_all.py`
