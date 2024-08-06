import java.awt.Event;
import java.awt.Rectangle;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Dialog;

// 
// Decompiled by Procyon v0.6.0
// 

class propertyDialog extends Dialog
{
    private Strike parent;
    private Button Cancel;
    private Panel buttonPanel;
    private Panel centralPanel;
    private Label text;
    private Label text1;
    private Label text2;
    private Label text3;
    private Label text4;
    private Label text5;
    private Label text6;
    public boolean displayNow;
    
    public propertyDialog(final Strike strike, final String title, final String str, final int i, final boolean b, final short n, final short n2, final float f) {
        super(strike, title, true);
        this.displayNow = false;
        this.setFont(new Font("TimesRoman", 0, 18));
        this.setBackground(Color.lightGray);
        this.parent = strike;
        this.displayNow = true;
        final Rectangle bounds = strike.bounds();
        super.move(bounds.x + 90, bounds.y + 90);
        super.resize(440, 290);
        this.Cancel = new Button("CANCEL");
        (this.buttonPanel = new Panel()).setLayout(new FlowLayout());
        this.buttonPanel.add(this.Cancel);
        this.add("South", this.buttonPanel);
        (this.centralPanel = new Panel()).setLayout(new GridLayout(8, 1, 2, 2));
        this.text = new Label(new String(" "), 1);
        this.centralPanel.add(this.text);
        this.text1 = new Label(new String("File  Name: " + str), 1);
        this.centralPanel.add(this.text1);
        if (b) {
            this.text2 = new Label(new String("Image Size: " + n + " X " + n2 + " True Color"), 1);
        }
        else {
            this.text2 = new Label(new String("Image Size: " + n + " X " + n2 + " Grey Image"), 1);
        }
        this.centralPanel.add(this.text2);
        if (b) {
            this.text3 = new Label(new String("Original Image(24 bit) Data Size: " + n * n2 * 3 + " bytes"), 1);
        }
        else {
            this.text3 = new Label(new String("Original Image Data Size: " + n * n2 + " bytes"), 1);
        }
        this.centralPanel.add(this.text3);
        this.text4 = new Label(new String("Compressed Image Data Size: " + i + " bytes"), 1);
        this.centralPanel.add(this.text4);
        if (b) {
            this.text5 = new Label(new String("Compression Ratio: " + n * n2 * 3 / (float)i), 1);
        }
        else {
            this.text5 = new Label(new String("Compression Ratio: " + n * n2 / (float)i), 1);
        }
        this.centralPanel.add(this.text5);
        this.text6 = new Label(new String("Decompress time: " + f + " Sec."), 1);
        this.centralPanel.add(this.text6);
        this.add("Center", this.centralPanel);
        this.setResizable(true);
    }
    
    public boolean handleEvent(final Event event) {
        if (event.target == this.Cancel) {
            this.displayNow = false;
            this.hide();
            this.dispose();
            return true;
        }
        switch (event.id) {
            case 201: {
                this.displayNow = false;
                this.hide();
                this.dispose();
                return true;
            }
            default: {
                return false;
            }
        }
    }
}
