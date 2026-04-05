"""Demonstrate get_page(), set_page(), next_page(), prev_page() on a Pagination control."""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # Read initial state (page=0, page_count=5)
    result = client.get_page(id="demoPagination")
    assert result.ok, f"get_page failed: {result}"
    assert result.value["page"] == 0, f"Expected page=0, got {result.value['page']}"
    assert result.value["page_count"] == 5, f"Expected page_count=5, got {result.value['page_count']}"
    print(f"get_page: page={result.value['page']}, page_count={result.value['page_count']} (ok)")

    # set_page to 3
    result = client.set_page(id="demoPagination", page=3)
    assert result.ok, f"set_page failed: {result}"
    result = client.get_page(id="demoPagination")
    assert result.value["page"] == 3
    print("set_page(3) -> page=3 (ok)")

    # prev_page -> 2
    result = client.prev_page(id="demoPagination")
    assert result.ok
    result = client.get_page(id="demoPagination")
    assert result.value["page"] == 2
    print("prev_page() -> page=2 (ok)")

    # next_page -> 3
    result = client.next_page(id="demoPagination")
    assert result.ok
    result = client.get_page(id="demoPagination")
    assert result.value["page"] == 3
    print("next_page() -> page=3 (ok)")

    # Clamping: set_page out of range
    result = client.set_page(id="demoPagination", page=999)
    assert result.ok
    result = client.get_page(id="demoPagination")
    assert result.value["page"] == 4, f"Expected clamped to 4, got {result.value['page']}"
    print("set_page(999) clamped to 4 (ok)")

    # next_page at last page is no-op
    result = client.next_page(id="demoPagination")
    assert result.ok
    result = client.get_page(id="demoPagination")
    assert result.value["page"] == 4
    print("next_page at last page is no-op (ok)")

    # Reset to 0
    client.set_page(id="demoPagination", page=0)

    print("\npagination_demo succeeded (ok)")


if __name__ == "__main__":
    main()
