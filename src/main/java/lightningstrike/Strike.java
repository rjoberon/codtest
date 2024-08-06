package lightningstrike;

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


import java.awt.event.WindowListener;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// 
// Decompiled by Procyon v0.6.0
// 

public class Strike extends Frame implements ActionListener, WindowListener
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

        // Constructor code...
        this.addWindowListener(this);
        // Add ActionListeners to menu items
 

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
        this.setSize(this.Width, this.Height);
        this.setVisible(true);
    }
    
    public Strike(final Lightning parent, final String fullDirName, final String[] fileList) {
        super("Lightning Strike Image Gallery 1.1 - Oct. 1996");
        // Constructor code...
        this.addWindowListener(this);
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
        this.setLocation(this.X, this.Y);
        this.setSize(this.Width, this.Height);
        this.setVisible(true);
    }
    
    public void reShow() {
        this.setLocation(this.X, this.Y);
        this.setSize(this.Width, this.Height);
        this.setVisible(true);
    }
    
    private void CreateMenus() {
        final MenuBar menuBar = new MenuBar();
        final Menu m = new Menu("File", false);
        if (!this.isApplet) {
            m.add(this.openMenuItem = new MenuItem("Open"));
            this.openMenuItem.addActionListener(this);
        }
        else {
            this.openMenuItem = null;
        }
        m.add(this.propertyMenuItem = new MenuItem("Properties"));
        this.propertyMenuItem.addActionListener(this);
        m.addSeparator();
        m.add(this.exitMenuItem = new MenuItem("Exit"));
        this.exitMenuItem.addActionListener(this);
        menuBar.add(m);
        final Menu i = new Menu("Display");
        i.add(this.refreshMenuItem = new MenuItem("Refresh"));
        this.refreshMenuItem.addActionListener(this);

        i.addSeparator();
        i.add(this.stretchUpMenuItem = new MenuItem("Zoom In (max 3096 pix.)"));
        this.stretchUpMenuItem.addActionListener(this);
        i.add(this.stretchDownMenuItem = new MenuItem("Zoom Out"));
        this.stretchDownMenuItem.addActionListener(this);
        i.addSeparator();
        i.add(this.normalMenuItem = new MenuItem("Normal"));
        this.normalMenuItem.addActionListener(this);
        i.addSeparator();
        i.add(this.doubleSizeMenuItem = new MenuItem("Double Size (max 3096 pix.)"));
        this.doubleSizeMenuItem.addActionListener(this);
        i.add(this.halfSizeMenuItem = new MenuItem("Half Size"));
        this.halfSizeMenuItem.addActionListener(this);
        menuBar.add(i);
        final Menu j = new Menu("About");
        j.add(this.aboutMenuItem = new MenuItem("About Lightning Strike"));
        this.aboutMenuItem.addActionListener(this);
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
            this.select.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (!isDecompressing) {
                        String str = fullDirName + select.getSelectedItem();
                        System.out.println("Select = " + str);
                        openRemoteFile(str);
                    }
                }
            });
        }
        else {
            this.add("South", this.gauge);
            this.select = null;
        }
        this.setVisible(true);
        this.gauge.setFillPercent(0.0);
        this.gauge.fill();
    }
    
//    private void CreateDialogs() {
//   }
    
 

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.openMenuItem) {
            this.propertyMenuItem.setEnabled(false);
            this.aboutMenuItem.setEnabled(false);
            this.exitMenuItem.setEnabled(false);
            this.memoryImage = null;
            if (!this.isApplet) {
                this.openFile();
            }
            this.propertyMenuItem.setEnabled(true);
            this.aboutMenuItem.setEnabled(true);
            this.exitMenuItem.setEnabled(true);
        } else if (source == this.propertyMenuItem && !this.isDecompressing && this.memoryImage != null) {
            if (this.myDialog == null) {
                if (this.oneCodImage != null) {
                    this.imageProperties = this.oneCodImage.getProperties();
                    this.myDialog = new propertyDialog(this, "Decompressed Image Properties", this.imageProperties.fileName, this.imageProperties.fileSize, this.imageProperties.color, this.imageProperties.width, this.imageProperties.height, this.imageProperties.decompressTime);
                    this.myDialog.setVisible(true);
                }
            } else if (this.myDialog.isVisible()) {
                this.myDialog.toFront();
            }
        } else if (source == this.exitMenuItem) {
            if (!this.isDecompressing) {
                this.setVisible(false);
                this.dispose();
                System.exit(0);
            }
        } else if (source == this.stretchUpMenuItem) {
            if (this.displayWidth != 0 && this.displayHeight != 0 && this.displayWidth + (int)(0.1 * this.displayWidth) <= 3096 && this.displayHeight + (int)(0.1 * this.displayHeight) <= 3096) {
                this.displayWidth += (int)(0.1 * this.displayWidth);
                this.displayHeight += (int)(0.1 * this.displayHeight);
                this.imageArea.repaint();
            }
        } else if (source == this.stretchDownMenuItem) {
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
        } else if (source == this.refreshMenuItem) {
            this.imageArea.repaint();
        } else if (source == this.doubleSizeMenuItem) {
            if (this.displayWidth != 0 && this.displayHeight != 0 && 2 * this.displayWidth <= 3096 && 2 * this.displayHeight <= 3096) {
                this.displayWidth *= 2;
                this.displayHeight *= 2;
                this.imageArea.repaint();
            }
        } else if (source == this.halfSizeMenuItem) {
            if (this.displayWidth != 0 && this.displayHeight != 0) {
                this.displayWidth *= 0.5;
                this.displayHeight *= 0.5;
                this.imageArea.repaint();
            }
        } else if (source == this.byteStreamMenuItem) {
            this.progressiveDisplay = !this.progressiveDisplay;
        } else if (source == this.aboutMenuItem) {
            if (this.aboutDialog == null) {
                this.aboutDialog = new AboutDialog(this);
                this.aboutDialog.setVisible(true);
            } else if (this.aboutDialog.displayNow) {
                this.aboutDialog.toFront();
            } else {
                this.aboutDialog = new AboutDialog(this);
                this.aboutDialog.setVisible(true);
            }
        }
    }        



    @Override
    public void windowClosing(WindowEvent e) {
        if (!this.isDecompressing) {
            if (this.isApplet) {
                this.parent.displayNow = false;
            }
            this.setVisible(false);
            this.dispose();
            if (!this.isApplet) {
                System.exit(0);
            }
        }
    }


    // Implement other WindowListener methods with empty bodies
    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosed(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}


    private void openFile() {
        this.openDialog = null;
        (this.openDialog = new FileDialog(this, "Open File")).setFile(new String("*.cod"));
        this.openDialog.setVisible(true);
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
                // print the contents of the Throwable in a readable manner on the console
                t.printStackTrace();

                this.isDecompressing = false;
                this.errorDialog = new ErrorMessage(this, new String("open and read cod file error ! mainly because of memory and swap space !"));
                this.errorDialog.setVisible(true);
            }
        }
        this.openDialog.setVisible(false);
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
                        final Rectangle bounds = this.imageArea.getBounds();
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
            this.errorDialog = new ErrorMessage(this, new String("open and read cod file error ! mainly because of memory and swap space !"));
            this.errorDialog.setVisible(true);
            this.isDecompressing = false;
        }
        this.isDecompressing = false;
    }
    
    static {
        Strike.first = true;
    }
}
