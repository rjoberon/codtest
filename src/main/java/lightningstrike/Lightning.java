package lightningstrike;

import java.awt.Graphics;
import java.awt.Button;
import java.applet.Applet;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// 
// Decompiled by Procyon v0.6.0
// 

// We will not use this class as an applet anyway...so we can simply ignore those warnings...
@SuppressWarnings("removal")
public class Lightning extends Applet implements ActionListener
{
    private Button b;
//    private boolean show;
    private String buttonString;
    public boolean displayNow;
    private boolean canNotActivate;
//    private Image openFileImage;
    public Strike oneStrike;
    private String hostName;
    private String dirName;
    private String fileName;
    private String fileNumber;
    private String tempName;
    private String fullDirName;
    private String[] fileList;
    private int numberOfFiles;
    
    public void init() {
        final String parameter = this.getParameter("host");
        this.hostName = parameter;
        if (parameter == null) {
            this.canNotActivate = true;
        }
        if ((this.dirName = this.getParameter("directory")) == null) {
            this.canNotActivate = true;
        }
        if ((this.fileNumber = this.getParameter("number_of_images")) == null) {
            this.canNotActivate = true;
        }
        else {
            this.numberOfFiles = Integer.parseInt(this.fileNumber);
        }
        this.fullDirName = "http://".concat(this.hostName).concat(this.dirName);
        if (this.numberOfFiles != 0) {
            this.fileList = new String[this.numberOfFiles];
        }
        for (int i = 1; i <= this.numberOfFiles; ++i) {
            this.tempName = new String("image" + String.valueOf(i));
            if ((this.fileName = this.getParameter(this.tempName)) == null) {
                this.canNotActivate = true;
            }
            this.fileList[i - 1] = this.fileName;
        }
        this.buttonString = " Lightning Strike Implemented in JAVA ";
        this.b = new Button(this.buttonString);
        this.add(this.b);
        this.b.addActionListener(this);

    }
    
    public void paint(final Graphics graphics) {
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.b) {
            if (this.oneStrike == null) {
                if (this.getParent().getParent() == null) {
                    System.out.println("Parent frame is null!");
                }
                if (!this.canNotActivate) {
                    this.oneStrike = new Strike(this, this.fullDirName, this.fileList);
                    this.displayNow = true;
                } else {
                    this.displayNow = false;
                }
                return;
            }
            if (this.displayNow) {
                this.oneStrike.toFront();
                this.oneStrike.requestFocus();
                this.oneStrike.reShow();
                return;
            }
            if (!this.canNotActivate) {
                this.oneStrike = new Strike(this, this.fullDirName, this.fileList);
                this.displayNow = true;
            } else {
                this.displayNow = false;
            }
        }
    }
    
    public Lightning() {
//        this.show = false;
        this.displayNow = false;
        this.canNotActivate = false;
    }
}
