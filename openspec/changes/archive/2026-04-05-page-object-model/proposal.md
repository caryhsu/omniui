## Why

Tests using OmniUI repeat the same selector strings across multiple test functions. Without a standard structure, each project reinvents its own wrapper layer. A `OmniPage` base class gives test authors a clear, documented pattern (Page Object Model) with zero boilerplate.

## What Changes

- Add `OmniPage` base class in `omniui/page.py`
- Export `OmniPage` from `omniui/__init__.py`
- Add usage example to `docs/` and a demo test in `tests/`

## Capabilities

### New Capabilities

- `page-object-model`: `OmniPage` base class that wraps `OmniUIClient` for structured page objects

### Modified Capabilities

## Impact

- `omniui/page.py` — new file
- `omniui/__init__.py` — export `OmniPage`
- `tests/test_page_object.py` — example usage test
