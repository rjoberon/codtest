package lightningstrike;

import java.awt.Frame;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Image;
import java.awt.Button;
import java.applet.Applet;

// 
// Decompiled by Procyon v0.6.0
// 

public class Lightning extends Applet
{
    private Button b;
    private boolean show;
    private String buttonString;
    public boolean displayNow;
    private boolean canNotActivate;
    private Image openFileImage;
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
        this.add(this.b = new Button(this.buttonString));
    }
    
    public void paint(final Graphics graphics) {
    }
    
    public boolean action(final Event event, final Object o) {
        if (event.target == this.b) {
            if (this.oneStrike == null) {
                if (this.getParent().getParent() == null) {
                    System.out.println("Parent frame is null!");
                }
                if (!this.canNotActivate) {
                    this.oneStrike = new Strike(this, this.fullDirName, this.fileList);
                    this.displayNow = true;
                }
                else {
                    this.displayNow = false;
                }
                return true;
            }
            if (this.displayNow) {
                this.oneStrike.toFront();
                this.oneStrike.requestFocus();
                this.oneStrike.reShow();
                return true;
            }
            if (!this.canNotActivate) {
                this.oneStrike = new Strike(this, this.fullDirName, this.fileList);
                this.displayNow = true;
            }
            else {
                this.displayNow = false;
            }
        }
        return false;
    }
    
    public Lightning() {
        this.show = false;
        this.displayNow = false;
        this.canNotActivate = false;
    }
}
