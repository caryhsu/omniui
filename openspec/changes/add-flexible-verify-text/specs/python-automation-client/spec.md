## MODIFIED Requirements

### Requirement: Stable selector argument model
The system SHALL allow high-level Python actions to be invoked with selector fields such as `id`, `text`, `type`, and `index` without exposing backend-specific transport or adapter details in the script surface.

#### Scenario: Use structural selector arguments in click API
- **WHEN** a user calls `click(text="Login", type="Button")`
- **THEN** the Python API accepts the selector and delegates backend resolution without requiring explicit OCR or vision flags from the caller

#### Scenario: Use index= to target second matching node
- **WHEN** an automation script calls `client.click(type="Button", index=1)`
- **THEN** the Python client passes `{"type": "Button", "index": 1}` as the selector to the Java agent, which resolves the second Button

## ADDED Requirements

### Requirement: verify_text supports flexible match modes
The system SHALL accept an optional `match` parameter in `verify_text(expected, *, match="exact", **selector)` supporting the following modes:

- `"exact"` (default) — `actual == expected`; preserves all existing behaviour
- `"contains"` — `expected in actual`
- `"starts_with"` — `actual.startswith(expected)`
- `"regex"` — `re.search(expected, actual) is not None`

An unknown `match` value SHALL raise `ValueError`.

#### Scenario: Default exact match unchanged
- **WHEN** a script calls `client.verify_text("Success", id="status")`
- **THEN** the result behaves identically to before — `ok=True` only when the text equals `"Success"` exactly

#### Scenario: contains match
- **WHEN** a script calls `client.verify_text("Success", match="contains", id="status")` and the actual text is `"Login Success (2026-04-04)"`
- **THEN** `result.ok = True` because `"Success"` is found within the actual text

#### Scenario: starts_with match
- **WHEN** a script calls `client.verify_text("Error:", match="starts_with", id="errorLabel")` and the actual text is `"Error: invalid input"`
- **THEN** `result.ok = True`

#### Scenario: regex match
- **WHEN** a script calls `client.verify_text(r"\d+ items", match="regex", id="countLabel")` and the actual text is `"42 items"`
- **THEN** `result.ok = True` because `re.search(r"\d+ items", "42 items")` matches

#### Scenario: unknown match mode raises ValueError
- **WHEN** a script calls `client.verify_text("x", match="fuzzy", id="label")`
- **THEN** the Python client raises `ValueError` immediately
