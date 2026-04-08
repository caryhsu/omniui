# OmniUI Demos

This directory collects runnable demos for OmniUI.

## JavaFX target apps

Demo apps are available under \demo/java/\:

| App | Port | Contents |
|-----|------|---------|
| [core-app](java/core-app/) | 48100 | Login flow, ComboBox, ListView, TreeView, TableView, GridPane |
| [input-app](java/input-app/) | 48101 | TextArea, PasswordField, Hyperlink, CheckBox, ChoiceBox, RadioButton, Slider, Spinner, ColorPicker, DatePicker |
| [advanced-app](java/advanced-app/) | 48102 | ContextMenu, MenuBar, Dialog, Alert, TabPane, Accordion, TreeTableView, SplitPane, ProgressBar, NodeState, ScrollPane, Tooltip |
| [drag-app](java/drag-app/) | 48103 | Drag &amp; Drop (label items → drop target) |
| [progress-app](java/progress-app/) | 48104 | ProgressBar, ProgressIndicator, async jobs |
| [image-app](java/image-app/) | 48105 | ImageView, image switching |
| [color-app](java/color-app/) | 48106 | ColorPicker, color result label |
| [todo-app](java/todo-app/) | 48107 | TableView (editable), dialog, task management |
| [login-app](java/login-app/) | 48108 | Login form with success/failure |
| [user-search-app](java/user-search-app/) | 48109 | TableView with pagination, live search/filter |
| [dynamic-fxml-app](java/dynamic-fxml-app/) | 48110 | FXML view loading, Form/Dashboard/List views |
| [explorer-app](java/explorer-app/) | 48111 | TreeView file explorer, TableView file listing |
| [settings-app](java/settings-app/) | 48112 | TabPane settings form with validation |

Start the app you want to test:

\\ash
demo/java/core-app/run-dev-with-agent.bat
demo/java/settings-app/run-dev-with-agent.bat
# ... etc.
\
Each app supports four launch modes: un-dev-with-agent\, un-dev-plain\, un-with-agent\ (jlink), un-plain\ (jlink).

## Python demos

Python demos are organized under \demo/python/\ to match each app:

| Subfolder | Targets | Port |
|-----------|---------|------|
| \demo/python/core/\ | core-app | 48100 |
| \demo/python/input/\ | input-app | 48101 |
| \demo/python/advanced/\ | advanced-app | 48102 |
| \demo/python/drag/\ | drag-app | 48103 |
| \demo/python/progress/\ | progress-app | 48104 |
| \demo/python/image/\ | image-app | 48105 |
| \demo/python/color/\ | color-app | 48106 |
| \demo/python/todo/\ | todo-app | 48107 |
| \demo/python/settings/\ | settings-app | 48112 |
| \demo/python/dynamicfxml/\ | dynamic-fxml-app | 48110 |
| \demo/python/explorer/\ | explorer-app | 48111 |
| \demo/python/usersearch/\ | user-search-app | 48109 |

### Run everything

\\ash
python demo/python/run_all.py
\
Starts each app in sequence with auto-launch. Requires the agent JAR and compiled classes.

You can also run:

\\ash
python -m demo.python.run_all
python scripts/run_demo.py
\
### Run a specific demo

\\ash
# Core demos (requires core-app on port 48100)
python demo/python/core/login_direct.py
python demo/python/core/discover_nodes.py

# Settings demo (requires settings-app on port 48112)
python demo/python/settings/settings_demo.py

# Dynamic FXML demo (requires dynamic-fxml-app on port 48110)
python demo/python/dynamicfxml/dynamic_fxml_demo.py

# Explorer demo (requires explorer-app on port 48111)
python demo/python/explorer/explorer_demo.py

# User Search demo (requires user-search-app on port 48109)
python demo/python/usersearch/user_search_demo.py
\
### Benchmark

\\ash
python demo/python/run_benchmark.py
\
Runs the Phase 1 benchmark and prints timing results for JavaFX node query and OCR parsing.
