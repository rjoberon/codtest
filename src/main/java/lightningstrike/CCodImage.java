package lightningstrike;

//import java.awt.image.ImageProducer;
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
            this.errorDialog = new ErrorMessage(this.oneStrike, new String("No enough memory"));
            this.errorDialog.setVisible(true);
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
    	/* beginning of file: "CIS" + version number ("2.5" or "3.0") */
        if (!Constants.READBYTES(inputStream, this.title, 8)) {
            return false;
        }
        /* check version number "3.0" */
        if (this.title[3] != 51 || this.title[5] != 48) {
            this.errorDialog = new ErrorMessage(this.oneStrike, new String("Version incompatible !"));
            this.errorDialog.setVisible(true);
            return false;
        }
        this.headerSize = Constants.READWORD(inputStream);
        this.codedSize = Constants.READDWORD(inputStream);
        this.commentSize = Constants.READWORD(inputStream);
        this.fileBuf = new byte[this.codedSize];
        if (this.fileBuf == null) {
            this.errorDialog = new ErrorMessage(this.oneStrike, new String("No Enough Memory !"));
            this.errorDialog.setVisible(true);
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
            this.errorDialog = new ErrorMessage(this.oneStrike, new String("This version can not decompress Loss-Less compressed image"));
            this.errorDialog.setVisible(true);
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
        if (this.colorType == Constants.COLOR_IMAGE) {
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
        if (this.colorType == Constants.COLOR_IMAGE) {
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
    
    private void YiqToRgb(final short n, final short[] yiq_y_power_data, final short[] yiq_i_power_data, final short[] yiq_q_power_data, final int[] rgb_image_data, final short image_width, final short image_height) {
        // The addition of the line offset means that the yiq data is stored in a row major order
        // (as are most graphics formats if memory serves)
        final int yiq_data_line_offset = this.width - image_width;
        if (this.colorType == Constants.COLOR_IMAGE) {
            int yiq_data_idx = 0;
            for (short image_pos_y = (short)(image_height - 1); image_pos_y >= 0; --image_pos_y) {
                for (short image_pos_x = 0; image_pos_x < image_width; ++image_pos_x) {
                    final int normalized_y_value = (yiq_y_power_data[yiq_data_idx] >> n) + this.mean;
                    final int normalized_i_value = yiq_i_power_data[yiq_data_idx] >> n;
                    final int normalized_q_value = yiq_q_power_data[yiq_data_idx] >> n;
                    // The folowing is equivalent to the conversion:
                    // R = Y + 0.953125*I + 0.62109375*Q
                    // G = Y - 0.2724609375*I - 0.646484375*Q
                    // B = Y - 1.106445313*I + 1.703125*Q
                    final int r_value = normalized_y_value + ((normalized_i_value * 979 + normalized_q_value * 636) >> 10);
                    final int g_value = normalized_y_value + ((normalized_i_value * -279 + normalized_q_value * -662) >> 10);
                    final int b_value = normalized_y_value + ((normalized_i_value * -1133 + normalized_q_value * 1744) >> 10);
                    rgb_image_data[image_pos_y * image_width + image_pos_x] = (0xFF000000 | ((r_value < 0) ? 0 : ((r_value > 255) ? 255 : r_value)) << 16 | ((g_value < 0) ? 0 : ((g_value > 255) ? 255 : g_value)) << 8 | ((b_value < 0) ? 0 : ((b_value > 255) ? 255 : b_value)));
                    ++yiq_data_idx;
                }
                yiq_data_idx += yiq_data_line_offset;
            }
            return;
        }
//        // I somehow feel that another check is missing here
//        else if (this.colorType == Constants.GRAY_IMAGE){
        else {
            int yiq_data_idx = 0;
            for (short image_pos_y = (short)(image_height - 1); image_pos_y >= 0; --image_pos_y) {
                for (short image_pos_x = 0; image_pos_x < image_width; ++image_pos_x) {
                    final int normalized_y_value = (yiq_y_power_data[yiq_data_idx] >> n) + this.mean;
                    final int gray_rgb_value = (normalized_y_value < 0) ? 0 : ((normalized_y_value > 255) ? 255 : normalized_y_value);
                    rgb_image_data[image_pos_y * image_width + image_pos_x] = (0xFF000000 | gray_rgb_value << 16 | gray_rgb_value << 8 | gray_rgb_value);
                    ++yiq_data_idx;
                }
                yiq_data_idx += yiq_data_line_offset;
            }
        }
//        else {
//            throw new IllegalArgumentException("Invalid color type");
//        }
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
        if (this.colorType == Constants.COLOR_IMAGE) {
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
        if (this.colorType == Constants.COLOR_IMAGE) {
            if ((this.off = this.yiqI.Decompress(fileBuf, this.off, n)) == -1) {
                return false;
            }
            if ((this.off = this.yiqQ.Decompress(fileBuf, this.off, n)) == -1) {
                return false;
            }
        }
        this.YiqToRgb(n2, this.yiqY.yiqP, this.yiqI.yiqP, this.yiqQ.yiqP, this.rgbData, this.currentWidth, this.currentHeight);
//        final int n3 = this.currentWidth * this.currentHeight; //unused variable
        this.decompressedImage = null;
        this.decompressedImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(this.currentWidth, this.currentHeight, this.rgbData, 0, this.currentWidth));
        return true;
    }
    
    public short GetLevels() {
        return this.waveletLevels;
    }
}
