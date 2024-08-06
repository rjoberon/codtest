import java.io.InputStream;

// 
// Decompiled by Procyon v0.6.0
// 

public class CYIQ extends Constants
{
    public short[] yiqP;
    public short width;
    public short height;
    public int size;
    public int[] levelDataSize;
    public short levels;
    public short zeroLevels;
    public short subSamples;
    public short mean;
    public short[] quantizer;
    public short[] maskQuantizer;
    public short[] threshold;
    public short[] maskThreshold;
    public short[] min;
    public short[] max;
    public boolean ifTest;
    public CCode codec;
    public int codPointer;
    
    public CYIQ() {
        this.codec = new CCode();
        this.levelDataSize = new int[11];
        this.quantizer = new short[Constants.MAX_SUBBANDS];
        this.maskQuantizer = new short[Constants.MAX_SUBBANDS];
        this.threshold = new short[Constants.MAX_SUBBANDS];
        this.maskThreshold = new short[Constants.MAX_SUBBANDS];
        this.min = new short[Constants.MAX_SUBBANDS];
        this.max = new short[Constants.MAX_SUBBANDS];
        this.width = 0;
        this.height = 0;
        this.levels = 5;
        this.mean = 128;
        this.yiqP = null;
        this.codPointer = 0;
    }
    
    protected void InputBeginOfBlock(final byte[] array, final short n, final short n2) {
        final int n3 = 3 * (this.levels - n) + n2;
        this.max[n3] = Constants.GETWORD(array, this.codPointer);
        this.codPointer += 2;
        this.min[n3] = Constants.GETWORD(array, this.codPointer);
        this.codPointer += 2;
        this.quantizer[n3] = Constants.GETWORD(array, this.codPointer);
        this.codPointer += 2;
    }
    
    protected void InputEndOfBlock(final byte[] array) {
        if (array[this.codPointer] == 99 && array[this.codPointer + 1] == 111 && array[this.codPointer + 2] == 100) {
            this.codPointer += 3;
            return;
        }
        if (array[this.codPointer - 1] == 99 && array[this.codPointer] == 111 && array[this.codPointer + 1] == 100) {
            this.codPointer += 2;
            return;
        }
        if (array[this.codPointer - 2] == 99 && array[this.codPointer - 1] == 111 && array[this.codPointer] == 100) {
            ++this.codPointer;
        }
    }
    
    public boolean ReadFile(final InputStream inputStream) {
        this.zeroLevels = Constants.READONEBYTE(inputStream);
        this.subSamples = Constants.READONEBYTE(inputStream);
        return true;
    }
    
    public boolean InitDecompress(final short n, final short n2, final short n3) {
        this.levels = (short)(n3 - this.subSamples);
        this.width = (short)(n >> this.subSamples);
        this.height = (short)(n2 >> this.subSamples);
        this.size = this.width * this.height;
        this.yiqP = null;
        this.yiqP = new short[this.size];
        return this.yiqP != null;
    }
    
    public void EndDecompress() {
    }
    
    public int Decompress(final byte[] array, final int codPointer, final short n) {
        this.codPointer = codPointer;
        final byte[] array2 = new byte[this.width * this.height];
        if (array2 == null) {
            return -1;
        }
        if (n == 0) {
            this.InputBeginOfBlock(array, n, (short)0);
            this.codPointer = this.codec.Decode(array, this.codPointer, array2, 2 * this.quantizer[3 * this.levels] + 1);
            this.InputEndOfBlock(array);
            if (!this.DataRearr(array2, n, (short)0)) {
                return -1;
            }
        }
        else if (n < this.levels - this.zeroLevels + 1) {
            for (short n2 = 0; n2 < 3; ++n2) {
                this.InputBeginOfBlock(array, n, n2);
                if (n < this.levels - this.zeroLevels) {
                    this.codPointer = this.codec.Decode(array, this.codPointer, array2, 2 * this.quantizer[3 * (this.levels - n) + n2] + 2);
                }
                else {
                    this.codPointer = this.codec.Decode(array, this.codPointer, array2, 2 * this.quantizer[3 * (this.levels - n) + n2] + 1);
                }
                this.InputEndOfBlock(array);
                if (!this.DataRearr(array2, n, n2)) {
                    return -1;
                }
            }
        }
        if (array == null) {
            return -1;
        }
        if (n < this.levels - this.zeroLevels + 1 && !this.WaveletDequantization(n)) {
            return -1;
        }
        if (n > 0 && !this.WaveletReconstruction(n)) {
            return -1;
        }
        return this.codPointer;
    }
    
