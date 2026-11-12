from __future__ import annotations

from typing import BinaryIO

try:
    from PIL import Image
except Exception:  # pragma: no cover
    Image = None

from .binary import read_bytes_with_off, read_dword, read_one_byte, read_word, skip
from .cyiq import CYIQ

GRAY_IMAGE = 0
COLOR_IMAGE = 1
BI_WAVELET = 1
ARITHMETIC = 1
ARITHMETIC_LOSSLESS = 2
SISD = 1
TABLE_QUANTIZATION = 1


class CCodImage:
    """Read and progressively decode a CIS 3.0 wavelet-compressed image."""

    def __init__(self, input_cod_file: BinaryIO):
        self.width = 300
        self.height = 200
        self.magic_marker = bytearray(8)
        self.yiq_y = CYIQ()
        self.yiq_i = CYIQ()
        self.yiq_q = CYIQ()
        self.input_cod_file = input_cod_file
        self.decompressed_image = None
        self.compression_type = 0
        self.encoder_type = 0
        self.quantizer_type = 0
        self.scb = 0
        self.color_type_value = 0
        self.mean = 0
        self.width_value = 0
        self.height_value = 0
        self.bpp = 0
        self.image_width = 0
        self.image_height = 0
        self.current_width = 0
        self.current_height = 0
        self.wavelet_levels = 0
        self.file_buf = b""
        self.color_type = 0
        self.coded_size = 0
        self.level_code_size = []
        self.header_size = 0
        self.comment_size = 0
        self.rgb_data = []
        self.off = 0
        self.read_data = 0

    def read_file(self):
        """Read the fixed header and each color plane's decoder metadata."""
        header_start = self.input_cod_file.tell()
        magic_marker = self.input_cod_file.read(8)
        if len(magic_marker) != 8:
            return False
        self.magic_marker = bytearray(magic_marker)
        # This decoder supports the CIS 3.0 marker only.
        if magic_marker[3:4] != b"3" or magic_marker[5:6] != b"0":
            raise RuntimeError("Version incompatible!")
        self.header_size = read_word(self.input_cod_file)
        self.coded_size = read_dword(self.input_cod_file)
        self.comment_size = read_word(self.input_cod_file)
        self.file_buf = bytearray(self.coded_size)
        self.image_width = read_word(self.input_cod_file)
        self.image_height = read_word(self.input_cod_file)
        self.bpp = read_one_byte(self.input_cod_file)
        self.color_type = read_one_byte(self.input_cod_file)
        if not skip(self.input_cod_file, 2):
            return False
        self.encoder_type = read_one_byte(self.input_cod_file)
        if self.encoder_type == ARITHMETIC_LOSSLESS:
            raise RuntimeError("This version can not decompress Loss-Less compressed image")
        self.compression_type = read_one_byte(self.input_cod_file)
        if self.compression_type != BI_WAVELET:
            return False
        self.quantizer_type = read_one_byte(self.input_cod_file)
        if self.quantizer_type != TABLE_QUANTIZATION:
            return False
        self.scb = read_one_byte(self.input_cod_file)
        if self.scb != SISD:
            return False
        self.color_type_value = read_one_byte(self.input_cod_file)
        self.wavelet_levels = read_one_byte(self.input_cod_file)
        self.level_code_size = [0] * (self.wavelet_levels + 1)
        for i in range(self.wavelet_levels + 1):
            self.level_code_size[i] = read_dword(self.input_cod_file)
        self.adjust_width_height()
        # Each component reads its own subsampling metadata, which determines its coefficient-buffer size.
        self.yiq_y.read_file(self.input_cod_file)
        if self.color_type == COLOR_IMAGE:
            self.yiq_i.read_file(self.input_cod_file)
            self.yiq_q.read_file(self.input_cod_file)
        header_bytes_read = self.input_cod_file.tell() - header_start
        if header_bytes_read > self.header_size:
            return False
        # Some 3.0 files have reserved bytes after the plane metadata; payload data starts at header_size.
        if not skip(self.input_cod_file, self.header_size - header_bytes_read):
            return False
        return True

    def adjust_width_height(self):
        """Pad the image dimensions to a multiple of the wavelet grid size."""
        n = 1 << self.wavelet_levels
        self.width = (self.image_width + (n - self.image_width % n) % n) & 0xFFFF
        self.height = (self.image_height + (n - self.image_height % n) % n) & 0xFFFF

    def init_decompress(self):
        """Allocate coefficient buffers for Y and, for color images, I and Q."""
        self.adjust_width_height()
        if not self.yiq_y.init_decompress(self.width, self.height, self.wavelet_levels):
            return False
        if self.color_type == COLOR_IMAGE:
            if not self.yiq_i.init_decompress(self.width, self.height, self.wavelet_levels):
                return False
            if not self.yiq_q.init_decompress(self.width, self.height, self.wavelet_levels):
                return False
        return True

    def end_decompress(self):
        """Release per-plane decoder state after all levels have been generated."""
        self.yiq_y.end_decompress()
        if self.color_type == COLOR_IMAGE:
            self.yiq_i.end_decompress()
            self.yiq_q.end_decompress()

    def yiq_to_rgb(self, level, yiq_y_power_data, yiq_i_power_data, yiq_q_power_data, rgb_image_data, image_width, image_height):
        """Convert the current YIQ level into ARGB pixels in the output buffer."""
        normalization_shift = self.wavelet_levels - level
        y_scale = 1 << max(0, level - self.yiq_y.levels)
        if self.color_type == COLOR_IMAGE:
            i_scale = 1 << max(0, level - self.yiq_i.levels)
            q_scale = 1 << max(0, level - self.yiq_q.levels)
            for image_pos_y in range(image_height - 1, -1, -1):
                # The coefficient planes may have different subsampling; map each output row separately.
                source_y = image_height - 1 - image_pos_y
                y_row_idx = (source_y // y_scale) * self.yiq_y.width
                i_row_idx = (source_y // i_scale) * self.yiq_i.width
                q_row_idx = (source_y // q_scale) * self.yiq_q.width
                for image_pos_x in range(image_width):
                    # Replicate a subsampled coefficient across its output pixel footprint.
                    y_idx = y_row_idx + image_pos_x // y_scale
                    i_idx = i_row_idx + image_pos_x // i_scale
                    q_idx = q_row_idx + image_pos_x // q_scale
                    normalized_y_value = (yiq_y_power_data[y_idx] >> normalization_shift) + self.mean
                    normalized_i_value = yiq_i_power_data[i_idx] >> normalization_shift
                    normalized_q_value = yiq_q_power_data[q_idx] >> normalization_shift
                    # Fixed-point YIQ-to-RGB matrix; clamp each channel to the byte range below.
                    r_value = normalized_y_value + ((normalized_i_value * 979 + normalized_q_value * 636) >> 10)
                    g_value = normalized_y_value + ((normalized_i_value * -279 + normalized_q_value * -662) >> 10)
                    b_value = normalized_y_value + ((normalized_i_value * -1133 + normalized_q_value * 1744) >> 10)
                    r_part = 0 if r_value < 0 else (255 if r_value > 255 else r_value)
                    g_part = 0 if g_value < 0 else (255 if g_value > 255 else g_value)
                    b_part = 0 if b_value < 0 else (255 if b_value > 255 else b_value)
                    rgb_image_data[image_pos_y * image_width + image_pos_x] = (0xFF000000 | (r_part << 16) | (g_part << 8) | b_part)
            return
        else:
            for image_pos_y in range(image_height - 1, -1, -1):
                source_y = image_height - 1 - image_pos_y
                y_row_idx = (source_y // y_scale) * self.yiq_y.width
                for image_pos_x in range(image_width):
                    y_idx = y_row_idx + image_pos_x // y_scale
                    normalized_y_value = (yiq_y_power_data[y_idx] >> normalization_shift) + self.mean
                    gray_rgb_value = 0 if normalized_y_value < 0 else (255 if normalized_y_value > 255 else normalized_y_value)
                    rgb_image_data[image_pos_y * image_width + image_pos_x] = (0xFF000000 | (gray_rgb_value << 16) | (gray_rgb_value << 8) | gray_rgb_value)

    def get_image(self):
        return self.decompressed_image

    def start_generate_image(self):
        """Parse the file and initialize buffers before the first progressive level."""
        if not self.read_file():
            return False
        if self.encoder_type == 2:
            return False
        self.rgb_data = [0] * (self.width * self.height)
        if not self.init_decompress():
            return False
        self.off = 0
        return True

    def end_generate_image(self):
        """Finish progressive decoding and release plane state."""
        self.end_decompress()
        return True

    def generate_image(self, level):
        """Decode one wavelet level and update the image at that level's resolution."""
        if level == 0:
            self.read_data = 0
        if not read_bytes_with_off(self.input_cod_file, self.file_buf, self.level_code_size[level], self.read_data):
            return False
        self.read_data += self.level_code_size[level]
        self.mean = self.yiq_y.mean
        self.current_width = self.image_width >> (self.wavelet_levels - level)
        self.current_height = self.image_height >> (self.wavelet_levels - level)
        n2 = self.wavelet_levels - level
        self.off = self.yiq_y.decompress(self.file_buf, self.off, level)
        if self.off == -1:
            return False
        if self.color_type == COLOR_IMAGE:
            for component in (self.yiq_i, self.yiq_q):
                # A subsampled plane has fewer effective levels than the luma plane.
                if level > component.levels:
                    continue
                self.off = component.decompress(self.file_buf, self.off, level)
                if self.off == -1:
                    return False
        self.yiq_to_rgb(level, self.yiq_y.yiq_p, self.yiq_i.yiq_p, self.yiq_q.yiq_p, self.rgb_data, self.current_width, self.current_height)
        if Image is not None:
            self.decompressed_image = Image.new("RGBA", (self.current_width, self.current_height))
            self.decompressed_image.putdata([self._argb_to_rgba(v) for v in self.rgb_data[: self.current_width * self.current_height]])
        return True

    @staticmethod
    def _argb_to_rgba(value):
        """Convert a packed ARGB integer to Pillow's RGBA tuple order."""
        alpha = (value >> 24) & 0xFF
        r = (value >> 16) & 0xFF
        g = (value >> 8) & 0xFF
        b = value & 0xFF
        return (r, g, b, alpha)

    def get_levels(self):
        return self.wavelet_levels

    @classmethod
    def open(cls, cod_image):
        """Decode every progressive level from a COD file and return the decoder."""
        with open(cod_image, "rb") as input_stream:
            cod = cls(input_stream)
            if not cod.start_generate_image():
                raise ValueError(f"Invalid or unsupported COD image: {cod_image}")
            try:
                for level in range(cod.wavelet_levels + 1):
                    if not cod.generate_image(level):
                        raise ValueError(
                            f"Failed to decode COD image at level {level}: {cod_image}"
                        )
            finally:
                cod.end_generate_image()
            return cod


def read_image(cod_image):
    """Decode a COD file and return its final image as a Pillow image."""
    if Image is None:
        raise RuntimeError("Pillow is required to read images; install cis-cod[images]")
    cod = CCodImage.open(cod_image)
    if cod.decompressed_image is None:
        raise ValueError(f"COD image could not be decoded: {cod_image}")
    return cod.decompressed_image


def write_image(cod_image, image_file):
    """Decode a COD file and save its final image using the output extension."""
    read_image(cod_image).convert("RGB").save(image_file)


def main():
    import sys
    if len(sys.argv) < 2:
        raise SystemExit("usage: c_cod_image.py <infile>")
    if len(sys.argv) > 2:
        outfile = sys.argv[2]
    else:
        outfile = sys.argv[1] + "_decoded.jpg"
    write_image(sys.argv[1], outfile)


if __name__ == "__main__":
    main()
