## 1. Java Agent

- [x] 1.1 In `resolve()` in `ReflectiveJavaFxTarget.java`: extract `index` (default 0) from selector at the top of the method; replace `.findFirst()` with `.skip(index).findFirst()` in all three selector branches (`id`, `text+type`, fallback)

## 2. Build

- [x] 2.1 Run `mvn clean install -f java-agent/pom.xml`
- [x] 2.2 Delete `demo/javafx-login-app/target/omniui-demo` and run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`

## 3. Demo & Tests

- [x] 3.1 Create `demo/python/index_selector_demo.py`: use `index=` to select the second Label or Button in the demo app and verify the correct text is returned
- [x] 3.2 Add `index_selector_demo` to `demo/python/run_all.py`

## 4. Documentation

- [x] 4.1 Add `index=` selector description to `README.md` and `README.zh-TW.md`
- [x] 4.2 Add `index=` to the selector reference in `docs/api/python-client.md`
- [x] 4.3 Mark `index=` selector `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
