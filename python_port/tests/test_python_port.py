import sys
from pathlib import Path
import unittest

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from lightningstrike.c_cod_image import CCodImage


class TestPythonPort(unittest.TestCase):
    def test_cod_image_matches_reference_png(self):
        base_dir = Path(__file__).resolve().parents[2]

        for i in range(1, 5):
            cod_path = base_dir / "src" / "test" / "resources" / ("testimg" + i + ".cod")
            png_path = base_dir / "src" / "test" / "resources" / ("testimg" + i + ".png")

            decoded = CCodImage.open(str(cod_path))
            actual = decoded.decompressed_image.convert("RGBA")
            expected = Image.open(png_path).convert("RGBA")

            self.assertIsNotNone(actual)
            self.assertEqual(actual.size, expected.size)

            for actual_pixel, expected_pixel in zip(actual.getdata(), expected.getdata()):
                self.assertEqual(actual_pixel, expected_pixel)

    def test_subsampled_chroma_image_decodes(self):
        base_dir = Path(__file__).resolve().parents[2]
        cod_path = base_dir / "src" / "test" / "resources" / "testimg3.cod"
        png_path = base_dir / "src" / "test" / "resources" / "testimg3.png"

        decoded = CCodImage.open(str(cod_path))
        with Image.open(png_path) as expected:
            expected_size = expected.size

        self.assertIsNotNone(decoded.decompressed_image)
        self.assertEqual(decoded.decompressed_image.size, expected_size)


if __name__ == "__main__":
    unittest.main()
