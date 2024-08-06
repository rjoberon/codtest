import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.util.Date;
import java.io.InputStream;
import java.awt.Image;

// 
// Decompiled by Procyon v0.6.0
// 

public class CCodImage extends Constants
{
    public Image decompressedImage;
    public InputStream m_pDoc;
    public InputStream m_pDocAnother;
    public byte compressionType;
    public byte encoderType;
    public byte quantizerType;
    public byte SCB;
    public byte colorTType;
    public CYIQ yiqY;
    public CYIQ yiqI;
    public CYIQ yiqQ;
    public short mean;
    public short width;
    public short height;
    public short bpp;
    public short imageWidth;
    public short imageHeight;
    public short currentWidth;
    public short currentHeight;
    public short waveletLevels;
    public byte[] fileBuf;
    public byte[] lpCurr;
    public short colorType;
    public boolean ifIncrDisp;
    public boolean ifUseMask;
    public short headerLength;
    public short commentLength;
    public byte[] comments;
    public byte[] title;
    public byte[] version;
    public short qualityFactor;
    public short colorFactor;
    public short imageType;
    public int codedSize;
    public int[] levelCodeSize;
    public short headerSize;
    public short commentSize;
    public int[] rgbData;
    public int[] tempRgbData;
    public int off;
    private String fileName;
    private long startTime;
    private long endTime;
    private float decompTime;
    private Strike oneStrike;
    private ErrorMessage errorDialog;
    public boolean incremental;
    private Date myTime;
    private int readData;
    
    public CCodImage(final Strike oneStrike, final InputStream pDoc, final InputStream pDocAnother, final String fileName) {
        this.width = 300;
        this.height = 200;
        this.imageWidth = 300;
        this.imageHeight = 200;
        this.incremental = true;
        this.title = new byte[8];
        this.version = new byte[3];
        this.oneStrike = oneStrike;
        try {
            this.yiqY = new CYIQ();
            this.yiqI = new CYIQ();
            this.yiqQ = new CYIQ();
        }
        catch (final Throwable t) {
            (this.errorDialog = new ErrorMessage(this.oneStrike, new String("No enough memory"))).show();
        }
        this.fileName = fileName;
        this.m_pDoc = pDoc;
        this.m_pDocAnother = pDocAnother;
        this.imageType = 0;
        this.comments = new byte[4096];
        this.compressionType = 1;
        this.encoderType = 1;
        this.SCB = 1;
        this.quantizerType = 1;
        this.colorTType = 1;
        this.fileBuf = null;
        this.codedSize = 0;
        this.waveletLevels = 5;
    }
    
    public void Generate(final Strike oneStrike, final InputStream pDoc, final InputStream pDocAnother, final String fileName) {
        this.fileName = fileName;
        this.m_pDoc = pDoc;
        this.m_pDocAnother = pDocAnother;
        this.imageType = 0;
        this.oneStrike = oneStrike;
        this.comments = new byte[4096];
        this.compressionType = 1;
        this.encoderType = 1;
        this.SCB = 1;
        this.quantizerType = 1;
        this.colorTType = 1;
        this.fileBuf = null;
        this.codedSize = 0;
        this.waveletLevels = 5;
    }
    
    public boolean ReadFile(final InputStream inputStream, final InputStream inputStream2) {
        if (!Constants.READBYTES(inputStream, this.title, 8)) {
            return false;
        }
        if (this.title[3] != 51 || this.title[5] != 48) {
            (this.errorDialog = new ErrorMessage(this.oneStrike, new String("Version incompatible !"))).show();
            return false;
        }
        this.headerSize = Constants.READWORD(inputStream);
        this.codedSize = Constants.READDWORD(inputStream);
        this.commentSize = Constants.READWORD(inputStream);
        this.fileBuf = new byte[this.codedSize];
        if (this.fileBuf == null) {
            (this.errorDialog = new ErrorMessage(this.oneStrike, new String("No Enough Memory !"))).show();
            return false;
        }
        this.imageWidth = Constants.READWORD(inputStream);
        this.imageHeight = Constants.READWORD(inputStream);
        this.bpp = Constants.READONEBYTE(inputStream);
        this.colorType = Constants.READONEBYTE(inputStream);
        if (!Constants.SKIP(inputStream, 2)) {
            return false;
        }
        this.encoderType = Constants.READONEBYTE(inputStream);
        if (this.encoderType == 2) {
            (this.errorDialog = new ErrorMessage(this.oneStrike, new String("This version can not decompress Loss-Less compressed image"))).show();
            return false;
        }
        this.compressionType = Constants.READONEBYTE(inputStream);
        if (this.compressionType != 1) {
            return false;
        }
        this.quantizerType = Constants.READONEBYTE(inputStream);
        if (this.quantizerType != 1) {
            return false;
        }
        this.SCB = Constants.READONEBYTE(inputStream);
        if (this.SCB != 1) {
            return false;
        }
        this.colorTType = Constants.READONEBYTE(inputStream);
        this.waveletLevels = Constants.READONEBYTE(inputStream);
        this.levelCodeSize = new int[this.waveletLevels + 1];
        for (int i = 0; i < this.waveletLevels + 1; ++i) {
            this.levelCodeSize[i] = Constants.READDWORD(inputStream);
        }
        this.AdjustWidthHeight();
        this.yiqY.ReadFile(inputStream);
        if (this.colorType == 1) {
            this.yiqI.ReadFile(inputStream);
            this.yiqQ.ReadFile(inputStream);
        }
        return Constants.SKIP(inputStream2, this.headerSize);
    }
    
