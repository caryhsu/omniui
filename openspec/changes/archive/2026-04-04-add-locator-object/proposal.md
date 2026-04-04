## Why

Automation scripts and Page Object Models frequently target the same node across multiple calls — connecting, waiting, interacting, and verifying. Without a reusable handle, callers repeat the same `id="..."` keyword on every line. This makes scripts verbose and fragile: renaming a node ID requires updating every call site.

A `Locator` object stores the selector once and re-uses it on every subsequent method call, reducing noise and making Page Object Model patterns natural.

## What Changes

- Add `omniui/locator.py` — `Locator` class with full interaction API
- Add `OmniUIClient.locator(**selector) -> Locator` factory method
- Export `Locator` from `omniui.__init__`

## Capabilities

### New Capabilities
- `locator`: Reusable node handle that stores a selector and delegates all interaction methods to the underlying `OmniUIClient`

### Modified Capabilities
- `python-automation-client`: New `locator()` factory method on `OmniUIClient`; new `Locator` type exported from the package

## Impact

- `omniui/locator.py` — new file: `Locator` class
- `omniui/core/engine.py` — add `locator()` factory method
- `omniui/__init__.py` — export `Locator`
- `docs/api/python-client.md` — new Locator section
- `docs/api/python-client.zh-TW.md` — same in Traditional Chinese
- `openspec/specs/python-automation-client/spec.md` — new Locator requirement
- `openspec/specs/python-automation-client/spec.zh-TW.md` — same in Traditional Chinese
- `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark Locator `[x]`
- `tests/test_client.py` — 6 new unit tests
