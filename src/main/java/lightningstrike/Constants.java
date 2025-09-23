package lightningstrike;

import java.io.InputStream;

// 
// Decompiled by Procyon v0.6.0
// 

public class Constants
{
    public static final int MAX_LEVELS = 10;
    public static final int MAX_SUBBANDS = 31;
    public static final int VERT_BAND = 0;
    public static final int HOR_BAND = 1;
    public static final int DIAG_BAND = 2;
    public static final int ZEROTREENODE = -126;
    public static final short GRAY_IMAGE = 0;
    public static final short COLOR_IMAGE = 1;
    public static final short GRAY8 = 611;
    public static final short COLOR24 = 62;
    public static final short COLOR_8bit = 10;
    public static final byte BMP = 1;
    public static final byte COD = 2;
    public static final byte TGA = 3;
    public static final byte GIF = 4;
    public static final byte JPG = 5;
    public static final byte BI_WAVELET = 1;
    public static final byte ARITHMETIC = 1;
    public static final byte ARITHMETIC_LOSSLESS = 2;
    public static final byte SISD = 1; //A Guess on possible meaning? Simplest Integer Spectral Decomposition(?) (From the paper §2)
    public static final byte TABLE_QUANTIZATION = 1;
    public static final byte YIQ_RGB = 1;
    public static final byte YUV_RGB = 2;
    public static final byte YCbCr = 3;
    public static final byte BEGIN = 0;
    public static final byte CURRENT = 1;
    public int fileResetLimit;
    
    public static short GETWORD(final byte[] array, final int n) {
        final int intermediate = (0xFF & array[n + 1]) << 8 | (0xFF & array[n]);
		final short result = (short)intermediate;
		return result;
    }
    
    public static byte READONEBYTE(final InputStream inputStream) {
        final byte[] b = { 0 };
        int i = 0;
        try {
            while (i == 0) {
                i = inputStream.read(b, 0, 1);
            }
        }
        catch (final Throwable t) {}
        return b[0];
    }
    
    public static boolean READBYTES(final InputStream inputStream, final byte[] b, final int n) {
        int i = n;
        int off = 0;
        try {
            while (i > 0) {
                final int read = inputStream.read(b, off, i);
                off += read;
                i -= read;
            }
        }
        catch (final Throwable t) {
            return false;
        }
        return true;
    }
    
    public static boolean READBYTESWITHOFF(final InputStream inputStream, final byte[] b, final int n, final int n2) {
        int i = n;
        int off = n2;
        try {
            while (i > 0) {
                final int read = inputStream.read(b, off, i);
                off += read;
                i -= read;
            }
        }
        catch (final Throwable t) {
            return false;
        }
        return true;
    }
    
    public static short READWORD(final InputStream inputStream) {
        final byte[] array = new byte[2];
        READBYTES(inputStream, array, 2);
        return (short)((0xFF & array[1]) << 8 | (0xFF & array[0]));
    }
    
    public static int READDWORD(final InputStream inputStream) {
        final byte[] array = new byte[4];
        READBYTES(inputStream, array, 4);
        // rja, 2025-09-23: removed cast to short since it produced negative values (→ data sizes)
		return (0xFF & array[3]) << 24 | (0xFF & array[2]) << 16 | (0xFF & array[1]) << 8 | (0xFF & array[0]);
    }
    
    public static boolean SEEK(final InputStream inputStream, final int n, final byte b) {
        try {
            if (b == 0) {
                inputStream.reset();
            }
            for (int i = n; i > 0; i -= (int)inputStream.skip(i)) {}
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }
    
    public static boolean SKIP(final InputStream inputStream, final int n) {
        try {
            for (int i = n; i > 0; i -= (int)inputStream.skip(i)) {}
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }
}
