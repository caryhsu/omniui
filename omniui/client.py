"""Public Python client entry points."""

from .core.engine import OmniUIClient
from .ocr_module import SimpleOcrEngine
from .vision_module import SimpleVisionEngine


class OmniUI:
    """Factory for creating OmniUI client sessions."""

    @staticmethod
    def connect(
        base_url: str = "http://127.0.0.1:48100",
        app_name: str = "LoginDemo",
        pid: int | None = None,
        ocr_engine: SimpleOcrEngine | None = None,
        vision_engine: SimpleVisionEngine | None = None,
    ) -> OmniUIClient:
        return OmniUIClient.connect(
            base_url=base_url,
            app_name=app_name,
            pid=pid,
            ocr_engine=ocr_engine,
            vision_engine=vision_engine,
        )