    public boolean SetDequantization(float n, float n2, final int n3, final byte b, final int n4) {
        final short[] yiqP = this.yiqP;
        final short[] yiqP2 = this.yiqP;
        if (n4 == 0) {
            n = 0.0f;
            n2 = 0.0f;
        }
        else {
            n /= n4;
            n2 /= -n4;
        }
        final short n5 = (short)(this.width >> n3 + 1);
        final short n6 = (short)(this.height >> n3 + 1);
        int n7 = 0;
        int n8 = 0;
        switch (b) {
            case 76:
            case 108: {
                n7 = 0;
                n8 = 0;
                break;
            }
            case 72:
            case 104: {
                n7 = n5;
                n8 = n5;
                break;
            }
            case 86:
            case 118: {
                n7 = n6 * this.width;
                n8 = n6 * this.width;
                break;
            }
            case 68:
            case 100: {
                n7 = n6 * this.width + n5;
                n8 = n6 * this.width + n5;
                break;
            }
            default: {
                n7 = 0;
                n8 = 0;
                break;
            }
        }
        for (short n9 = 0; n9 < n6; ++n9) {
            short n10;
            for (n10 = 0; n10 < n5; ++n10) {
                yiqP2[n8] = yiqP[n7];
                if (yiqP[n7] > 0) {
                    yiqP2[n8] = (short)(yiqP[n7] * n);
                }
                else if (yiqP[n7] < 0) {
                    yiqP2[n8] = (short)(yiqP[n7] * n2);
                }
                ++n7;
                ++n8;
            }
            n7 += this.width - n10;
            n8 += this.width - n10;
        }
        return true;
    }
    
    public boolean WaveletDequantization(final short n) {
        final short n2 = (short)(this.levels - n);
        if (n2 < this.levels) {
            this.SetDequantization(this.max[n2 * 3], this.min[n2 * 3], n2, (byte)118, this.quantizer[n2 * 3]);
            this.SetDequantization(this.max[n2 * 3 + 1], this.min[n2 * 3 + 1], n2, (byte)104, this.quantizer[n2 * 3 + 1]);
            this.SetDequantization(this.max[n2 * 3 + 2], this.min[n2 * 3 + 2], n2, (byte)100, this.quantizer[n2 * 3 + 2]);
        }
        else {
            this.SetDequantization(this.max[n2 * 3], this.min[n2 * 3], n2 - 1, (byte)108, this.quantizer[n2 * 3]);
        }
        return true;
    }
    
    public boolean DataRearr(final byte[] array, final short n, final short n2) {
        final short[] yiqP = this.yiqP;
        int n3 = 0;
        int n4 = 0;
        final int[] array2 = new int[3];
        final int[] array3 = new int[3];
        final int[] array4 = new int[3];
        int n5;
        int n6;
        if (n == 0) {
            n5 = this.height >> this.levels - n;
            n6 = this.width >> this.levels - n;
        }
        else {
            n5 = this.height >> this.levels - n + 1;
            n6 = this.width >> this.levels - n + 1;
        }
        array2[1] = n6;
        array2[0] = n5 * this.width;
        array2[2] = n5 * this.width + n6;
        array3[1] = 0;
        array4[1] = n6;
        array3[0] = n5;
        array4[0] = 0;
        array3[2] = n5;
        array4[2] = n6;
        if (n == 0) {
            for (short n7 = 0; n7 < n5; ++n7) {
                for (short n8 = 0; n8 < n6; ++n8) {
                    yiqP[n3] = array[n4];
                    ++n4;
                    ++n3;
                }
                n3 += this.width - n6;
            }
        }
        else {
            final short n9 = (short)(this.quantizer[3 * (this.levels - n) + n2] + 1);
            int n10 = array2[n2];
            for (short n11 = 0; n11 < n5; ++n11) {
                for (short n12 = 0; n12 < n6; ++n12) {
                    if (yiqP[n10] == Constants.ZEROTREENODE) {
                        yiqP[n10++] = 0;
                    }
                    else {
                        yiqP[n10] = array[n4++];
                        if (yiqP[n10] == n9) {
                            this.SetUpZeroTree((short)(n11 + array3[n2]), (short)(n12 + array4[n2]), n, (short)(Constants.ZEROTREENODE), (short)(this.levels - this.zeroLevels));
                            yiqP[n10] = 0;
                        }
                        ++n10;
                    }
                }
                n10 += this.width - n6;
            }
        }
        return true;
    }
    
