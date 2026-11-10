package lightningstrike;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


/**
 * TODO: add documentation to this class
 *
 * @author rja
 */
public class CCodImage {
	private Image decompressedImage;
	private InputStream inputCodFile;
	private byte compressionType;
	private byte encoderType;
	private byte quantizerType;
	private byte SCB;
	private byte colorTType;
	private CYIQ yiqY;
	private CYIQ yiqI;
	private CYIQ yiqQ;
	private short mean;
	private short width;
	private short height;
	private short bpp;
	private short imageWidth;
	private short imageHeight;
	private short currentWidth;
	private short currentHeight;
	private short waveletLevels;
	private byte[] fileBuf;
	private short colorType;
	private byte[] magicMarker;
	private int codedSize;
	private int[] levelCodeSize;
	private short headerSize;
	private short commentSize;
	private int[] rgbData;
	private int off; // current offset in file
	private int readData;

	/**
	 * Initialize some variables (really needed?)
	 * 
	 * @param pDoc
	 */
	public CCodImage(final InputStream pDoc) {
		this.width = 300;
		this.height = 200;
		this.magicMarker = new byte[8];
		this.yiqY = new CYIQ();
		this.yiqI = new CYIQ();
		this.yiqQ = new CYIQ();
		this.inputCodFile = pDoc;
	}

	private boolean ReadFile() {
		/*
		 * | len | content      |
		 * |-----+--------------|
		 * |   8 | version      |
		 * |   2 | header size  |
		 * |   4 | data size    |
		 * |   2 | comment size |
		 * |   2 | width        |
		 * |   2 | height       |
		 * |   1 | bpp          |
		 * |   1 | color type   |   
		 */
		/* beginning of file: "CIS" + version number ("2.5" or "3.0") */
		if (!Constants.READBYTES(this.inputCodFile, this.magicMarker, 8)) {
			return false;
		}
		/* check version number "3.0" */
		if (this.magicMarker[3] != 51 || this.magicMarker[5] != 48) {
			throw new RuntimeException("Version incompatible!");
		}
		this.headerSize = Constants.READWORD(this.inputCodFile);
		this.codedSize = Constants.READDWORD(this.inputCodFile);
		this.commentSize = Constants.READWORD(this.inputCodFile);
		this.fileBuf = new byte[this.codedSize];
		if (this.fileBuf == null) {
			throw new RuntimeException("No Enough Memory!");
		}
		this.imageWidth = Constants.READWORD(this.inputCodFile);
		this.imageHeight = Constants.READWORD(this.inputCodFile);
		this.bpp = Constants.READONEBYTE(this.inputCodFile);
		this.colorType = Constants.READONEBYTE(this.inputCodFile);
		if (!Constants.SKIP(this.inputCodFile, 2)) {
			return false;
		}
		this.encoderType = Constants.READONEBYTE(this.inputCodFile);
		if (this.encoderType == Constants.ARITHMETIC_LOSSLESS) {
			throw new RuntimeException("This version can not decompress Loss-Less compressed image");
		}
		this.compressionType = Constants.READONEBYTE(this.inputCodFile);
		if (this.compressionType != Constants.BI_WAVELET) {
			return false;
		}
		this.quantizerType = Constants.READONEBYTE(this.inputCodFile);
		if (this.quantizerType != Constants.TABLE_QUANTIZATION) {
			return false;
		}
		this.SCB = Constants.READONEBYTE(this.inputCodFile);
		if (this.SCB != Constants.SISD) { // Sparse Inverse Spectral Decomposition(?)
			return false;
		}
		this.colorTType = Constants.READONEBYTE(this.inputCodFile);
		this.waveletLevels = Constants.READONEBYTE(this.inputCodFile);
		this.levelCodeSize = new int[this.waveletLevels + 1];
		for (int i = 0; i < this.waveletLevels + 1; ++i) {
			this.levelCodeSize[i] = Constants.READDWORD(this.inputCodFile);
		}
		this.AdjustWidthHeight();
		this.yiqY.ReadFile(this.inputCodFile);
		if (this.colorType == Constants.COLOR_IMAGE) {
			this.yiqI.ReadFile(this.inputCodFile);
			this.yiqQ.ReadFile(this.inputCodFile);
		}
		return true;
	}

