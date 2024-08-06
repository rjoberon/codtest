import java.awt.Rectangle;
import java.io.FileInputStream;
import java.awt.Event;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.Component;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;
import java.io.InputStream;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.MenuItem;
import java.awt.FileDialog;
import java.awt.Frame;

// 
// Decompiled by Procyon v0.6.0
// 

public class Strike extends Frame
{
    ImageCanvas imageArea;
    private Lightning parent;
    private String fileName;
    private FileDialog openDialog;
    private FileDialog saveDialog;
    private MenuItem openMenuItem;
    private MenuItem propertyMenuItem;
    private MenuItem aboutMenuItem;
    private MenuItem exitMenuItem;
    private MenuItem stretchUpMenuItem;
    private MenuItem stretchDownMenuItem;
    private MenuItem normalMenuItem;
    private MenuItem doubleSizeMenuItem;
    private MenuItem halfSizeMenuItem;
    private MenuItem byteStreamMenuItem;
    private MenuItem refreshMenuItem;
    private MenuItem redecompMenuItem;
    private AboutDialog aboutDialog;
    private ErrorMessage errorDialog;
    private propertyDialog myDialog;
    private Toolkit strikeToolkit;
    private Image memoryImage;
    private Image[] manyImages;
    private Image firstImage;
    private static boolean first;
    private InputStream inputCODFile;
    private int[] memoryData;
    private int count;
    private boolean firstTime;
    public CCodImage oneCodImage;
    public URL tempURL;
    public URL tempURLAnother;
    public InputStream tempFile;
    public InputStream tempFileAnother;
    public int X;
    public int Y;
    public int Width;
    public int Height;
    public int displayWidth;
    public int displayHeight;
    public Dimension imageSize;
    public boolean progressiveDisplay;
    private Properties imageProperties;
    private boolean isApplet;
    private String fullDirName;
    private String[] fileList;
    public boolean isDecompressing;
    private winSelector select;
    private boolean interuptNow;
    public Bargauge gauge;
    
    public Strike() {
        super("Lightning Strike Image Gallery 1.1 - Oct. 1996");
        this.progressiveDisplay = false;
        this.isDecompressing = false;
        this.interuptNow = false;
        this.setBackground(Color.gray);
        this.setFont(new Font("TimesRoman", 0, 12));
        this.isApplet = false;
        this.CreateMenus();
        this.CreateImageArea();
        this.X = 200;
        this.Y = 150;
        this.Width = 450;
        this.Height = 450;
        this.firstTime = true;
        this.move(this.X, this.Y);
        this.resize(this.Width, this.Height);
        this.show();
    }
    
    public Strike(final Lightning parent, final String fullDirName, final String[] fileList) {
        super("Lightning Strike Image Gallery 1.1 - Oct. 1996");
        this.progressiveDisplay = false;
        this.isDecompressing = false;
        this.interuptNow = false;
        this.setBackground(Color.gray);
        this.setFont(new Font("TimesRoman", 0, 12));
        this.isApplet = true;
        this.parent = parent;
        this.fullDirName = fullDirName;
        this.fileList = fileList;
        this.CreateMenus();
        this.CreateImageArea();
        this.X = 200;
        this.Y = 150;
        this.Width = 450;
        this.Height = 450;
        this.firstTime = true;
        this.move(this.X, this.Y);
        this.resize(this.Width, this.Height);
        this.show();
    }
    
    public void reShow() {
        this.move(this.X, this.Y);
        this.resize(this.Width, this.Height);
        this.show();
    }
    
