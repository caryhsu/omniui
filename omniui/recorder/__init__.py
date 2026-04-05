"""OmniUI recorder sub-package: selector inference and script generation."""
from .selector_inference import infer_selector
from .script_gen import generate_script

__all__ = ["infer_selector", "generate_script"]