    public void SetUpZeroTree(final short n, final short n2, final short n3, final short n4, final short n5) {
        int n6 = 2;
        int n7 = 2;
        int n8 = (short)(n << 1) * this.width + (short)(n2 << 1);
        for (short n9 = n3; n9 < n5; ++n9) {
            int n10 = n8;
            for (int i = 0; i < n7; ++i) {
                for (short n11 = 0; n11 < n6; ++n11) {
                    this.yiqP[n10++] = n4;
                }
                n10 += this.width - n6;
            }
            n7 <<= 1;
            n6 <<= 1;
            n8 <<= 1;
        }
    }
    
    public boolean WaveletReconstruction(final short n) {
        final short n2 = (short)(this.height >> this.levels - n);
        final short n3 = (short)(this.width >> this.levels - n);
        if (n <= this.levels - this.zeroLevels) {
            if (!this.WaveletReconsT(this.yiqP, n2, n3, this.width)) {
                return false;
            }
        }
        else if (!this.WaveletReconsTFast(this.yiqP, n2, n3, this.width)) {
            return false;
        }
        return true;
    }
    
    private boolean WaveletReconsT(final short[] array, final short n, final short n2, final short n3) {
        final short[] array2;
        if ((array2 = new short[n * n2]) == null) {
            return false;
        }
        int n4 = 0;
        int n5 = n2;
        int n6 = 0;
        int n7 = (n / 2 + 1) * n3;
        int n8 = n7 - n3;
        for (short n9 = 0; n9 < n2; ++n9) {
            final short n10 = array[n8];
            array2[n5] = (short)((array[n6] << 1) + array[n7] + n10 >> 2);
            array2[n4] = (short)((array[n6] << 1) + array[n7] - (n10 << 2) - n10 - (n10 << 1) >> 2);
            ++n6;
            ++n7;
            ++n8;
            ++n4;
            ++n5;
        }
        int n11 = (n - 2) * n2;
        int n12 = n11 + n2;
        int n13 = (n / 2 - 1) * n3;
        int n14 = n13 - n3;
        int n15 = (n - 1) * n3;
        int n16 = n15 - n3;
        for (short n17 = 0; n17 < n2; ++n17) {
            final short n18 = array[n15];
            array2[n12] = (short)((array[n13] << 1) + n18 >> 2);
            array2[n11] = (short)((array[n13] + array[n14] >> 2) + ((n18 << 1) + array[n16] >> 3) - n18);
            ++n13;
            ++n14;
            ++n15;
            ++n16;
            ++n11;
            ++n12;
        }
        final short n19 = (short)(n / 2 - 1);
        int n20 = n2 * 3;
        int n21 = 2 * n2;
        int n22 = n3;
        int n23 = n3 * (2 + n / 2);
        int n24 = n23 - n3;
        for (short n25 = 1; n25 < n19; ++n25) {
            for (short n26 = 0; n26 < n2; ++n26) {
                final short n27 = (short)((array[n22] << 1) + (array[n23] + array[n24]) >> 2);
                array2[n20] = n27;
                array2[n21] = (short)((array2[n21 - n2] + n27 >> 1) - array[n24]);
                ++n22;
                ++n23;
                ++n24;
                ++n20;
                ++n21;
            }
            n22 += n3 - n2;
            n23 += n3 - n2;
            n24 = n23 - n3;
            n20 += n2;
            n21 += n2;
        }
        final short n28 = (short)(n2 / 2 - 1);
        final short n29 = 0;
        final int n30 = n2 / 2 + 1;
        final int n31 = n30 - 1;
        int n32 = 0;
        int n33 = 0;
        int n34 = 1;
        for (short n35 = 0; n35 < n; ++n35) {
            final short n36 = array2[n32 + n31];
            array[n34] = (short)(array2[n32 + n29] + (array2[n32 + n30] + n36 >> 1));
            array[n33] = (short)(array2[n32 + n29] + (array2[n32 + n30] - n36 >> 1) - n36 - (n36 << 1));
            n33 += n3;
            n34 += n3;
            n32 += n2;
        }
        final int n37 = n2 / 2 - 1;
        final int n38 = n37 - 1;
        final int n39 = n2 - 1;
        final int n40 = n39 - 1;
        int n41 = 0;
        int n42 = n2 - 2;
        int n43 = n42 + 1;
        for (short n44 = 0; n44 < n; ++n44) {
            array[n43] = (short)(array2[n41 + n37] + (array2[n41 + n39] >> 1));
            array[n42] = (short)(((array2[n41 + n37] + array2[n41 + n38] - array2[n41 + n39] << 1) + array2[n41 + n40] >> 2) - array2[n41 + n39]);
            n42 += n3;
            n43 += n3;
            n41 += n2;
        }
        final short n45 = (short)(n2 / 2 - 1);
        int n46 = 0;
        int n47 = 2;
        int n48 = n47 + 1;
        for (short n49 = 0; n49 < n; ++n49) {
            int n50 = n46 + 1;
            int n51 = n46 + n2 / 2 + 2;
            int n52 = n51 - 1;
            short n53 = array[n47 - 1];
            for (short n54 = 1; n54 < n45; ++n54) {
                final short n55 = (short)((array2[n51] + array2[n52] >> 1) + array2[n50]);
                array[n48] = n55;
                array[n47] = (short)((n53 + n55 >> 1) - (array2[n52] << 1));
                ++n50;
                ++n51;
                ++n52;
                n47 += 2;
                n48 += 2;
                n53 = n55;
            }
            n46 += n2;
            n47 += n3 - 2 * n45 + 2;
            n48 = n47 + 1;
        }
        return true;
    }
    
