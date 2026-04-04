## Why

`verify_text()` currently only supports exact string matching. Real UI tests often need looser assertions — "does the status label *contain* 'Success'?" or "does the error message *match* a pattern?". Exact match is too brittle for dynamic text (timestamps, counts, locale-formatted numbers). Adding `contains=`, `starts_with=`, and `regex=` match modes solves this with zero Java agent changes — purely Python-side logic.

## What Changes

- `verify_text()` in `engine.py`: accept optional `match` parameter (`"exact"` default, `"contains"`, `"starts_with"`, `"regex"`)
- No Java agent changes needed
- Demo + documentation

## Capabilities

### New Capabilities
- (none — enhances existing behaviour)

### Modified Capabilities
- `python-automation-client`: `verify_text` now accepts an optional `match` mode parameter

## Impact

- `omniui/core/engine.py` — `verify_text()` only (~5 lines added)
- `demo/python/flexible_verify_text_demo.py` — smoke test
- `demo/python/run_all.py` — add demo
- `README.md`, `README.zh-TW.md`, `docs/api/python-client.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
- **No Java agent changes, no jlink rebuild needed**
