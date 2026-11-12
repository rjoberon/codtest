"""Reader for legacy CIS/COD images."""

from .c_code import CCode
from .cyiq import CYIQ

__all__ = ["CCodImage", "CCode", "CYIQ", "read_image", "write_image"]


def __getattr__(name):
	if name in {"CCodImage", "read_image", "write_image"}:
		from .c_cod_image import CCodImage, read_image, write_image

		return {
			"CCodImage": CCodImage,
			"read_image": read_image,
			"write_image": write_image,
		}[name]
	raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
