from __future__ import annotations

class CCode:
    Code_value_bits = 16
    Top_value = (1 << Code_value_bits) - 1
    First_qtr = (Top_value // 4) + 1
    Half = 2 * First_qtr
    Third_qtr = 3 * First_qtr
    No_of_chars = 1024
    No_of_symbols = No_of_chars + 1
    EOF_symbol = No_of_chars + 1
    DECODE_ERR = -9999

    def __init__(self):
        self.no_of_chars = CCode.No_of_chars
        self.no_of_symbols = CCode.No_of_symbols
        self.eof_symbol = CCode.EOF_symbol
        self.max_frequency = 8192
        self.model_delta = 10
        self.char_to_index = [0] * CCode.No_of_chars
        self.index_to_char = [0] * (CCode.No_of_symbols + 1)
        self.freq = [0] * (CCode.No_of_symbols + 1)
        self.cum_freq = [0] * (CCode.No_of_symbols + 1)
        self.i_buf = b""
        self.o_buf = b""
        self.i_buf_index = 0
        self.o_buf_index = 0
        self.low = 0
        self.high = 0
        self.value = 0
        self.buffer = 0
        self.bits_to_go = 0
        self.garbage_bits = 0
        self.curbit = 0

    def init(self, no_of_chars):
        if no_of_chars > 255:
            raise ValueError("Warning: Number of characters is larger than 255. Only the 8 LSB will be used. Could mean that decoder will not work correctly.")
        if no_of_chars > CCode.No_of_chars:
            raise ValueError("Number of characters is larger than the maximum number of characters allowed.")
        self.max_frequency = 8192
        if no_of_chars == 1:
            self.model_delta = 1000
        else:
            self.model_delta = 10
        self.no_of_chars = no_of_chars
        self.no_of_symbols = self.no_of_chars + 1
        self.eof_symbol = self.no_of_symbols

    def decode(self, i_buf, i_buf_index, o_buf, no_of_chars):
        self.i_buf = i_buf
        self.o_buf = o_buf
        self.i_buf_index = i_buf_index
        self.o_buf_index = 0
        self.init(no_of_chars)
        # The median shift likely reverses the encoder's mapping and helps coefficient quantization.
        if no_of_chars % 2 == 0:
            median_char_idx = no_of_chars // 2 - 1
        else:
            median_char_idx = no_of_chars // 2
        self.start_model()
        self.start_inputing_bits()
        self.start_decoding()
        while True:
            decode_symbol = self.decode_symbol(self.cum_freq)
            if decode_symbol == self.eof_symbol:
                break
            if decode_symbol == CCode.DECODE_ERR:
                break
            current_byte = self.index_to_char[decode_symbol]
            # Convert back to a signed byte after removing the median offset.
            value = (current_byte & 0xFF) - median_char_idx
            if value > 127:
                value -= 256
            self.o_buf[self.o_buf_index] = value
            self.o_buf_index += 1
            self.update_model(decode_symbol)
        return self.i_buf_index

    def start_inputing_bits(self):
        self.bits_to_go = 0
        self.garbage_bits = 0
        self.curbit = 0

    def input_bit(self):
        if self.bits_to_go == 0:
            if self.i_buf_index >= len(self.i_buf):
                # The arithmetic coder permits a limited number of padding bits after the input ends.
                self.buffer = 255
                self.garbage_bits += 1
                if self.garbage_bits > (CCode.Code_value_bits - 2):
                    return CCode.DECODE_ERR
            else:
                self.buffer = self.i_buf[self.i_buf_index]
                self.i_buf_index += 1
            self.bits_to_go = 8
        bit = self.buffer & 0x1
        self.buffer >>= 1
        self.bits_to_go -= 1
        self.curbit += 1
        return bit

    def start_decoding(self):
        self.value = 0
        # Seed the code value with the first 16 bits, then begin with the full interval.
        for _ in range(16):
            self.value = (2 * self.value + (self.input_bit() & 0x1)) & 0xFFFF
        self.low = 0
        self.high = CCode.Top_value
        return True

    def decode_symbol(self, current_cum_freq):
        # This follows Witten, Neal, and Cleary, "Arithmetic coding for data compression"
        # (https://doi.org/10.1145/214762.214771); see also Sayood, Introduction to Data Compression, 4th ed., p. 117.
        total_cum_freq = current_cum_freq[0]
        range_size = self.high - self.low + 1
        # Map the current code value into the cumulative-frequency range.
        scaled_value = ((self.value - self.low + 1) * total_cum_freq - 1) // range_size
        current_symbol_index = 1
        while current_cum_freq[current_symbol_index] > scaled_value:
            current_symbol_index += 1
        if current_symbol_index == self.eof_symbol:
            return current_symbol_index
        # Narrow the interval to the range assigned to this symbol.
        self.high = self.low + (range_size * current_cum_freq[current_symbol_index - 1] // total_cum_freq) - 1
        self.low = self.low + (range_size * current_cum_freq[current_symbol_index] // total_cum_freq)
        # Renormalize with the E1, E2, and E3 mappings described in the paper.
        while True:
            if self.high < CCode.Half:
                # E1: the interval lies in the lower half.
                pass
            elif self.low >= CCode.Half:
                # E2: the interval lies in the upper half.
                self.value -= CCode.Half
                self.low -= CCode.Half
                self.high -= CCode.Half
            elif self.low >= CCode.First_qtr and self.high < CCode.Third_qtr:
                # E3: the interval lies in the middle half.
                self.value -= CCode.First_qtr
                self.low -= CCode.First_qtr
                self.high -= CCode.First_qtr
            else:
                break
            # Apply the scale transformation and bring in the next input bit.
            self.low = 2 * self.low
            self.high = 2 * self.high + 1
            next_bit = self.input_bit()
            if next_bit == CCode.DECODE_ERR:
                return CCode.DECODE_ERR
            self.value = 2 * self.value + (next_bit & 0x1)
        return current_symbol_index

    def start_model(self):
        # Start with the identity symbol mapping and equal frequencies.
        for i in range(self.no_of_chars):
            self.char_to_index[i] = i + 1
            self.index_to_char[i + 1] = i
        for j in range(self.no_of_symbols + 1):
            self.freq[j] = 1
            self.cum_freq[j] = self.no_of_symbols - j
        self.freq[0] = 0

    def update_model(self, decoded_symbol_index):
        if self.cum_freq[0] >= self.max_frequency:
            # Halve frequencies to keep cumulative totals within the coder's limit.
            rescaled_cum_freq = 0
            for i in range(self.no_of_symbols, -1, -1):
                self.freq[i] = (self.freq[i] + 1) // 2
                self.cum_freq[i] = rescaled_cum_freq
                rescaled_cum_freq += self.freq[i]
        next_higher_freq_symbol_index = decoded_symbol_index
        # Keep symbols ordered by frequency by moving ties toward the higher-frequency end.
        while self.freq[next_higher_freq_symbol_index] == self.freq[next_higher_freq_symbol_index - 1]:
            next_higher_freq_symbol_index -= 1
        if next_higher_freq_symbol_index < decoded_symbol_index:
            # Update both lookup tables to reflect the symbol swap.
            higher_freq_symbol_char = self.index_to_char[next_higher_freq_symbol_index]
            decoded_symbol_char = self.index_to_char[decoded_symbol_index]
            self.index_to_char[next_higher_freq_symbol_index] = decoded_symbol_char
            self.index_to_char[decoded_symbol_index] = higher_freq_symbol_char
            self.char_to_index[higher_freq_symbol_char & 0xFF] = decoded_symbol_index
            self.char_to_index[decoded_symbol_char & 0xFF] = next_higher_freq_symbol_index
        new_decoded_symbol_index = next_higher_freq_symbol_index
        # Increase this symbol's frequency and all cumulative totals that include it.
        self.freq[new_decoded_symbol_index] += self.model_delta
        for i in range(new_decoded_symbol_index - 1, -1, -1):
            self.cum_freq[i] += self.model_delta
