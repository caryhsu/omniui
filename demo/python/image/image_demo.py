"""Demonstrate get_image_url() and is_image_loaded() on the image-app.

Scenario:
- Wait for imageView1/imageView2 to finish loading (statusLabel = "ready")
- Verify imageView1 has a valid URL and is loaded
- Verify imageView2 has a URL but is NOT loaded (broken)
- Click switchBtn and verify the images have swapped
- Drag dragSource1 → dropPane: verify dropTargetImage URL = dragSource1's URL
- Drag dragSource2 → dropPane: verify dropTargetImage URL changes to dragSource2's URL

Note: Images load from https://picsum.photos/ — requires network access.
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
    assert url2.value and len(url2.value) > 0, f"Expected non-empty broken URL"
    print(f"imageView2 URL = {url2.value!r} (ok)")

    assert not client.is_image_loaded(id="imageView2"), "Expected imageView2 to NOT be loaded (broken)"
    print("imageView2 is_image_loaded = False (ok)")

    # ── Switch images and verify swap ─────────────────────────────────────────
    result = client.click(id="switchBtn")
    assert result.ok, f"click switchBtn failed: {result}"

    url1_after = client.get_image_url(id="imageView1")
    url2_after = client.get_image_url(id="imageView2")
    assert url1_after.ok and url2_after.ok
    assert url1_after.value == url2.value, (
        f"Expected imageView1 URL={url2.value!r}, got {url1_after.value!r}"
    )
    assert url2_after.value == url1.value, (
        f"Expected imageView2 URL={url1.value!r}, got {url2_after.value!r}"
    )
    print("images swapped correctly (ok)")

    # ── Drag source1 → dropPane: target image updates ────────────────────────
    src1_url = client.get_image_url(id="dragSource1")
    assert src1_url.ok and src1_url.value, "get_image_url(dragSource1) failed"
    print(f"dragSource1 URL = {src1_url.value!r} (ok)")

    drag1 = client.drag(id="dragSource1").to(id="dropPane")
    assert drag1.ok, f"drag dragSource1 → dropPane failed: {drag1}"
    print("dragged dragSource1 → dropPane (ok)")

    drop_text = client.get_text(id="dropResult")
    assert drop_text.ok and "source1" in drop_text.value, (
        f"Expected 'source1 dropped!', got: {drop_text.value!r}"
    )
    print(f"dropResult = {drop_text.value!r} (ok)")

    target_url = client.get_image_url(id="dropTargetImage")
    assert target_url.ok, f"get_image_url(dropTargetImage) failed: {target_url}"
    assert target_url.value == src1_url.value, (
        f"Expected dropTargetImage URL={src1_url.value!r}, got {target_url.value!r}"
    )
    print(f"dropTargetImage URL matches dragSource1 (ok)")

    # ── Drag source2 → dropPane: target image updates again ──────────────────
    src2_url = client.get_image_url(id="dragSource2")
    assert src2_url.ok and src2_url.value

    drag2 = client.drag(id="dragSource2").to(id="dropPane")
    assert drag2.ok, f"drag dragSource2 → dropPane failed: {drag2}"

    drop_text2 = client.get_text(id="dropResult")
    assert drop_text2.ok and "source2" in drop_text2.value, (
        f"Expected 'source2 dropped!', got: {drop_text2.value!r}"
    )
    print(f"dragSource2 → dropPane, dropResult = {drop_text2.value!r} (ok)")

    target_url2 = client.get_image_url(id="dropTargetImage")
    assert target_url2.ok
    assert target_url2.value == src2_url.value, (
        f"Expected dropTargetImage URL={src2_url.value!r}, got {target_url2.value!r}"
    )
    print("dropTargetImage URL matches dragSource2 (ok)")

    print("\nimage_demo succeeded (ok)")


if __name__ == "__main__":
    main()

