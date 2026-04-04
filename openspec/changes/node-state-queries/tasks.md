## 1. Python Engine

- [ ] 1.1 Add `is_visible(**selector) -> bool` to `engine.py`: call `find()`, read `metadata["visible"]`, return `False` on not-found or any exception
- [ ] 1.2 Add `is_enabled(**selector) -> bool` to `engine.py`: call `find()`, read `metadata["enabled"]`, return `False` on not-found or any exception

## 2. Demo Script

- [ ] 2.1 Create `demo/python/node_state_demo.py`: verify `is_visible` and `is_enabled` return expected values for known nodes (e.g. `loginButton` visible+enabled, non-existent id returns False)

## 3. Integration

- [ ] 3.1 Add `node_state_demo` to both import branches and `main()` in `demo/python/run_all.py`
- [ ] 3.2 Run `python demo/python/node_state_demo.py` and confirm it passes
