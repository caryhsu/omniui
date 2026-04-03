# OmniUI Python Demo Scripts

These scripts are small runnable examples for the current Python client.

## Scripts

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

All scripts assume the reference JavaFX demo app is already running.

## Advanced demo discovery

```bash
python demo/python/discover_advanced_controls.py
```

Lists the currently visible advanced JavaFX demo controls such as the combo box, list view, tree view, table view, and grid section.

`run_all.py` also includes this advanced-control discovery step before the interaction demos.

## Advanced interaction demos

```bash
python demo/python/select_combo_role.py
python demo/python/select_list_item.py
python demo/python/select_tree_item.py
python demo/python/select_table_row.py
```

These scripts exercise direct JavaFX selection behavior for the advanced demo controls and verify the corresponding status labels.
