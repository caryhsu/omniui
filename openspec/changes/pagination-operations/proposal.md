## Why

Tests that interact with paginated data need to read the current page, jump to a specific page, and navigate forward/backward. JavaFX `Pagination` control exposes `getCurrentPageIndex()` / `setCurrentPageIndex()` making this straightforward to implement via reflection.

## What Changes

- Add `get_page(id=...)` — returns current 0-based page index and total page count
- Add `set_page(id=..., page=n)` — jump to page n (0-based)
- Add `next_page(id=...)` — advance one page (no-op at last page)
- Add `prev_page(id=...)` — go back one page (no-op at first page)

## Capabilities

### New Capabilities

- `pagination-read`: Read current page index and total page count from a `Pagination` node
- `pagination-navigate`: Set, advance, or go back a page in a `Pagination` node

### Modified Capabilities

## Impact

- `java-agent/…/ReflectiveJavaFxTarget.java` — new action cases + handler methods
- `omniui/core/engine.py` — four new public methods
- `demo/java/advanced-app/…/AdvancedDemoApp.java` — add a `Pagination` widget
- `demo/python/advanced/pagination_demo.py` — new demo script
- `demo/python/run_all.py` — import + Pagination Demo section
- `tests/test_client.py` — `PaginationTests`
