# OmniUI Python Demo Scripts

這些 script 是目前 Python client 的小型可執行範例。

## 腳本列表

- [run_all.py](run_all.py)
- [discover_nodes.py](discover_nodes.py)
- [discover_advanced_controls.py](discover_advanced_controls.py)
- [select_combo_role.py](select_combo_role.py)
- [select_list_item.py](select_list_item.py)
- [select_tree_item.py](select_tree_item.py)
- [select_table_row.py](select_table_row.py)
- [login_direct.py](login_direct.py)
- [login_with_fallback.py](login_with_fallback.py)
- [recorder_preview.py](recorder_preview.py)
- [run_benchmark.py](run_benchmark.py)

所有 script 都假設參考 JavaFX demo app 已先啟動。

## 進階元件 discovery

```bash
python demo/python/discover_advanced_controls.py
```

列出目前可見的進階 JavaFX demo 元件，例如 combo box、list view、tree view、table view 與 grid section。

`run_all.py` 現在也會在互動 demo 前先執行這一步。

## 進階互動 demo

```bash
python demo/python/select_combo_role.py
python demo/python/select_list_item.py
python demo/python/select_tree_item.py
python demo/python/select_table_row.py
```

這些 script 會直接操作進階 JavaFX 控制項的 selection，並驗證對應的 status label。
