"""Demonstrate the settings-app across all 5 tabs.

Scenario:
1.  Reset → verify default username "john_doe" and email "john@example.com"
2.  Account tab:
      - type new username / email
      - enter matching passwords → Save → "Settings saved"
3.  Account tab password mismatch:
      - enter mismatched passwords → Save → error status
4.  Appearance tab:
      - switch to Appearance, change theme to "Dark"
      - verify fontSizeLabel initial format "12 pt"
5.  Notifications tab:
      - toggle DND on → dndStatusLabel = "DND: On"
      - toggle DND off → dndStatusLabel = "DND: Off"
6.  Advanced tab:
      - enable proxy check → proxyField becomes enabled
      - disable proxy check → proxyField becomes disabled
7.  About tab:
      - verify appNameLabel and versionLabel are present
8.  Test Notification button:
      - click testNotifBtn → 2 s later → statusLabel = "✔ Test notification sent."
"""
from __future__ import annotations

import time

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── 1. Reset to defaults ──────────────────────────────────────────────────
    r = client.click(id="resetBtn")
    assert r.ok, f"resetBtn click failed: {r}"
    time.sleep(0.3)

    r = client.get_text(id="usernameField")
    assert r.ok, f"get_text(usernameField) failed: {r}"
    assert r.value == "john_doe", f"Expected 'john_doe', got {r.value!r}"
    print("reset → usernameField = 'john_doe' (ok)")

    r = client.get_text(id="emailField")
    assert r.ok, f"get_text(emailField) failed: {r}"
    assert r.value == "john@example.com", f"Expected 'john@example.com', got {r.value!r}"
    print("reset → emailField = 'john@example.com' (ok)")

    # ── 2. Account tab: valid save ────────────────────────────────────────────
    r = client.select_tab("Account", id="settingsTabs")
    assert r.ok, f"select_tab(Account) failed: {r}"

    # clear and type a new username
    r = client.press_key("Control+A", id="usernameField")
    assert r.ok
    r = client.type("demo_user", id="usernameField")
    assert r.ok, f"type usernameField: {r}"

    # clear password fields and set matching passwords
    client.press_key("Control+A", id="passwordField")
    client.type("Pass1234", id="passwordField")
    client.press_key("Control+A", id="confirmPasswordField")
    client.type("Pass1234", id="confirmPasswordField")

    r = client.click(id="saveBtn")
    assert r.ok, f"saveBtn click failed: {r}"
    time.sleep(0.2)

    r = client.get_text(id="statusLabel")
    assert r.ok, f"get_text(statusLabel) failed: {r}"
    assert "saved" in r.value.lower(), f"Expected 'saved' in status, got {r.value!r}"
    print(f"valid save → statusLabel = {r.value!r} (ok)")

    # ── 3. Account tab: password mismatch ────────────────────────────────────
    client.press_key("Control+A", id="passwordField")
    client.type("AAA", id="passwordField")
    client.press_key("Control+A", id="confirmPasswordField")
    client.type("BBB", id="confirmPasswordField")

    r = client.click(id="saveBtn")
    assert r.ok, f"saveBtn click (mismatch) failed: {r}"
    time.sleep(0.2)

    r = client.get_text(id="statusLabel")
    assert r.ok, f"get_text(statusLabel) failed: {r}"
    assert "match" in r.value.lower(), f"Expected mismatch error, got {r.value!r}"
    print(f"password mismatch → statusLabel = {r.value!r} (ok)")

    # ── 4. Appearance tab ────────────────────────────────────────────────────
    r = client.select_tab("Appearance", id="settingsTabs")
    assert r.ok, f"select_tab(Appearance) failed: {r}"
    time.sleep(0.2)

    # verify fontSizeLabel initial text after reset
    r = client.get_text(id="fontSizeLabel")
    assert r.ok, f"get_text(fontSizeLabel) failed: {r}"
    assert "pt" in r.value, f"Expected fontSizeLabel to contain 'pt', got {r.value!r}"
    print(f"Appearance → fontSizeLabel = {r.value!r} (ok)")

    r = client.select("Dark", id="themeCombo")
    assert r.ok, f"select(Dark, themeCombo) failed: {r}"
    print("Appearance → themeCombo selected 'Dark' (ok)")

    # ── 5. Notifications tab: DND toggle ──────────────────────────────────────
    r = client.select_tab("Notifications", id="settingsTabs")
    assert r.ok, f"select_tab(Notifications) failed: {r}"
    time.sleep(0.2)

    # ensure DND is off first (reset already set it to off)
    r = client.get_text(id="dndStatusLabel")
    assert r.ok, f"get_text(dndStatusLabel) failed: {r}"
    assert "off" in r.value.lower(), f"Expected DND off, got {r.value!r}"
    print(f"Notifications → dndStatusLabel = {r.value!r} (ok)")

    r = client.click(id="dndToggle")
    assert r.ok, f"dndToggle click failed: {r}"
    time.sleep(0.1)
    r = client.get_text(id="dndStatusLabel")
    assert r.ok
    assert "on" in r.value.lower(), f"Expected DND on, got {r.value!r}"
    print(f"DND toggled on → dndStatusLabel = {r.value!r} (ok)")

    r = client.click(id="dndToggle")
    assert r.ok, f"dndToggle click (off) failed: {r}"
    time.sleep(0.1)
    r = client.get_text(id="dndStatusLabel")
    assert r.ok
    assert "off" in r.value.lower(), f"Expected DND off again, got {r.value!r}"
    print(f"DND toggled off → dndStatusLabel = {r.value!r} (ok)")

    # ── 6. Advanced tab: proxy enable/disable ────────────────────────────────
    r = client.select_tab("Advanced", id="settingsTabs")
    assert r.ok, f"select_tab(Advanced) failed: {r}"
    time.sleep(0.2)

    # proxy disabled by default
    r = client.get_selected(id="enableProxyCheck")
    assert r.ok, f"get_selected(enableProxyCheck) failed: {r}"
    assert r.value is False, f"Expected enableProxyCheck=False, got {r.value}"
    print("Advanced → enableProxyCheck = False (default ok)")

    r = client.click(id="enableProxyCheck")
    assert r.ok, f"click enableProxyCheck failed: {r}"
    time.sleep(0.1)
    # type into proxyField (should now be enabled)
    r = client.type("http://proxy.example.com:8080", id="proxyField")
    assert r.ok, f"type proxyField (enabled) failed: {r}"
    print("Advanced → enableProxy=True → proxyField accepts input (ok)")

    r = client.click(id="enableProxyCheck")  # disable again
    assert r.ok
    time.sleep(0.1)
    print("Advanced → enableProxy toggled off (ok)")

    # ── 7. About tab: labels present ─────────────────────────────────────────
    r = client.select_tab("About", id="settingsTabs")
    assert r.ok, f"select_tab(About) failed: {r}"
    time.sleep(0.2)

    r = client.get_text(id="appNameLabel")
    assert r.ok, f"get_text(appNameLabel) failed: {r}"
    assert r.value, "appNameLabel is empty"
    print(f"About → appNameLabel = {r.value!r} (ok)")

    r = client.get_text(id="versionLabel")
    assert r.ok, f"get_text(versionLabel) failed: {r}"
    assert r.value, "versionLabel is empty"
    print(f"About → versionLabel = {r.value!r} (ok)")

    # ── 8. Test Notification (on Notifications tab) ──────────────────────────
    r = client.select_tab("Notifications", id="settingsTabs")
    assert r.ok, f"select_tab(Notifications) for test notif: {r}"
    time.sleep(0.2)

    r = client.click(id="testNotifBtn")
    assert r.ok, f"click testNotifBtn failed: {r}"
    # animation takes 2 s
    client.wait_for_text(id="statusLabel", expected="Test notification sent", timeout=8.0)
    print("Test Notification → 'Test notification sent' in statusLabel (ok)")

    print("\nsettings_demo succeeded (ok)")


if __name__ == "__main__":
    main()
