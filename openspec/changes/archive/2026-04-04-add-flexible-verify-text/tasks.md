## 1. Python Client

- [ ] 1.1 In `engine.py`: update `verify_text(self, expected, **selector)` signature to `verify_text(self, expected, *, match: str = "exact", **selector)`
- [ ] 1.2 Replace the `actual == expected` comparison with a match-mode dispatcher using `re` module; raise `ValueError` for unknown modes; store `"match"` key in `result.value`

## 2. Demo & Tests

- [ ] 2.1 Create `demo/python/flexible_verify_text_demo.py`: test all 4 modes (`exact`, `contains`, `starts_with`, `regex`) against the `status` label and `loginSectionTitle` label
- [ ] 2.2 Add `flexible_verify_text_demo` to `demo/python/run_all.py`

## 3. Documentation

- [ ] 3.1 Update `verify_text` description in `README.md` and `README.zh-TW.md`
- [ ] 3.2 Update `verify_text` method doc in `docs/api/python-client.md`
- [ ] 3.3 Mark `Flexible verify_text` `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
