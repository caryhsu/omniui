"""Demonstrate the dynamic-fxml-app across all three dynamically-loaded views.

Scenario:
1. Load Form View via formBtn
   - fill nameField and emailField
   - click submitBtn → statusLabel shows "✔ Submitted — ..."
   - click clearBtn → form is cleared
2. Load Dashboard View via dashboardBtn
   - verify usersCountLabel has a numeric value
   - click refreshBtn → values update
3. Load List View via listBtn
   - add an item via newItemField + addBtn → item appears in itemList
   - click clearBtn → list is empty
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

    # ── 1. Form View ──────────────────────────────────────────────────────────
    r = client.click(id="formBtn")
    assert r.ok, f"formBtn click failed: {r}"
    time.sleep(0.3)

    r = client.type("Alice", id="nameField")
    assert r.ok, f"type nameField failed: {r}"

    r = client.type("alice@example.com", id="emailField")
    assert r.ok, f"type emailField failed: {r}"

    r = client.click(id="submitBtn")
    assert r.ok, f"submitBtn click failed: {r}"
    time.sleep(0.2)

    r = client.get_text(id="statusLabel")
    assert r.ok, f"get_text(statusLabel) failed: {r}"
    assert "Submitted" in r.value, f"Expected 'Submitted' in status, got {r.value!r}"
    print(f"Form submit → statusLabel = {r.value!r} (ok)")

    r = client.click(id="clearBtn")
    assert r.ok, f"clearBtn click failed: {r}"
    time.sleep(0.1)

    r = client.get_text(id="nameField")
    assert r.ok
    assert r.value == "", f"Expected nameField cleared, got {r.value!r}"
    print("Form clear → nameField = '' (ok)")

    # ── 2. Dashboard View ─────────────────────────────────────────────────────
    r = client.click(id="dashboardBtn")
    assert r.ok, f"dashboardBtn click failed: {r}"
    time.sleep(0.3)

    r = client.get_text(id="usersCountLabel")
    assert r.ok, f"get_text(usersCountLabel) failed: {r}"
    assert r.value.isdigit(), f"Expected numeric usersCountLabel, got {r.value!r}"
    before_count = r.value
    print(f"Dashboard → usersCountLabel = {before_count!r} (ok)")

    r = client.click(id="refreshBtn")
    assert r.ok, f"refreshBtn click failed: {r}"
    time.sleep(0.2)
    print("Dashboard → refreshBtn clicked (ok)")

    # ── 3. List View ──────────────────────────────────────────────────────────
    r = client.click(id="listBtn")
    assert r.ok, f"listBtn click failed: {r}"
    time.sleep(0.3)

    r = client.type("Test item from OmniUI", id="newItemField")
    assert r.ok, f"type newItemField failed: {r}"

    r = client.click(id="addBtn")
    assert r.ok, f"addBtn click failed: {r}"
    time.sleep(0.1)

    r = client.select("Test item from OmniUI", id="itemList")
    assert r.ok, f"Item not found in itemList: {r}"
    print("List → added 'Test item from OmniUI' (ok)")

    # clear the list
    r = client.click(id="clearBtn")
    assert r.ok, f"list clearBtn failed: {r}"
    time.sleep(0.1)
    print("List → clearBtn clicked (ok)")

    print("\ndynamic_fxml_demo succeeded (ok)")


if __name__ == "__main__":
    main()
