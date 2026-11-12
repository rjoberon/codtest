# CIS/COD Image Reader

A Python library for reading legacy CIS/COD images.

## Supported format

The decoder currently supports CIS/COD version 3.0 wavelet-compressed images. It
does not support lossless-encoded files, and other versions or encoding options
may not decode.

## Installation

Install the package and its optional Pillow integration from a checkout:

```bash
python -m pip install ".[images]"
```

Pillow is optional. Without it, the decoder can read the image data, but it
does not create a Pillow image or write image files.

## Usage

```python
from cis_cod import CCodImage

decoded = CCodImage.open("image.cod")
image = decoded.decompressed_image
image.save("image.png")
```

To save an image directly, the output format is selected from the filename
extension supported by Pillow:

```python
from cis_cod import CCodImage

CCodImage.write_image("image.cod", "image.png")
```

To run the module's command-line entry point from the repository root without
installing the package, use:

```bash
PYTHONPATH=src python -m cis_cod.c_cod_image image.cod image.jpg
```

The output filename is optional; when omitted, the command writes
`image.cod_decoded.jpg`. Pillow must be installed for image output.

## Development

Install the test dependencies and run the test suite:

```bash
python -m pip install -e ".[test]"
python -m pytest
```

The `.cod` samples, source images, and decompressed reference images used by
the tests are in [`tests/fixtures`](tests/fixtures/).
