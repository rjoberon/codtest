import sys
from pathlib import Path
import tempfile
import unittest

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT / "src") not in sys.path:
    sys.path.insert(0, str(ROOT / "src"))

from cis_cod.c_cod_image import CCodImage


class TestCisCod(unittest.TestCase):
    def test_write_image_uses_output_extension(self):
        cod_path = ROOT / "tests" / "fixtures" / "testimg1.cod"

        with tempfile.TemporaryDirectory() as output_dir:
            for extension, expected_format in ((".png", "PNG"), (".jpg", "JPEG")):
                with self.subTest(extension=extension):
                    output_path = Path(output_dir) / f"decoded{extension}"
                    CCodImage.write_image(str(cod_path), str(output_path))

                    with Image.open(output_path) as output_image:
                        self.assertEqual(output_image.format, expected_format)

    def test_cod_images_match_decompressed_references(self):
        for i in range(2, 5):
            cod_path = ROOT / "tests" / "fixtures" / f"testimg{i}.cod"
            png_path = ROOT / "tests" / "fixtures" / f"testimg{i}_decompressed.png"

            with self.subTest(image=f"testimg{i}"):
                decoded = CCodImage.open(str(cod_path))
                actual = decoded.decompressed_image.convert("RGBA")
                with Image.open(png_path) as reference:
                    expected = reference.convert("RGBA")

                self.assertEqual(actual.size, expected.size)

                for actual_pixel, expected_pixel in zip(actual.getdata(), expected.getdata()):
                    self.assertEqual(actual_pixel, expected_pixel)

    def test_testimg1_decodes_to_source_dimensions(self):
        cod_path = ROOT / "tests" / "fixtures" / "testimg1.cod"
        png_path = ROOT / "tests" / "fixtures" / "testimg1.png"

        decoded = CCodImage.open(str(cod_path))
        with Image.open(png_path) as source:
            source_size = source.size

        self.assertIsNotNone(decoded.decompressed_image)
        self.assertEqual(decoded.decompressed_image.size, source_size)

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
