from io import BytesIO
import unittest

from cis_cod.binary import read_bytes_with_off


class ShortReadStream(BytesIO):
    def readinto(self, buffer):
        return super().readinto(buffer[:2])


class TestReadBytesWithOffset(unittest.TestCase):
    def test_reads_until_target_is_filled(self):
        target = bytearray(b"......")

        self.assertTrue(read_bytes_with_off(ShortReadStream(b"abcd"), target, 4, -1))
        self.assertEqual(target, b"abcd..")

    def test_returns_false_after_partial_eof(self):
        target = bytearray(b"......")

        self.assertFalse(read_bytes_with_off(ShortReadStream(b"ab"), target, 4, 1))
        self.assertEqual(target, b".ab...")

    def test_rejects_reads_past_target(self):
        with self.assertRaisesRegex(ValueError, "exceeds target buffer"):
            read_bytes_with_off(BytesIO(b"abc"), bytearray(2), 3, 0)


if __name__ == "__main__":
    unittest.main()
