## Key Decisions

### 1. No new action types needed
`TextArea` and `PasswordField` both extend `TextInputControl`, which declares
`getText()` and `setText(String)`. The existing `get_text` and `type` actions already
call these methods via reflection. No new cases in `performOnFxThread` are required —
both control types work out of the box once exposed in the demo app.

### 2. PasswordField — read plain text
`PasswordField.getText()` returns the unmasked string (the mask is display-only).
Automation reads the raw value; no special handling needed.

### 3. Hyperlink — reuse existing `click` / `fire`
`Hyperlink` extends `ButtonBase` → `fire()` triggers its `onAction` handler.
The existing `handleClick` path works as-is. To verify visited state, use
`isVisited()` via the existing `get_value` → `safeInvoke` mechanism.

### 4. Demo sections in LoginDemoApp
Three new VBox sections added below the TabPane section:
- **TextArea section**: id `demoTextArea`, pre-filled text, read-back assertion
- **PasswordField section**: id `demoPasswordField`, write → read plain text
- **Hyperlink section**: id `demoHyperlink`, click → check `isVisited()`

### 5. Python client — no new public methods
All three controls use existing API:
- `client.type(selector, text)` — sets text
- `client.get_text(selector)` — reads text
- `client.click(selector)` — fires Hyperlink
- `client.get_value(selector)` — reads `isVisited()` boolean via `get_value`

### 6. Demo scripts (one per control)
- `text_area_demo.py` — write multi-line text, read back, verify
- `password_field_demo.py` — write password, read back plain text, verify
- `hyperlink_demo.py` — check initial visited=False, click, check visited=True
