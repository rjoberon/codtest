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

Pillow is optional. Install the `images` extra to use `read_image()` or write
image files. Without Pillow, the lower-level decoder remains available.

After installation, decode an image from the command line:

```bash
cis-cod image.cod image.jpg
```

The output filename is optional; when omitted, the command writes
`image.cod_decoded.jpg`. Pillow must be installed for image output.

## Usage

```python
from cis_cod import read_image

image = read_image("image.cod")
image.save("image.png")
```

For access to decoder metadata and lower-level decoding controls, use `CodImageDecoder`:

```python
from cis_cod import CodImageDecoder

decoded = CodImageDecoder.open("image.cod")
image = decoded.decompressed_image
```

To save an image directly, the output format is selected from the filename
extension supported by Pillow:

```python
from cis_cod import write_image

write_image("image.cod", "image.png")
```

To run the module's command-line entry point from the repository root without
installing the package, use:

```bash
PYTHONPATH=src python -m cis_cod.cod_image image.cod image.jpg
```

## Development

Install the test dependencies and run the test suite:

```bash
python -m pip install -e ".[test]"
python -m pytest
```

The `.cod` samples, source images, and decompressed reference images used by
the tests are in [`tests/fixtures`](tests/fixtures/).
