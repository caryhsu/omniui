# Headless Mode & CI/CD

OmniUI supports running JavaFX tests in headless environments (e.g., GitHub Actions, Docker) using **Xvfb** (virtual framebuffer on Linux).

## How it works

JavaFX requires a display to render its UI. In CI environments without a physical screen, **Xvfb** provides a virtual display that JavaFX connects to transparently — no code changes needed.

```
Xvfb :99 -screen 0 1280x800x24 &
export DISPLAY=:99
python demo/python/run_all.py
```

## GitHub Actions Workflows

Two workflows are provided:

### `ci-unit.yml` — Unit Tests

Runs the Python unit test suite (no Java app required). Runs on every push and PR.

```bash
python -m pytest tests/ -v
```

### `ci-integration.yml` — Integration Tests

Builds all Java apps and runs the full demo suite against live JavaFX apps via Xvfb.
Can be triggered manually via `workflow_dispatch`.

## Local Headless Testing (Linux / WSL)

```bash
# Install Xvfb
sudo apt-get install xvfb libgtk-3-0 libgl1

# Start virtual display
Xvfb :99 -screen 0 1280x800x24 &
export DISPLAY=:99

# Run tests
cd demo/python
python run_all.py
```

## OS Platform Detection

`run_all.py` automatically selects the correct JavaFX platform JARs:

| OS | JAR classifier |
|---|---|
| Windows | `-win.jar` |
| macOS | `-mac.jar` |
| Linux | `-linux.jar` |

This is handled by `_javafx_classifier()` in `run_all.py` — no manual configuration needed.

## Maven pom.xml Notes

The demo app `pom.xml` files currently declare `<classifier>win</classifier>` for JavaFX dependencies. This only affects compilation resolution; at runtime the JARs are loaded from the local `.m2` cache by path, using the OS-detected classifier from `run_all.py`.

To make Maven itself platform-aware, add profiles (optional):

```xml
<profiles>
  <profile>
    <id>linux</id>
    <activation><os><family>unix</family></os></activation>
    <properties><javafx.platform>linux</javafx.platform></properties>
  </profile>
  <profile>
    <id>windows</id>
    <activation><os><family>windows</family></os></activation>
    <properties><javafx.platform>win</javafx.platform></properties>
  </profile>
  <profile>
    <id>mac</id>
    <activation><os><family>mac</family></os></activation>
    <properties><javafx.platform>mac</javafx.platform></properties>
  </profile>
</profiles>
```
