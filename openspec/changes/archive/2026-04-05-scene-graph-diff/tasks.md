## 1. Models

- [ ] 1.1 Add `UISnapshot` dataclass to `omniui/core/models.py`: fields `nodes: list[dict]`, `timestamp: float`
- [ ] 1.2 Add `UIDiff` dataclass to `omniui/core/models.py`: fields `added: list[dict]`, `removed: list[dict]`, `changed: list[dict]`
- [ ] 1.3 Export `UISnapshot` and `UIDiff` from `omniui/__init__.py`

## 2. Python Client

- [ ] 2.1 Add `snapshot()` method to `OmniUIClient`: calls `get_nodes()` and wraps result in `UISnapshot(nodes=..., timestamp=time.time())`
- [ ] 2.2 Add `diff(before: UISnapshot, after: UISnapshot) -> UIDiff` method: pure function, uses `fxId` or `handle` as identity key, compares `text`, `enabled`, `visible`, `value`, `nodeType` attributes

## 3. Demo

- [ ] 3.1 Create `demo/python/advanced/snapshot_diff_demo.py`: take a snapshot, click a button, take another snapshot, call `diff()` and print added/removed/changed
- [ ] 3.2 Add `snapshot_diff_demo` to `run_all.py`

## 4. Tests

- [ ] 4.1 Add `SnapshotDiffTests` to `tests/test_client.py`: test `snapshot()` structure, test `diff()` added/removed/changed/no-change scenarios

## 5. Documentation

- [ ] 5.1 Mark `Scene graph snapshot` and `Scene graph diff` as `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
