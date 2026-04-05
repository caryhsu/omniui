"""Demonstrate get_image_url() and is_image_loaded() on the image-app.

Scenario:
- Wait for both images to finish loading/failing (statusLabel = "ready")
- Verify imageView1 has a valid URL and is loaded
- Verify imageView2 has a URL but is NOT loaded (broken)
- Click switchBtn and verify the images have swapped
- Drag dragImageView to dropZone and verify dropResult = "dropped!"

Note: imageView1 loads from https://picsum.photos/ — requires network access.
      imageView2 uses an invalid domain — always fails DNS.
"""
from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()

    # ── Wait for both images to finish (loaded or error) ─────────────────────
    client.wait_for_text(id="statusLabel", expected="ready", timeout=15.0)
    print("images ready (ok)")

    # ── Verify imageView1: valid image ────────────────────────────────────────
    url1 = client.get_image_url(id="imageView1")
    assert url1.ok, f"get_image_url(imageView1) failed: {url1}"
    assert url1.value and len(url1.value) > 0, f"Expected non-empty URL, got: {url1.value!r}"
    assert "picsum" in url1.value, f"Expected picsum URL, got: {url1.value!r}"
    print(f"imageView1 URL = {url1.value!r} (ok)")

    assert client.is_image_loaded(id="imageView1"), "Expected imageView1 to be loaded"
    print("imageView1 is_image_loaded = True (ok)")

    # ── Verify imageView2: broken image ───────────────────────────────────────
    url2 = client.get_image_url(id="imageView2")
    assert url2.ok, f"get_image_url(imageView2) failed: {url2}"
    assert url2.value and len(url2.value) > 0, f"Expected non-empty broken URL, got: {url2.value!r}"
    print(f"imageView2 URL = {url2.value!r} (ok)")

    assert not client.is_image_loaded(id="imageView2"), "Expected imageView2 to NOT be loaded (broken)"
    print("imageView2 is_image_loaded = False (ok)")

    # ── Switch images and verify swap ─────────────────────────────────────────
    result = client.click(id="switchBtn")
    assert result.ok, f"click switchBtn failed: {result}"
    print("clicked switchBtn (ok)")

    url1_after = client.get_image_url(id="imageView1")
    url2_after = client.get_image_url(id="imageView2")
    assert url1_after.ok and url2_after.ok
    assert url1_after.value == url2.value, (
        f"Expected imageView1 URL to be swapped to {url2.value!r}, got {url1_after.value!r}"
    )
    assert url2_after.value == url1.value, (
        f"Expected imageView2 URL to be swapped to {url1.value!r}, got {url2_after.value!r}"
    )
    print(f"images swapped correctly (ok)")

    # ── Drag dragImageView → dropZone ─────────────────────────────────────────
    drag_result = client.drag(id="dragImageView").to(id="dropZone")
    assert drag_result.ok, f"drag dragImageView to dropZone failed: {drag_result}"
    print("drag dragImageView → dropZone (ok)")

    drop_text = client.get_text(id="dropResult")
    assert drop_text.ok, f"get_text(dropResult) failed: {drop_text}"
    assert drop_text.value == "dropped!", f"Expected 'dropped!', got: {drop_text.value!r}"
    print(f"dropResult = {drop_text.value!r} (ok)")

    print("\nimage_demo succeeded (ok)")


if __name__ == "__main__":
    main()
