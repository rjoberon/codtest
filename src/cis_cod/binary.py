def get_word(data, offset):
    """Read a signed little-endian 16-bit value from a byte buffer."""
    value = ((0xFF & data[offset + 1]) << 8) | (0xFF & data[offset])
    if value >= 0x8000:
        value -= 0x10000
    return value


def read_one_byte(stream):
    data = stream.read(1)
    if not data:
        raise EOFError("Unexpected end of stream while reading byte")
    return data[0]


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


def read_word(stream):
    buf = read_bytes(stream, 2)
    value = ((0xFF & buf[1]) << 8) | (0xFF & buf[0])
    if value >= 0x8000:
        value -= 0x10000
    return value


def read_dword(stream):
    buf = read_bytes(stream, 4)
    return (
        ((0xFF & buf[3]) << 24)
        | ((0xFF & buf[2]) << 16)
        | ((0xFF & buf[1]) << 8)
        | (0xFF & buf[0])
    )


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
