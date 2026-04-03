# advanced-javafx-demo-scenarios (delta)

## Delta: add-text-input-controls

### New demo sections in LoginDemoApp

Three new UI sections added after the TabPane section:

| Control       | fx:id                | Demo actions                              |
|---------------|----------------------|-------------------------------------------|
| TextArea      | `demoTextArea`       | set text (multi-line), read back, clear   |
| PasswordField | `demoPasswordField`  | set password, read plain text back        |
| Hyperlink     | `demoHyperlink`      | verify not-visited, click, verify visited |

### New demo scripts

| Script                   | Verifies                                                 |
|--------------------------|----------------------------------------------------------|
| `text_area_demo.py`      | Multi-line write/read roundtrip, clear                   |
| `password_field_demo.py` | Password write/read plain-text roundtrip                 |
| `hyperlink_demo.py`      | Initial visited=False, click, visited=True               |
