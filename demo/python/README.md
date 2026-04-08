# OmniUI Python Demo Scripts

Demo scripts are organized per app under `demo/python/`:

| Package | Target App | Port |
|---------|-----------|------|
| `core/` | core-app | 48100 |
| `input/` | input-app | 48101 |
| `advanced/` | advanced-app | 48102 |
| `drag/` | drag-app | 48103 |
| `progress/` | progress-app | 48104 |
| `image/` | image-app | 48105 |
| `color/` | color-app | 48106 |
| `todo/` | todo-app | 48107 |
| `settings/` | settings-app | 48112 |
| `dynamicfxml/` | dynamic-fxml-app | 48110 |
| `explorer/` | explorer-app | 48111 |
| `usersearch/` | user-search-app | 48109 |

## Run all demos (auto-launch)

```bash
python demo/python/run_all.py
```

Launches each JVM with the agent, runs all demo scripts in sequence, then shuts down each app.

## Run a specific demo

Each package has a `*_demo.py` entry point that connects to the app on its preferred port.

```bash
# App must be running in with-agent mode first
python demo/python/settings/settings_demo.py        # port 48112
python demo/python/dynamicfxml/dynamic_fxml_demo.py # port 48110
python demo/python/explorer/explorer_demo.py         # port 48111
python demo/python/usersearch/user_search_demo.py   # port 48109
python demo/python/drag/drag_listview_demo.py        # port 48103
python demo/python/todo/todo_demo.py                 # port 48107
```

## Benchmark

```bash
python demo/python/run_benchmark.py
```
