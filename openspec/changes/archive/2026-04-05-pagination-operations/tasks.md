## 1. Java Agent

- [ ] 1.1 Add `get_page` case in `performOnFxThread()` → `handleGetPage(node, fxId, handle)`
- [ ] 1.2 Implement `handleGetPage()`: check `getSimpleName().equals("Pagination")`, call `getCurrentPageIndex()` + `getPageCount()`, return `{page, page_count}`
- [ ] 1.3 Add `set_page` case → `handleSetPage(node, fxId, handle, payload)`
- [ ] 1.4 Implement `handleSetPage()`: parse `page` from payload, clamp to `[0, pageCount-1]`, call `setCurrentPageIndex()`
- [ ] 1.5 Add `next_page` case → inline: get current + pageCount, set `min(current+1, pageCount-1)`
- [ ] 1.6 Add `prev_page` case → inline: get current, set `max(current-1, 0)`

## 2. Demo App

- [ ] 2.1 Add `Pagination` with `fx:id="demoPagination"` (5 pages) to `AdvancedDemoApp`

## 3. Python Client

- [ ] 3.1 Add `get_page(self, *, id: str)` in `engine.py`
- [ ] 3.2 Add `set_page(self, *, id: str, page: int)` in `engine.py`
- [ ] 3.3 Add `next_page(self, *, id: str)` in `engine.py`
- [ ] 3.4 Add `prev_page(self, *, id: str)` in `engine.py`

## 4. Demo Script

- [ ] 4.1 Create `demo/python/advanced/pagination_demo.py`
- [ ] 4.2 Add import + `Pagination Demo` section to `demo/python/run_all.py`

## 5. Tests

- [ ] 5.1 Add `PaginationTests` to `tests/test_client.py`
- [ ] 5.2 Run `python -m pytest tests/` — confirm all pass

## 6. Wrap-up

- [ ] 6.1 Update `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark Pagination as `[x]`
- [ ] 6.2 `git commit` + `git push`
