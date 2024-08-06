package lightningstrike;

// 
// Decompiled by Procyon v0.6.0
// 

public class CCode
{
    static final int Code_value_bits = 16;
    static final int Top_value = 65535;
    static final int First_qtr = 16384;
    static final int Half = 32768;
    static final int Third_qtr = 49152;
    static final int No_of_chars = 1024;
    static final int EOF_symbol = 1025;
    static final int No_of_symbols = 1025;
    static final int DECODE_ERR = -9999;
    public byte[] iBuf;
    public byte[] oBuf;
    public int no_of_chars;
    public int eOF_symbol;
    public int no_of_symbols;
    public int[] char_to_index;
    public byte[] index_to_char;
    public int[] freq;
    public int[] cum_freq;
    public int low;
    public int high;
    public int value;
    public int buffer;
    public int bits_to_go;
    public int garbage_bits;
    public int bits_to_follow;
    public int curbit;
    public int eofstartbit;
    public int max_frequency;
    public int model_delta;
    private int iBufIndex;
    private int oBufIndex;
    
    public CCode() {
        this.no_of_chars = 1024;
        this.no_of_symbols = this.no_of_chars + 1;
        this.eOF_symbol = this.no_of_symbols;
        this.max_frequency = 8192;
        this.model_delta = 10;
        this.char_to_index = new int[1024];
        this.index_to_char = new byte[1026];
        this.freq = new int[1026];
        this.cum_freq = new int[1026];
    }
    
    public void Init(final int no_of_chars) {
        if (no_of_chars == 1) {
            this.max_frequency = 8192;
            this.model_delta = 1000;
        }
        else {
            this.max_frequency = 8192;
            this.model_delta = 10;
        }
        this.no_of_chars = no_of_chars;
        this.no_of_symbols = this.no_of_chars + 1;
        this.eOF_symbol = this.no_of_symbols;
    }
    
    // The name of the final parameter was taken from the call to the Init method
     public int Decode(final byte[] iBuf, final int iBufIndex, final byte[] oBuf, final int no_of_chars) {
        this.iBuf = iBuf;
        this.oBuf = oBuf;
        this.iBufIndex = iBufIndex;
        this.oBufIndex = 0;
        this.Init(no_of_chars);
        int n2;
        if (no_of_chars % 2 == 0) {
            n2 = no_of_chars / 2 - 1;
        }
        else {
            n2 = no_of_chars / 2;
        }
        this.StartModel();
        this.StartInputingBits();
        this.StartDecoding();
        while (true) {
            final int decodeSymbol = this.DecodeSymbol(this.cum_freq);
            if (decodeSymbol == this.eOF_symbol) {
                break;
            }
            this.oBuf[this.oBufIndex] = (byte)(this.index_to_char[decodeSymbol] - n2);
            ++this.oBufIndex;
            this.UpdateModel(decodeSymbol);
        }
        return this.iBufIndex;
    }
    
    public void StartInputingBits() {
        this.bits_to_go = 0;
        this.garbage_bits = 0;
        this.curbit = 0;
    }
    
    public int InputBit() {
        if (this.bits_to_go == 0) {
            this.buffer = this.iBuf[this.iBufIndex];
            ++this.iBufIndex;
            if (this.buffer == -1) {
                ++this.garbage_bits;
                if (this.garbage_bits > 14) {
                    return -9999;
                }
            }
            this.bits_to_go = 8;
        }
        final int n = this.buffer & 0x1;
        this.buffer >>= 1;
        --this.bits_to_go;
        ++this.curbit;
        return n;
    }
    
    public boolean StartDecoding() {
        this.value = 0;
        for (int i = 1; i <= 16; ++i) {
            this.value = 2 * this.value + this.InputBit();
        }
        this.low = 0;
        this.high = 65535;
        this.curbit = 16;
        return true;
    }
    
    public int DecodeSymbol(final int[] array) {
        int n = 1;
        final int n2 = this.high - this.low + 1;
        final int n3 = ((this.value - this.low + 1) * array[0] - 1) / n2;
        int n4 = 1;
        while (array[n++] > n3) {
            ++n4;
        }
        if (n4 == this.eOF_symbol) {
            return n4;
        }
        this.high = this.low + n2 * array[n4 - 1] / array[0] - 1;
        this.low += n2 * array[n4] / array[0];
        while (true) {
            if (this.high >= 32768) {
                if (this.low >= 32768) {
                    this.value -= 32768;
                    this.low -= 32768;
                    this.high -= 32768;
                }
                else {
                    if (this.low < 16384 || this.high >= 49152) {
                        break;
                    }
                    this.value -= 16384;
                    this.low -= 16384;
                    this.high -= 16384;
                }
            }
            this.low *= 2;
            this.high = 2 * this.high + 1;
            if (this.bits_to_go == 0) {
                this.buffer = this.iBuf[this.iBufIndex];
                ++this.iBufIndex;
                if (this.buffer == -1) {
                    ++this.garbage_bits;
                }
                this.bits_to_go = 8;
            }
            final int n5 = this.buffer & 0x1;
            this.buffer >>= 1;
            --this.bits_to_go;
            this.value = 2 * this.value + n5;
            ++this.curbit;
        }
        return n4;
    }
    
    public void StartModel() {
        for (int i = 0; i < this.no_of_chars; ++i) {
            this.char_to_index[i] = i + 1;
            this.index_to_char[i + 1] = (byte)i;
        }
        for (int j = 0; j <= this.no_of_symbols; ++j) {
            this.freq[j] = 1;
            this.cum_freq[j] = this.no_of_symbols - j;
        }
        this.freq[0] = 0;
    }
    
    //the name of the parameter could be guessed by the use in the Decode method
    public void UpdateModel(final int decoded_symbol_index) {
        if (this.cum_freq[0] >= this.max_frequency) {
            int rescaled_cum_freq = 0;
            for (int i = this.no_of_symbols; i >= 0; --i) {
                this.freq[i] = (this.freq[i] + 1) / 2;
                this.cum_freq[i] = rescaled_cum_freq;
                rescaled_cum_freq += this.freq[i];
            }
        }

        //Find the next symbol with a lower index and a different frequency
        int next_higher_freq_symbol_index;
        for (next_higher_freq_symbol_index = decoded_symbol_index; this.freq[next_higher_freq_symbol_index] == this.freq[next_higher_freq_symbol_index - 1]; --next_higher_freq_symbol_index) {}
        if (next_higher_freq_symbol_index < decoded_symbol_index) {
            final byte lower_rank_symbol_char = this.index_to_char[next_higher_freq_symbol_index];
            final byte decoded_symbol_char = this.index_to_char[decoded_symbol_index];
            this.index_to_char[next_higher_freq_symbol_index] = decoded_symbol_char;
            this.index_to_char[decoded_symbol_index] = lower_rank_symbol_char;
            this.char_to_index[lower_rank_symbol_char] = decoded_symbol_index;
            this.char_to_index[decoded_symbol_char] = next_higher_freq_symbol_index;
        }

        final int new_decoded_symbol_index = next_higher_freq_symbol_index;

        this.freq[new_decoded_symbol_index] += this.model_delta;

        //update the cum_freq of the remaining symbols
        for(int i = new_decoded_symbol_index -1; i >= 0; --i) {
            this.cum_freq[i] += this.model_delta;
        }
    }
}
