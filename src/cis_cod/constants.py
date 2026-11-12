class Constants:
    MAX_LEVELS = 10
    MAX_SUBBANDS = 31
    VERT_BAND = 0
    HOR_BAND = 1
    DIAG_BAND = 2
    ZEROTREENODE = -126
    GRAY_IMAGE = 0
    COLOR_IMAGE = 1
    GRAY8 = 611
    COLOR24 = 62
    COLOR_8bit = 10
    BMP = 1
    COD = 2
    TGA = 3
    GIF = 4
    JPG = 5
    BI_WAVELET = 1
    ARITHMETIC = 1
    ARITHMETIC_LOSSLESS = 2
    SISD = 1
    TABLE_QUANTIZATION = 1
    YIQ_RGB = 1
    YUV_RGB = 2
    YCbCr = 3
    BEGIN = 0
    CURRENT = 1

    @staticmethod
    def get_word(array, offset):
        intermediate = ((0xFF & array[offset + 1]) << 8) | (0xFF & array[offset])
        # was: return intermediate & 0xFFFF
        if intermediate >= 0x8000:
            intermediate -= 0x10000
        return intermediate

    @staticmethod
    def read_one_byte(stream):
        data = stream.read(1)
        if not data:
            raise EOFError("Unexpected end of stream while reading byte")
        return data[0]

    @staticmethod
    def read_bytes(stream, size):
        chunks = []
        remaining = size
        while remaining > 0:
            chunk = stream.read(remaining)
            if not chunk:
                raise EOFError("Unexpected end of stream while reading bytes")
            chunks.append(chunk)
            remaining -= len(chunk)
        return b"".join(chunks)

    @staticmethod
    def read_word(stream):
        buf = Constants.read_bytes(stream, 2)
        value = ((0xFF & buf[1]) << 8) | (0xFF & buf[0])
        if value >= 0x8000:
            value -= 0x10000
        return value

    @staticmethod
    def read_dword(stream):
        buf = Constants.read_bytes(stream, 4)
        return (
            ((0xFF & buf[3]) << 24)
            | ((0xFF & buf[2]) << 16)
            | ((0xFF & buf[1]) << 8)
            | (0xFF & buf[0])
        )

    @staticmethod
    def skip(stream, count):
        if count <= 0:
            return True
        skipped = 0
        while skipped < count:
            chunk = stream.read(min(4096, count - skipped))
            if not chunk:
                return False
            skipped += len(chunk)
        return True

    @staticmethod
    def read_bytes_with_off(stream, target, count, offset):
        if offset < 0:
            offset = 0
        if offset + count > len(target):
            raise ValueError("read_bytes_with_off exceeds target buffer")
        written = 0
        while written < count:
            chunk = stream.read(count - written)
            if not chunk:
                return False
            target[offset + written:offset + written + len(chunk)] = chunk
            written += len(chunk)
        return True