    public boolean InitDecompress() {
        this.AdjustWidthHeight();
        if (!this.yiqY.InitDecompress(this.width, this.height, this.waveletLevels)) {
            return false;
        }
        if (this.colorType == 1) {
            if (!this.yiqI.InitDecompress(this.width, this.height, this.waveletLevels)) {
                return false;
            }
            if (!this.yiqQ.InitDecompress(this.width, this.height, this.waveletLevels)) {
                return false;
            }
        }
        return true;
    }
    
    public void EndDecompress() {
        this.yiqY.EndDecompress();
        if (this.colorType == 1) {
            this.yiqI.EndDecompress();
            this.yiqQ.EndDecompress();
        }
    }
    
    public void AdjustWidthHeight() {
        final short n = (short)(1 << this.waveletLevels);
        this.width = (short)(this.imageWidth + (n - this.imageWidth % n) % n);
        this.height = (short)(this.imageHeight + (n - this.imageHeight % n) % n);
    }
    
    private void YiqToRgb(final short n, final short[] array, final short[] array2, final short[] array3, final int[] array4, final short n2, final short n3) {
        int n4 = 0;
        if (this.colorType == 1) {
            for (short n5 = (short)(n3 - 1); n5 >= 0; --n5) {
                for (short n6 = 0; n6 < n2; ++n6) {
                    final int n7 = (array[n4] >> n) + this.mean;
                    final int n8 = array3[n4] >> n;
                    final int n9 = array2[n4] >> n;
                    final int n10 = (n9 * 979 + n8 * 636 >> 10) + n7;
                    final int n11 = (n9 * -279 + n8 * -662 >> 10) + n7;
                    final int n12 = (n9 * -1133 + n8 * 1744 >> 10) + n7;
                    array4[n5 * n2 + n6] = (0xFF000000 | ((n10 < 0) ? 0 : ((n10 > 255) ? 255 : n10)) << 16 | ((n11 < 0) ? 0 : ((n11 > 255) ? 255 : n11)) << 8 | ((n12 < 0) ? 0 : ((n12 > 255) ? 255 : n12)));
                    ++n4;
                }
                n4 += this.width - n2;
            }
            return;
        }
        int n13 = 0;
        for (short n14 = (short)(n3 - 1); n14 >= 0; --n14) {
            for (short n15 = 0; n15 < n2; ++n15) {
                final int n16 = (array[n13] >> n) + this.mean;
                final int n17 = (n16 < 0) ? 0 : ((n16 > 255) ? 255 : n16);
                array4[n14 * n2 + n15] = (0xFF000000 | n17 << 16 | n17 << 8 | n17);
                ++n13;
            }
            n13 += this.width - n2;
        }
    }
    
    public Image getImage() {
        return this.decompressedImage;
    }
    
    public Dimension getImageSize() {
        return new Dimension(this.imageWidth, this.imageHeight);
    }
    
    public Properties getProperties() {
        final Properties properties = new Properties();
        properties.fileName = this.fileName;
        properties.fileSize = this.codedSize + this.headerSize;
        if (this.colorType == 1) {
            properties.color = true;
        }
        else {
            properties.color = false;
        }
        properties.width = this.imageWidth;
        properties.height = this.imageHeight;
        properties.decompressTime = this.decompTime;
        return properties;
    }
    
    public boolean StartGenerateImage() {
        if (!this.ReadFile(this.m_pDoc, this.m_pDocAnother)) {
            return false;
        }
        if (this.encoderType == 2) {
            return false;
        }
        this.rgbData = null;
        this.rgbData = new int[this.width * this.height];
        if (this.rgbData == null) {
            return false;
        }
        this.myTime = new Date();
        this.startTime = this.myTime.getTime();
        if (!this.InitDecompress()) {
            return false;
        }
        this.off = 0;
        return true;
    }
    
    public boolean EndGenerateImage() {
        this.EndDecompress();
        this.myTime = new Date();
        this.endTime = this.myTime.getTime();
        this.decompTime = (this.endTime - this.startTime) / 1000.0f;
        return true;
    }
    
    public boolean GenerateImage(final short n) {
        if (n == 0) {
            this.readData = 0;
        }
        if (!Constants.READBYTESWITHOFF(this.m_pDocAnother, this.fileBuf, this.levelCodeSize[n], this.readData)) {
            return false;
        }
        this.readData += this.levelCodeSize[n];
        this.mean = this.yiqY.mean;
        final byte[] fileBuf = this.fileBuf;
        this.currentWidth = (short)(this.imageWidth >> this.waveletLevels - n);
        this.currentHeight = (short)(this.imageHeight >> this.waveletLevels - n);
        final short n2 = (short)(this.waveletLevels - n);
        if ((this.off = this.yiqY.Decompress(fileBuf, this.off, n)) == -1) {
            return false;
        }
        if (this.colorType == 1) {
            if ((this.off = this.yiqI.Decompress(fileBuf, this.off, n)) == -1) {
                return false;
            }
            if ((this.off = this.yiqQ.Decompress(fileBuf, this.off, n)) == -1) {
                return false;
            }
        }
        this.YiqToRgb(n2, this.yiqY.yiqP, this.yiqI.yiqP, this.yiqQ.yiqP, this.rgbData, this.currentWidth, this.currentHeight);
        final int n3 = this.currentWidth * this.currentHeight;
        this.decompressedImage = null;
        this.decompressedImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(this.currentWidth, this.currentHeight, this.rgbData, 0, this.currentWidth));
        return true;
    }
    
    public short GetLevels() {
        return this.waveletLevels;
    }
}
