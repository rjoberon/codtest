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
            if (decodeSymbol == CCode.DECODE_ERR) {
                //How would that be handled?
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
            if (this.iBufIndex >= this.iBuf.length) {
                // As done in the original code (i.e. the ACM paper) we allow to read more bits than available
                // but only up to a limit of (CCode.Code_value_bits - 2) times
                // the returned bits in this case are "arbitrary" and in our case will be set to 1
                // (since EOF was -1, and thus the lower bits = 0xFF)
                this.buffer = 255;
                ++this.garbage_bits;
                if (this.garbage_bits > (CCode.Code_value_bits - 2)) {
                    return CCode.DECODE_ERR;
                }
            } else {
                this.buffer = this.iBuf[this.iBufIndex];
                ++this.iBufIndex;
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
        this.high = Top_value;
        this.curbit = 16;
        return true;
    }
    
    
    public int DecodeSymbol(final int[] current_cum_freq) {
        //Seems to be the algorithm as described:
        // "Arithmetic coding for data compression" by Witten, Neal, and Cleary
        // https://dl.acm.org/doi/pdf/10.1145/214762.214771
        // a more detailed explanation can be found in
        // "Introduction to Data Compression" by Khalid Sayood
        // 4th Edition, page 117
        // and
        // https://marknelson.us/posts/2014/10/19/data-compression-with-arithmetic-coding.html

        //Decode contents of the current buffer in value
        final int total_cum_freq = current_cum_freq[0];
        final int range = this.high - this.low + 1;
        final int scaled_value = ((this.value - this.low + 1) * total_cum_freq - 1) / range;

//        // i was initially "n"
//        // current_symbol_index was "n4"
//        // current_cum_freq was "array"
        // int i = 1;
        // int current_symbol_index = 1;
        // while (current_cum_freq[i++] > n3) {
        //     ++current_symbol_index;
        // }
// The folowing code should be equivalent to the above commented code

        int current_symbol_index = 1;
        for (int i = 1; current_cum_freq[i] > scaled_value; i++) {
            ++current_symbol_index;
        }


        if (current_symbol_index == this.eOF_symbol) {
            return current_symbol_index;
        }

        //Update the interval boundaries
        this.high = this.low + range * current_cum_freq[current_symbol_index - 1] / total_cum_freq - 1;
        this.low += range * current_cum_freq[current_symbol_index] / total_cum_freq;

        //Rescale the interval boundaries
        //Mapping E_1(x) = 2x
        //Mapping E_2(x) = 2(x - 0.5)
        //Mapping E_3(x) = 2(x - 0.25)
        while (true) {
             if (this.high >= CCode.Half) {
                if (this.low >= CCode.Half) {
                    // The interval is entirely confined to the upper half of the unit interval
                    // use E_2 mapping
                    this.value -= CCode.Half;
                    this.low -= CCode.Half;
                    this.high -= CCode.Half;
                } else {
                    if (this.low < CCode.First_qtr || this.high >= CCode.Third_qtr) {
                        // low must be smaller than half, and high larger than half
                        // this means that if either of the above conditions hold, the interval is at least half the size of the unit interval
                        // the next symbol can be decoded
                        break;
                    } else {
                        // Then interval is confined to the central half of the unit interval
                        // use E_3 mapping
                        this.value -= CCode.First_qtr;
                        this.low -= CCode.First_qtr;
                        this.high -= CCode.First_qtr;
                    }
                }
            } else {
                // The interval is entirely confined to the lower half of the unit interval
                // use E_1 mapping
            }

            //Apply rest of mapping
            this.low *= 2;
            this.high *= 2;
            this.high += 1;

            //Integrate a new bit into the value
            final int next_bit = InputBit();
            if (next_bit == CCode.DECODE_ERR) {
                //Due to the IGNORE_GARBAGE method, this should never happen
                return CCode.DECODE_ERR;
            }
            this.value *= 2;
            this.value += (next_bit != 0) ? 1 : 0; //Make sure that next_bit really is only 1 or 0
        }
        return current_symbol_index;
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
            final byte higher_freq_symbol_char = this.index_to_char[next_higher_freq_symbol_index];
            final byte decoded_symbol_char = this.index_to_char[decoded_symbol_index];
            this.index_to_char[next_higher_freq_symbol_index] = decoded_symbol_char;
            this.index_to_char[decoded_symbol_index] = higher_freq_symbol_char;
            this.char_to_index[higher_freq_symbol_char] = decoded_symbol_index;
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
