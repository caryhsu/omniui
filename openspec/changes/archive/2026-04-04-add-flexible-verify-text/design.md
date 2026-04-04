## Context

`verify_text()` in `engine.py` currently:
1. Calls `get_text(**selector)`
2. Sets `result.value = {"actual": ..., "expected": ..., "matches": actual == expected}`
3. Sets `result.ok = matches`

The entire logic is Python-side. Adding match modes requires only extending step 2/3.

## Goals / Non-Goals

**Goals:**
- `match="exact"` — default, current behaviour unchanged
- `match="contains"` — `expected in actual`
- `match="starts_with"` — `actual.startswith(expected)`
- `match="regex"` — `re.search(expected, actual) is not None`

**Non-Goals:**
- `ends_with` (can be done with regex `expected + "$"`)
- Case-insensitive flag (use regex with `(?i)` prefix for now)
- Changing the return value structure (keep `{"actual", "expected", "matches"}`)

## Decisions

### D1: `match` as a keyword-only parameter with default `"exact"`
```python
def verify_text(self, expected: str, *,  match: str = "exact", **selector: Any)
```
Default `"exact"` means all existing callers continue to work unchanged.

### D2: Store `match` mode in result.value for traceability
```python
result.value = {"actual": ..., "expected": ..., "match": match, "matches": ...}
```
Adding `"match"` to the dict doesn't break existing callers who only read `["matches"]`.

### D3: Raise `ValueError` for unknown match mode
Fast-fail on typos like `match="Contains"` rather than silently returning False.

## Risks / Trade-offs

- **`re.search` vs `re.fullmatch`**: `search` matches anywhere in the string — more useful for UI text. Document this.
- **Existing tests**: all use `verify_text(expected, id=...)` without `match=` — unaffected.
