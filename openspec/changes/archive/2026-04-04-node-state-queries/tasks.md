## 1. Python Engine

- [x] 1.1 Add `is_visible(**selector) -> bool` to `engine.py`: call `find()`, read `metadata["visible"]`, return `False` on not-found or any exception
- [x] 1.2 Add `is_enabled(**selector) -> bool` to `engine.py`: call `find()`, read `metadata["enabled"]`, return `False` on not-found or any exception

## 2. Demo Script

- [x] 2.1 Create `demo/python/node_state_demo.py`: verify `is_visible` and `is_enabled` return expected values for known nodes (e.g. `loginButton` visible+enabled, non-existent id returns False)

## 3. Integration

- [x] 3.1 Add `node_state_demo` to both import branches and `main()` in `demo/python/run_all.py`
- [x] 3.2 Run `python demo/python/node_state_demo.py` and confirm it passes

## 4. Documentation

- [x] 4.1 Update `README.md` Status section: add `is_visible`, `is_enabled` under **Actions — basic interaction**
- [x] 4.2 Update `README.zh-TW.md` with the same changes in Chinese
- [x] 4.3 Update `docs/api/python-client.md`: add `is_visible` and `is_enabled` method entries with signatures, descriptions, and usage examples
- [x] 4.4 Update `ROADMAP.md` and `ROADMAP.zh-TW.md`: mark node state queries as done (`- [x]`)
