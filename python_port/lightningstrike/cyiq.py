from __future__ import annotations

from array import array

from .c_code import CCode
from .constants import Constants


def s16(value: int) -> int:
    """Convert an integer to Java short semantics.

    Java's short is a signed 16-bit integer. The original decoder stores coefficients and
    reconstructed values in short fields and narrows every intermediate calculation to 16
    bits, which matters for the last wavelet reconstruction pass. Without this step, Python
    keeps the wider integer and tiny off-by-one errors accumulate.
    """
    value = int(value)
    value &= 0xFFFF
    return value - 0x10000 if value >= 0x8000 else value


class CYIQ:
    def __init__(self):
        self.codec = CCode()
        self.level_data_size = [0] * 11
        self.quantizer = array('h', [0]) * Constants.MAX_SUBBANDS
        self.mask_quantizer = array('h', [0]) * Constants.MAX_SUBBANDS
        self.threshold = array('h', [0]) * Constants.MAX_SUBBANDS
        self.mask_threshold = array('h', [0]) * Constants.MAX_SUBBANDS
        self.min = array('h', [0]) * Constants.MAX_SUBBANDS
        self.max = array('h', [0]) * Constants.MAX_SUBBANDS
        self.width = 0
        self.height = 0
        self.levels = 5
        self.mean = 128
        self.yiq_p = None
        self.cod_pointer = 0
        self.zero_levels = 0
        self.sub_samples = 0

    def input_begin_of_block(self, cod_data, n, n2):
        idx = 3 * (self.levels - n) + n2
        self.max[idx] = Constants.get_word(cod_data, self.cod_pointer)
        self.cod_pointer += 2
        self.min[idx] = Constants.get_word(cod_data, self.cod_pointer)
        self.cod_pointer += 2
        self.quantizer[idx] = Constants.get_word(cod_data, self.cod_pointer)
        self.cod_pointer += 2

    def input_end_of_block(self, cod_data):
        if cod_data[self.cod_pointer:self.cod_pointer + 3] == b"cod":
            self.cod_pointer += 3
            return
        if self.cod_pointer > 0 and cod_data[self.cod_pointer - 1:self.cod_pointer + 2] == b"cod":
            self.cod_pointer += 2
            return
        if self.cod_pointer > 1 and cod_data[self.cod_pointer - 2:self.cod_pointer + 1] == b"cod":
            self.cod_pointer += 1

    def read_file(self, input_stream):
        self.zero_levels = Constants.read_one_byte(input_stream)
        # This plane's subsampling flag controls its width, height, and coefficient-buffer size.
        self.sub_samples = Constants.read_one_byte(input_stream)
        return True

    def init_decompress(self, width, height, wavelet_levels):
        self.levels = wavelet_levels - self.sub_samples
        self.width = width >> self.sub_samples
        self.height = height >> self.sub_samples
        self.size = self.width * self.height
        # Allocate only this plane's subsampled coefficient grid.
        self.yiq_p = array('h', [0]) * self.size
        return self.yiq_p is not None

    def end_decompress(self):
        pass

    def decompress(self, cod_data, cod_pointer, n):
        self.cod_pointer = cod_pointer
        o_buf = [0] * (self.width * self.height)
        if n == 0:
            self.input_begin_of_block(cod_data, n, 0)
            self.cod_pointer = self.codec.decode(cod_data, self.cod_pointer, o_buf, 2 * self.quantizer[3 * self.levels] + 1)
            self.input_end_of_block(cod_data)
            if not self.data_rearr(o_buf, n, 0):
                return -1
        elif n < self.levels - self.zero_levels + 1:
            for n2 in range(3):
                self.input_begin_of_block(cod_data, n, n2)
                if n < self.levels - self.zero_levels:
                    self.cod_pointer = self.codec.decode(cod_data, self.cod_pointer, o_buf, 2 * self.quantizer[3 * (self.levels - n) + n2] + 2)
                else:
                    self.cod_pointer = self.codec.decode(cod_data, self.cod_pointer, o_buf, 2 * self.quantizer[3 * (self.levels - n) + n2] + 1)
                self.input_end_of_block(cod_data)
                if not self.data_rearr(o_buf, n, n2):
                    return -1
        if cod_data is None:
            return -1
        if n < self.levels - self.zero_levels + 1 and not self.wavelet_dequantization(n):
            return -1
        if n > 0 and not self.wavelet_reconstruction(n):
            return -1
        return self.cod_pointer

    def set_dequantization(self, n, n2, level, direction, count):
        if count == 0:
            n = 0.0
            n2 = 0.0
        else:
            n /= count
            n2 /= -count
        n5 = self.width >> (level + 1)
        n6 = self.height >> (level + 1)
        n7 = 0
        n8 = 0
        if direction in (76, 108):
            n7 = 0
            n8 = 0
        elif direction in (72, 104):
            n7 = n5
            n8 = n5
        elif direction in (86, 118):
            n7 = n6 * self.width
            n8 = n6 * self.width
        elif direction in (68, 100):
            n7 = n6 * self.width + n5
            n8 = n6 * self.width + n5
        else:
            n7 = 0
            n8 = 0
        for n9 in range(n6):
            n10 = 0
            while n10 < n5:
                self.yiq_p[n8] = self.yiq_p[n7]
                if self.yiq_p[n7] > 0:
                    self.yiq_p[n8] = s16(self.yiq_p[n7] * n)
                elif self.yiq_p[n7] < 0:
                    self.yiq_p[n8] = s16(self.yiq_p[n7] * n2)
                n7 += 1
                n8 += 1
                n10 += 1
            n7 += self.width - n10
            n8 += self.width - n10
        return True

    def wavelet_dequantization(self, n):
        level = self.levels - n
        if level < self.levels:
            self.set_dequantization(self.max[level * 3], self.min[level * 3], level, ord('v'), self.quantizer[level * 3])
            self.set_dequantization(self.max[level * 3 + 1], self.min[level * 3 + 1], level, ord('h'), self.quantizer[level * 3 + 1])
            self.set_dequantization(self.max[level * 3 + 2], self.min[level * 3 + 2], level, ord('d'), self.quantizer[level * 3 + 2])
        else:
            self.set_dequantization(self.max[level * 3], self.min[level * 3], level - 1, ord('l'), self.quantizer[level * 3])
        return True

    def data_rearr(self, decoded_values, n, n2):
        yiq_p = self.yiq_p
        n3 = 0
        n4 = 0
        array2 = [0, 0, 0]
        array3 = [0, 0, 0]
        array4 = [0, 0, 0]
        if n == 0:
            n5 = self.height >> (self.levels - n)
            n6 = self.width >> (self.levels - n)
        else:
            n5 = self.height >> (self.levels - n + 1)
            n6 = self.width >> (self.levels - n + 1)
        array2[1] = n6
        array2[0] = n5 * self.width
        array2[2] = n5 * self.width + n6
        array3[1] = 0
        array4[1] = n6
        array3[0] = n5
        array4[0] = 0
        array3[2] = n5
        array4[2] = n6
        if n == 0:
            for n7 in range(n5):
                for n8 in range(n6):
                    yiq_p[n3] = s16(decoded_values[n4])
                    n4 += 1
                    n3 += 1
                n3 += self.width - n6
        else:
            n9 = self.quantizer[3 * (self.levels - n) + n2] + 1
            n10 = array2[n2]
            for n11 in range(n5):
                for n12 in range(n6):
                    if yiq_p[n10] == Constants.ZEROTREENODE:
                        yiq_p[n10] = 0
                        n10 += 1
                    else:
                        yiq_p[n10] = s16(decoded_values[n4])
                        n4 += 1
                        if yiq_p[n10] == n9:
                            self.set_up_zero_tree(n11 + array3[n2], n12 + array4[n2], n, Constants.ZEROTREENODE, self.levels - self.zero_levels)
                            yiq_p[n10] = 0
                        n10 += 1
                n10 += self.width - n6
        return True

    def set_up_zero_tree(self, n, n2, n3, n4, n5):
        n6 = 2
        n7 = 2
        n8 = (n << 1) * self.width + (n2 << 1)
        for n9 in range(n3, n5):
            n10 = n8
            for i in range(n7):
                for n11 in range(n6):
                    self.yiq_p[n10] = n4
                    n10 += 1
                n10 += self.width - n6
            n7 <<= 1
            n6 <<= 1
            n8 <<= 1

    def wavelet_reconstruction(self, level):
        region_height = self.height >> (self.levels - level)
        region_width = self.width >> (self.levels - level)
        if level <= self.levels - self.zero_levels:
            if not self.wavelet_recons_t(self.yiq_p, region_height, region_width, self.width):
                return False
        elif not self.wavelet_recons_t_fast(self.yiq_p, region_height, region_width, self.width):
            return False
        return True

    def wavelet_recons_t(self, coefficients, region_height, region_width, row_stride):
        workspace = array('h', [0]) * (region_height * region_width)
        n4 = 0
        n5 = region_width
        n6 = 0
        n7 = (region_height // 2 + 1) * row_stride
        n8 = n7 - row_stride
        for n9 in range(region_width):
            n10 = coefficients[n8]
            workspace[n5] = s16(((coefficients[n6] << 1) + coefficients[n7] + n10) >> 2)
            workspace[n4] = s16(((coefficients[n6] << 1) + coefficients[n7] - (n10 << 2) - n10 - (n10 << 1)) >> 2)
            n6 += 1
            n7 += 1
            n8 += 1
            n4 += 1
            n5 += 1
        n11 = (region_height - 2) * region_width
        n12 = n11 + region_width
        n13 = (region_height // 2 - 1) * row_stride
        n14 = n13 - row_stride
        n15 = (region_height - 1) * row_stride
        n16 = n15 - row_stride
        for n17 in range(region_width):
            n18 = coefficients[n15]
            workspace[n12] = s16(((coefficients[n13] << 1) + n18) >> 2)
            workspace[n11] = s16((s16(coefficients[n13] + coefficients[n14]) >> 2) + (s16((n18 << 1) + coefficients[n16]) >> 3) - n18)
            n13 += 1
            n14 += 1
            n15 += 1
            n16 += 1
            n11 += 1
            n12 += 1
        n19 = region_height // 2 - 1
        n20 = region_width * 3
        n21 = 2 * region_width
        n22 = row_stride
        n23 = row_stride * (2 + region_height // 2)
        n24 = n23 - row_stride
        for n25 in range(1, n19):
            for n26 in range(region_width):
                n27 = s16((s16(coefficients[n22] << 1) + s16(coefficients[n23] + coefficients[n24])) >> 2)
                workspace[n20] = n27
                workspace[n21] = s16((s16(workspace[n21 - region_width] + n27) >> 1) - coefficients[n24])
                n22 += 1
                n23 += 1
                n24 += 1
                n20 += 1
                n21 += 1
            n22 += row_stride - region_width
            n23 += row_stride - region_width
            n24 = n23 - row_stride
            n20 += region_width
            n21 += region_width
        n29 = 0
        n30 = region_width // 2 + 1
        n31 = n30 - 1
        n32 = 0
        n33 = 0
        n34 = 1
        for n35 in range(region_height):
            n36 = workspace[n32 + n31]
            coefficients[n34] = s16(workspace[n32 + n29] + (s16(workspace[n32 + n30] + n36) >> 1))
            coefficients[n33] = s16(workspace[n32 + n29] + (s16(workspace[n32 + n30] - n36) >> 1) - n36 - (n36 << 1))
            n33 += row_stride
            n34 += row_stride
            n32 += region_width
        n37 = region_width // 2 - 1
        n38 = n37 - 1
        n39 = region_width - 1
        n40 = n39 - 1
        n41 = 0
        n42 = region_width - 2
        n43 = n42 + 1
        for n44 in range(region_height):
            coefficients[n43] = s16(workspace[n41 + n37] + (s16(workspace[n41 + n39]) >> 1))
            coefficients[n42] = s16((((s16(workspace[n41 + n37] + workspace[n41 + n38] - workspace[n41 + n39]) << 1) + workspace[n41 + n40]) >> 2) - workspace[n41 + n39])
            n42 += row_stride
            n43 += row_stride
            n41 += region_width
        n45 = region_width // 2 - 1
        n46 = 0
        n47 = 2
        n48 = n47 + 1
        for n49 in range(region_height):
            n50 = n46 + 1
            n51 = n46 + region_width // 2 + 2
            n52 = n51 - 1
            n53 = coefficients[n47 - 1]
            for n54 in range(1, n45):
                n55 = s16((s16(workspace[n51] + workspace[n52]) >> 1) + workspace[n50])
                coefficients[n48] = n55
                coefficients[n47] = s16((s16(n53 + n55) >> 1) - (workspace[n52] << 1))
                n50 += 1
                n51 += 1
                n52 += 1
                n47 += 2
                n48 += 2
                n53 = n55
            n46 += region_width
            n47 += row_stride - 2 * n45 + 2
            n48 = n47 + 1
        return True

    def wavelet_recons_t_fast(self, coefficients, region_height, region_width, row_stride):
        workspace = array('h', [0]) * (region_height * region_width)
        n4 = region_height // 2 - 1
        n5 = region_width * 2
        n6 = n5 + region_width
        n7 = row_stride
        n8 = 0
        for n9 in range(1, n4):
            for n10 in range(region_width):
                workspace[n6] = s16(coefficients[n7] >> 1)
                workspace[n5] = s16((coefficients[n7] + coefficients[n8]) >> 2)
                n7 += 1
                n8 += 1
                n5 += 1
                n6 += 1
            n7 += row_stride - region_width
            n8 = n7 - row_stride
            n5 += region_width
            n6 = n5 + region_width
        n11 = 0
        n12 = region_width
        n13 = 0
        for n14 in range(region_width):
            value = s16(coefficients[n13] >> 1)
            workspace[n12] = value
            workspace[n11] = value
            n13 += 1
            n11 += 1
            n12 += 1
        n15 = (region_height - 2) * region_width
        n16 = n15 + region_width
        n17 = (region_height // 2 - 1) * row_stride
        n18 = n17 - row_stride
        for n19 in range(region_width):
            workspace[n16] = s16(coefficients[n18] >> 1)
            workspace[n15] = s16((coefficients[n17] + coefficients[n18]) >> 2)
            n17 += 1
            n18 += 1
            n15 += 1
            n16 += 1
        n20 = region_width // 2 - 1
        n21 = 0
        n22 = 2
        n23 = n22 + 1
        for n24 in range(region_height):
            n25 = 1
            n26 = 0
            for n27 in range(1, n20):
                coefficients[n23] = s16(workspace[n21 + n25])
                coefficients[n22] = s16((workspace[n21 + n25] + workspace[n21 + n26]) >> 1)
                n25 += 1
                n26 += 1
                n22 += 2
                n23 += 2
            n21 += region_width
            n22 += row_stride - 2 * n20 + 2
            n23 = n22 + 1
        n28 = 0
        n29 = 0
        n30 = 0
        n31 = 1
        for n32 in range(region_height):
            value = s16(workspace[n29 + n28])
            coefficients[n31] = value
            coefficients[n30] = value
            n30 += row_stride
            n31 += row_stride
            n29 += region_width
        n33 = region_width // 2 - 1
        n34 = n33 - 1
        n35 = 0
        n36 = region_width - 2
        n37 = n36 + 1
        for n38 in range(region_height):
            coefficients[n37] = s16(workspace[n35 + n33])
            coefficients[n36] = s16((workspace[n35 + n33] + workspace[n35 + n34]) >> 1)
            n36 += row_stride
            n37 += row_stride
            n35 += region_width
        return True