    private void CreateMenus() {
        final MenuBar menuBar = new MenuBar();
        final Menu m = new Menu("File", false);
        if (!this.isApplet) {
            m.add(this.openMenuItem = new MenuItem("Open"));
        }
        else {
            this.openMenuItem = null;
        }
        m.add(this.propertyMenuItem = new MenuItem("Properties"));
        m.addSeparator();
        m.add(this.exitMenuItem = new MenuItem("Exit"));
        menuBar.add(m);
        final Menu i = new Menu("Display");
        i.add(this.refreshMenuItem = new MenuItem("Refresh"));
        i.addSeparator();
        i.add(this.stretchUpMenuItem = new MenuItem("Zoom In (max 3096 pix.)"));
        i.add(this.stretchDownMenuItem = new MenuItem("Zoom Out"));
        i.addSeparator();
        i.add(this.normalMenuItem = new MenuItem("Normal"));
        i.addSeparator();
        i.add(this.doubleSizeMenuItem = new MenuItem("Double Size (max 3096 pix.)"));
        i.add(this.halfSizeMenuItem = new MenuItem("Half Size"));
        menuBar.add(i);
        final Menu j = new Menu("About");
        j.add(this.aboutMenuItem = new MenuItem("About Lightning Strike"));
        menuBar.add(j);
        this.setMenuBar(menuBar);
    }
    
    public void incrDisplay(final Image image) {
        this.imageArea.setImage(image);
        this.imageArea.repaint();
    }
    
    private void CreateImageArea() {
        this.add("Center", this.imageArea = new ImageCanvas(this.memoryImage, this));
        this.gauge = new Bargauge(Color.blue);
        if (this.isApplet) {
            final Panel comp = new Panel();
            comp.setLayout(new BorderLayout());
            this.add("South", comp);
            comp.add("North", this.gauge);
            comp.add("Center", this.select = new winSelector(3, this.fileList));
        }
        else {
            this.add("South", this.gauge);
            this.select = null;
        }
        this.show();
        this.gauge.setFillPercent(0.0);
        this.gauge.fill();
    }
    
    private void CreateDialogs() {
    }
    