    private boolean WaveletReconsTFast(final short[] array, final short n, final short n2, final short n3) {
        final short[] array2;
        if ((array2 = new short[n * n2]) == null) {
            return false;
        }
        final short n4 = (short)(n / 2 - 1);
        int n5 = n2 * 2;
        int n6 = n5 + n2;
        int n7 = n3;
        int n8 = 0;
        for (short n9 = 1; n9 < n4; ++n9) {
            for (short n10 = 0; n10 < n2; ++n10) {
                array2[n6] = (short)(array[n7] >> 1);
                array2[n5] = (short)(array[n7] + array[n8] >> 2);
                ++n7;
                ++n8;
                ++n5;
                ++n6;
            }
            n7 += n3 - n2;
            n8 = n7 - n3;
            n5 += n2;
            n6 = n5 + n2;
        }
        int n11 = 0;
        int n12 = n2;
        int n13 = 0;
        for (short n14 = 0; n14 < n2; ++n14) {
            array2[n11] = (array2[n12] = (short)(array[n13] >> 1));
            ++n13;
            ++n11;
            ++n12;
        }
        int n15 = (n - 2) * n2;
        int n16 = n15 + n2;
        int n17 = (n / 2 - 1) * n3;
        int n18 = n17 - n3;
        for (short n19 = 0; n19 < n2; ++n19) {
            array2[n16] = (short)(array[n18] >> 1);
            array2[n15] = (short)(array[n17] + array[n18] >> 2);
            ++n17;
            ++n18;
            ++n15;
            ++n16;
        }
        final short n20 = (short)(n2 / 2 - 1);
        int n21 = 0;
        int n22 = 2;
        int n23 = n22 + 1;
        for (short n24 = 0; n24 < n; ++n24) {
            short n25 = 1;
            short n26 = 0;
            for (short n27 = 1; n27 < n20; ++n27) {
                array[n23] = array2[n21 + n25];
                array[n22] = (short)(array2[n21 + n25] + array2[n21 + n26] >> 1);
                ++n25;
                ++n26;
                n22 += 2;
                n23 += 2;
            }
            n21 += n2;
            n22 += n3 - 2 * n20 + 2;
            n23 = n22 + 1;
        }
        final short n28 = 0;
        int n29 = 0;
        int n30 = 0;
        int n31 = 1;
        for (short n32 = 0; n32 < n; ++n32) {
            array[n30] = (array[n31] = array2[n29 + n28]);
            n30 += n3;
            n31 += n3;
            n29 += n2;
        }
        final int n33 = n2 / 2 - 1;
        final int n34 = n33 - 1;
        int n35 = 0;
        int n36 = n2 - 2;
        int n37 = n36 + 1;
        for (short n38 = 0; n38 < n; ++n38) {
            array[n37] = array2[n35 + n33];
            array[n36] = (short)(array2[n35 + n33] + array2[n35 + n34] >> 1);
            n36 += n3;
            n37 += n3;
            n35 += n2;
        }
        return true;
    }
}
