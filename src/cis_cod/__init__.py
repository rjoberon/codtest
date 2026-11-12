"""Reader for legacy CIS/COD images."""

__all__ = ["CodImageDecoder", "read_image", "write_image"]


def __getattr__(name):
	if name in {"CodImageDecoder", "read_image", "write_image"}:
		from .cod_image import CodImageDecoder, read_image, write_image

		return {
			"CodImageDecoder": CodImageDecoder,
			"read_image": read_image,
			"write_image": write_image,
		}[name]
	raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