    public boolean handleEvent(final Event event) {
        switch (event.id) {
            case 201: {
                if (!this.isDecompressing) {
                    if (this.isApplet) {
                        this.parent.displayNow = false;
                    }
                    this.hide();
                    this.dispose();
                    if (!this.isApplet) {
                        System.exit(0);
                    }
                }
                return false;
            }
            case 1001: {
                if (event.target instanceof MenuItem) {
                    if (event.target == this.openMenuItem) {
                        this.propertyMenuItem.disable();
                        this.aboutMenuItem.disable();
                        this.exitMenuItem.disable();
                        this.memoryImage = null;
                        if (!this.isApplet) {
                            this.openFile();
                        }
                        this.propertyMenuItem.enable();
                        this.aboutMenuItem.enable();
                        this.exitMenuItem.enable();
                    }
                    else {
                        if (event.target == this.propertyMenuItem && !this.isDecompressing && this.memoryImage != null) {
                            if (this.myDialog == null) {
                                if (this.oneCodImage != null) {
                                    this.imageProperties = this.oneCodImage.getProperties();
                                    (this.myDialog = new propertyDialog(this, new String("Decompressed Image Properties"), this.imageProperties.fileName, this.imageProperties.fileSize, this.imageProperties.color, this.imageProperties.width, this.imageProperties.height, this.imageProperties.decompressTime)).show();
                                }
                            }
                            else if (this.myDialog.displayNow) {
                                this.myDialog.toFront();
                            }
                            else {
                                this.myDialog = null;
                                this.imageProperties = this.oneCodImage.getProperties();
                                (this.myDialog = new propertyDialog(this, new String("Decompressed Image Properties"), this.imageProperties.fileName, this.imageProperties.fileSize, this.imageProperties.color, this.imageProperties.width, this.imageProperties.height, this.imageProperties.decompressTime)).show();
                            }
                            return true;
                        }
                        if (event.target == this.exitMenuItem) {
                            if (!this.isDecompressing) {
                                if (this.isApplet) {
                                    this.parent.displayNow = false;
                                }
                                this.hide();
                                this.dispose();
                                if (!this.isApplet) {
                                    System.exit(0);
                                }
                            }
                            return false;
                        }
                        if (event.target == this.stretchUpMenuItem) {
                            if (this.displayWidth != 0 && this.displayHeight != 0 && this.displayWidth + (int)(0.1 * this.displayWidth) <= 3096 && this.displayHeight + (int)(0.1 * this.displayHeight) <= 3096) {
                                this.displayWidth += (int)(0.1 * this.displayWidth);
                                this.displayHeight += (int)(0.1 * this.displayHeight);
                                this.imageArea.repaint();
                            }
                            return true;
                        }
                        if (event.target == this.stretchDownMenuItem) {
                            if (this.displayWidth != 0 && this.displayHeight != 0) {
                                this.displayWidth -= (int)(0.1 * this.displayWidth);
                                this.displayHeight -= (int)(0.1 * this.displayHeight);
                                if (this.displayWidth <= 0) {
                                    this.displayWidth = 1;
                                }
                                if (this.displayHeight <= 0) {
                                    this.displayHeight = 1;
                                }
                                this.imageArea.repaint();
                            }
                            return true;
                        }
                        if (event.target == this.refreshMenuItem) {
                            this.imageArea.repaint();
                            return true;
                        }
                        if (event.target == this.normalMenuItem) {
                            if (this.imageSize != null) {
                                this.displayWidth = this.imageSize.width;
                                this.displayHeight = this.imageSize.height;
                                this.imageArea.repaint();
                            }
                            else {
                                this.displayWidth = 0;
                                this.displayHeight = 0;
                            }
                            return true;
                        }
                        if (event.target == this.doubleSizeMenuItem) {
                            if (this.displayWidth != 0 && this.displayHeight != 0 && 2 * this.displayWidth <= 3096 && 2 * this.displayHeight <= 3096) {
                                this.displayWidth *= 2;
                                this.displayHeight *= 2;
                                this.imageArea.repaint();
                            }
                            return true;
                        }
                        if (event.target == this.halfSizeMenuItem) {
                            if (this.displayWidth != 0 && this.displayHeight != 0) {
                                this.displayWidth *= (int)0.5;
                                this.displayHeight *= (int)0.5;
                                this.imageArea.repaint();
                            }
                            return true;
                        }
                        if (event.target == this.byteStreamMenuItem) {
                            if (this.progressiveDisplay) {
                                this.progressiveDisplay = false;
                            }
                            else {
                                this.progressiveDisplay = true;
                            }
                            return true;
                        }
                        if (event.target == this.aboutMenuItem) {
                            if (this.aboutDialog == null) {
                                (this.aboutDialog = new AboutDialog(this)).show();
                            }
                            else if (this.aboutDialog.displayNow) {
                                this.aboutDialog.toFront();
                            }
                            else {
                                (this.aboutDialog = new AboutDialog(this)).show();
                            }
                            return true;
                        }
                    }
                }
                else if (event.target == this.select && !this.isDecompressing) {
                    final String str = new String(this.fullDirName + this.select.getSelectedItem());
                    System.out.println("Select = " + str);
                    this.openRemoteFile(str);
                }
                return true;
            }
            case 403: {
                if (event.key == 1010) {
                    new DesignDialog(this, "Designed by Dehua KANG, June 1996").show();
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private void openFile() {
        this.openDialog = null;
        (this.openDialog = new FileDialog(this, "Open File")).setFile(new String("*.cod"));
        this.openDialog.show();
        final String file = this.openDialog.getFile();
        if (file != null) {
            final String concat = this.openDialog.getDirectory().concat(file);
            try {
                this.gauge.setFillPercent(0.0);
                this.gauge.fill();
                this.isDecompressing = true;
                this.tempFile = new FileInputStream(concat);
                this.tempFileAnother = new FileInputStream(concat);
                if (this.firstTime) {
                    this.oneCodImage = new CCodImage(this, this.tempFile, this.tempFileAnother, concat);
                    this.firstTime = true;
                }
                else {
                    this.oneCodImage.Generate(this, this.tempFile, this.tempFileAnother, concat);
                }
                if (this.oneCodImage.StartGenerateImage()) {
                    for (short getLevels = this.oneCodImage.GetLevels(), n = 0; n < getLevels + 1; ++n) {
                        if (n != getLevels) {
                            this.gauge.setFillPercent(n / (double)getLevels * 100.0);
                            this.gauge.fill();
                        }
                        this.oneCodImage.GenerateImage(n);
                        this.memoryImage = this.oneCodImage.getImage();
                        if (n == 0) {
                            this.imageSize = this.oneCodImage.getImageSize();
                            this.displayWidth = this.imageSize.width;
                            this.displayHeight = this.imageSize.height;
                            final Rectangle bounds = this.imageArea.bounds();
                            this.Width += this.imageSize.width - bounds.width;
                            this.Height += this.imageSize.height - bounds.height;
                            if (this.Width <= 100) {
                                this.Width = 100;
                            }
                            if (this.Height <= 100) {
                                this.Height = 100;
                            }
                        }
                        this.imageArea.setImage(this.memoryImage);
                        this.imageArea.paint(this.imageArea.getGraphics());
                    }
                    this.interuptNow = false;
                    this.oneCodImage.EndGenerateImage();
                }
                this.tempFile.close();
                this.tempFileAnother.close();
                this.gauge.setFillPercent(100.0);
                this.gauge.fill();
            }
            catch (final Throwable t) {
                this.isDecompressing = false;
                (this.errorDialog = new ErrorMessage(this, new String("open and read cod file error ! mainly because of memory and swap space !"))).show();
            }
        }
        this.openDialog.hide();
        this.openDialog.dispose();
        this.isDecompressing = false;
    }
    
    private void openRemoteFile(final String s) {
        try {
            this.gauge.setFillPercent(0.0);
            this.gauge.fill();
            this.isDecompressing = true;
            this.tempURL = null;
            this.tempURLAnother = null;
            this.tempURL = new URL(s);
            this.tempURLAnother = new URL(s);
            this.tempFile = this.tempURL.openStream();
            this.tempFileAnother = this.tempURLAnother.openStream();
            if (this.firstTime) {
                this.oneCodImage = new CCodImage(this, this.tempFile, this.tempFileAnother, s);
                this.firstTime = true;
            }
            else {
                this.oneCodImage.Generate(this, this.tempFile, this.tempFileAnother, s);
            }
            if (this.oneCodImage.StartGenerateImage()) {
                for (short getLevels = this.oneCodImage.GetLevels(), n = 0; n < getLevels + 1; ++n) {
                    if (n != getLevels) {
                        this.gauge.setFillPercent(n / (double)getLevels * 100.0);
                        this.gauge.fill();
                    }
                    if (!this.oneCodImage.GenerateImage(n)) {
                        break;
                    }
                    this.memoryImage = this.oneCodImage.getImage();
                    if (n == 0) {
                        this.imageSize = this.oneCodImage.getImageSize();
                        this.displayWidth = this.imageSize.width;
                        this.displayHeight = this.imageSize.height;
                        final Rectangle bounds = this.imageArea.bounds();
                        this.Width += this.imageSize.width - bounds.width;
                        this.Height += this.imageSize.height - bounds.height;
                        if (this.Width <= 100) {
                            this.Width = 100;
                        }
                        if (this.Height <= 100) {
                            this.Height = 100;
                        }
                    }
                    this.imageArea.setImage(this.memoryImage);
                    this.imageArea.paint(this.imageArea.getGraphics());
                }
                this.oneCodImage.EndGenerateImage();
            }
            this.gauge.setFillPercent(100.0);
            this.gauge.fill();
            this.tempFile.close();
            this.tempFileAnother.close();
        }
        catch (final Throwable t) {
            this.isDecompressing = false;
            (this.errorDialog = new ErrorMessage(this, new String("open and read cod file error ! mainly because of memory and swap space !"))).show();
            this.isDecompressing = false;
        }
        this.isDecompressing = false;
    }
    
    static {
        Strike.first = true;
    }
}
