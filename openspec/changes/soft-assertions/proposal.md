## Why

Test scripts that assert multiple properties stop at the first failure, leaving the rest untested. Soft assertions let a test collect all failures in one pass and report them together, giving engineers full visibility with a single run.

## What Changes

- New `SoftAssertContext` context manager accessible via `client.soft_assert()`
- All assertion failures inside the `with` block are accumulated instead of raised immediately
- At `__exit__` the context raises a single `AssertionError` summarising every failure
- Module-level `soft_assert()` convenience function also exported from `omniui`

## Capabilities

### New Capabilities

- `soft-assertions`: Context manager that collects `AssertionError` failures and re-raises them as a combined report at block exit

### Modified Capabilities

(none)

## Impact

- `omniui/core/engine.py` — new `SoftAssertContext` class + `OmniUIClient.soft_assert()` + module-level `soft_assert()`
- `omniui/__init__.py` — re-export `soft_assert` and `SoftAssertContext`
- No Java agent changes required
- New demo `demo/python/core/soft_assert_demo.py`
- New tests in `tests/test_client.py`