	/**
	 * @return <code>true</code> when decompression could be initialized
	 */
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

	/**
	 * 
	 */
	public void EndDecompress() {
		this.yiqY.EndDecompress();
		if (this.colorType == 1) {
			this.yiqI.EndDecompress();
			this.yiqQ.EndDecompress();
		}
	}

	/**
	 * 
	 */
	private void AdjustWidthHeight() {
		final short n = (short)(1 << this.waveletLevels);
		this.width = (short)(this.imageWidth + (n - this.imageWidth % n) % n);
		this.height = (short)(this.imageHeight + (n - this.imageHeight % n) % n);
	}

	/**
	 * @param n
	 * @param yiq_y_power_data
	 * @param yiq_i_power_data
	 * @param yiq_q_power_data
	 * @param rgb_image_data
	 * @param image_width
	 * @param image_height
	 */
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
		// The following is equivalent to the conversion:
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

	/**
	 * @return the decompressed image
	 */
	public Image getImage() {
		return this.decompressedImage;
	}

	/**
	 * @return <code>true</code> if start was successful
	 */
	public boolean StartGenerateImage() {
		if (!this.ReadFile()) {
			return false;
		}
		if (this.encoderType == 2) {
			return false;
		}
		this.rgbData = new int[this.width * this.height];
		if (!this.InitDecompress()) {
			return false;
		}
		this.off = 0;
		return true;
	}

	/**
	 * @return <code>true</code> when decompression was successful.
	 */
	public boolean EndGenerateImage() {
		this.EndDecompress();
		return true;
	}

	/**
	 * @param level the level to be generated
	 * @return <code>true</code> if decompression was successful
	 */
	public boolean GenerateImage(final short level) {
		if (level == 0) {
			this.readData = 0;
		}
		if (!Constants.READBYTESWITHOFF(this.inputCodFile, this.fileBuf, this.levelCodeSize[level], this.readData)) {
			return false;
		}
		this.readData += this.levelCodeSize[level];
		this.mean = this.yiqY.mean;
		this.currentWidth = (short)(this.imageWidth >> this.waveletLevels - level);
		this.currentHeight = (short)(this.imageHeight >> this.waveletLevels - level);
		final short n2 = (short)(this.waveletLevels - level);
		if ((this.off = this.yiqY.Decompress(this.fileBuf, this.off, level)) == -1) {
			return false;
		}
		if (this.colorType == Constants.COLOR_IMAGE) {
			if ((this.off = this.yiqI.Decompress(this.fileBuf, this.off, level)) == -1) {
				return false;
			}
			if ((this.off = this.yiqQ.Decompress(this.fileBuf, this.off, level)) == -1) {
				return false;
			}
		}
		this.YiqToRgb(n2, this.yiqY.yiqP, this.yiqI.yiqP, this.yiqQ.yiqP, this.rgbData, this.currentWidth, this.currentHeight);
		this.decompressedImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(this.currentWidth, this.currentHeight, this.rgbData, 0, this.currentWidth));
		return true;
	}

	/**
	 * @return the wavelet levels
	 */
	public short GetLevels() {
		return this.waveletLevels;
	}

	/**
	 * @param args command line arguments
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("usage:");
			System.err.println(CCodImage.class + " <infile>");
			System.exit(1);
		}
		final String inFile = args[0];
		writeJpg(inFile, inFile + "_decoded.jpg");
	}

	public static CCodImage getImage(final String codImage) throws IOException {
		final InputStream in = new BufferedInputStream(new FileInputStream(codImage));
		final CCodImage cod = new CCodImage(in);

		if (cod.StartGenerateImage()) {
			short level = 0;
			for (level = 0; level < cod.GetLevels() + 1; ++level) {
				cod.GenerateImage(level);
			}
			cod.EndGenerateImage();
		}
		in.close();
		return cod;
	}
	/**
	 * @throws IOException
	 */
	public static void writeJpg(final String codImage, final String jpgImage) throws IOException {
		final CCodImage cod = getImage(codImage);
		final BufferedImage bi = new BufferedImage(cod.currentWidth, cod.currentHeight, BufferedImage.TYPE_INT_RGB);
		bi.getGraphics().drawImage(cod.getImage(), 0, 0, null);
		ImageIO.write(bi, "jpg", new File(jpgImage));
	}
	
}
