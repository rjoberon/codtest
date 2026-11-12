import sys
from math import sqrt
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from PIL import Image, ImageChops, ImageStat

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT / "src") not in sys.path:
    sys.path.insert(0, str(ROOT / "src"))

import cis_cod
from cis_cod import read_image, write_image
from cis_cod.c_cod_image import CCodImage


def _image_rmse(image, reference):
    difference = ImageChops.difference(image.convert("RGB"), reference.convert("RGB"))
    channel_rms = ImageStat.Stat(difference).rms
    return sqrt(sum(value**2 for value in channel_rms) / len(channel_rms))


class TestCisCod(unittest.TestCase):
    def test_package_exports_only_image_api(self):
        self.assertEqual(cis_cod.__all__, ["CCodImage", "read_image", "write_image"])
        self.assertFalse(hasattr(cis_cod, "CCode"))
        self.assertFalse(hasattr(cis_cod, "CYIQ"))

    def test_write_image_uses_output_extension(self):
        cod_path = ROOT / "tests" / "fixtures" / "testimg1.cod"

        with tempfile.TemporaryDirectory() as output_dir:
            for extension, expected_format in ((".png", "PNG"), (".jpg", "JPEG")):
                with self.subTest(extension=extension):
                    output_path = Path(output_dir) / f"decoded{extension}"
                    write_image(str(cod_path), str(output_path))

                    with Image.open(output_path) as output_image:
                        self.assertEqual(output_image.format, expected_format)

    def test_cod_image_rmse_against_java_and_legacy_references(self):
        java_rmse = {1: 2.72, 2: 3.18, 3: 3.11, 4: 3.32}
        legacy_rmse = {2: 3.17, 3: 3.14, 4: 3.23}

        for i in range(1, 5):
            cod_path = ROOT / "tests" / "fixtures" / f"testimg{i}.cod"
            java_path = ROOT / "tests" / "fixtures" / f"testimg{i}_decompressed_java.png"

            with self.subTest(image=f"testimg{i}"):
                actual = read_image(str(cod_path))
                with Image.open(java_path) as java_reference:
                    java_reference = java_reference.convert("RGB")

                self.assertEqual(actual.size, java_reference.size)
                self.assertAlmostEqual(
                    _image_rmse(actual, java_reference), java_rmse[i], delta=0.01
                )

                if i in legacy_rmse:
                    legacy_path = ROOT / "tests" / "fixtures" / f"testimg{i}_decompressed.png"
                    with Image.open(legacy_path) as legacy_reference:
                        legacy_reference = legacy_reference.convert("RGB")

                    self.assertEqual(actual.size, legacy_reference.size)
                    self.assertAlmostEqual(
                        _image_rmse(actual, legacy_reference),
                        legacy_rmse[i],
                        delta=0.01,
                    )

    def test_testimg1_decodes_to_source_dimensions(self):
        cod_path = ROOT / "tests" / "fixtures" / "testimg1.cod"
        png_path = ROOT / "tests" / "fixtures" / "testimg1.png"

        image = read_image(str(cod_path))
        with Image.open(png_path) as source:
            source_size = source.size

        self.assertIsInstance(image, Image.Image)
        self.assertEqual(image.size, source_size)

    def test_read_image_rejects_invalid_cod_file(self):
        with tempfile.TemporaryDirectory() as output_dir:
            cod_path = Path(output_dir) / "invalid.cod"
            cod_path.write_bytes(b"")

            with self.assertRaisesRegex(ValueError, "Invalid or unsupported COD image"):
                read_image(str(cod_path))

    def test_read_image_requires_pillow(self):
        with patch("cis_cod.c_cod_image.Image", None):
            with self.assertRaisesRegex(RuntimeError, "Pillow is required"):
                read_image("image.cod")

    def test_subsampled_chroma_image_decodes(self):
        cod_path = ROOT / "tests" / "fixtures" / "testimg3.cod"
        png_path = ROOT / "tests" / "fixtures" / "testimg3_decompressed.png"

        decoded = CCodImage.open(str(cod_path))
        with Image.open(png_path) as expected:
            expected_size = expected.size

        self.assertIsNotNone(decoded.decompressed_image)
        self.assertEqual(decoded.decompressed_image.size, expected_size)


if __name__ == "__main__":
    unittest.main()
