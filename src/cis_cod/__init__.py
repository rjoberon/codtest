"""Reader for legacy CIS/COD images."""

from .c_code import CCode
from .cyiq import CYIQ

__all__ = ["CCodImage", "CCode", "CYIQ"]


def __getattr__(name):
	if name == "CCodImage":
		from .c_cod_image import CCodImage

		return CCodImage
	raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
