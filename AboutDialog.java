import java.awt.Event;
import java.awt.Rectangle;
import java.awt.Label;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Dialog;

// 
// Decompiled by Procyon v0.6.0
// 

class AboutDialog extends Dialog
{
    Strike parent;
    Button Cancel;
    Panel buttonPanel;
    public boolean displayNow;
    
    public AboutDialog(final Strike strike) {
        super(strike, "About Lightning Strike", true);
        this.displayNow = false;
        this.setFont(new Font("TimesRoman", 0, 18));
        this.setBackground(Color.lightGray);
        this.parent = strike;
        this.displayNow = true;
        final Rectangle bounds = strike.bounds();
        super.move(bounds.x + 90, bounds.y + 90);
        super.resize(480, 220);
        final Panel comp = new Panel();
        comp.setBackground(Color.lightGray);
        this.add("North", comp);
        final Panel comp2 = new Panel();
        comp2.setBackground(Color.lightGray);
        this.add("East", comp2);
        final Panel comp3 = new Panel();
        comp3.setBackground(Color.lightGray);
        this.add("West", comp3);
        this.Cancel = new Button("Cancel");
        (this.buttonPanel = new Panel()).setLayout(new FlowLayout());
        this.buttonPanel.add(this.Cancel);
        this.add("South", this.buttonPanel);
        final Panel comp4 = new Panel();
        comp4.setLayout(new GridLayout(7, 1));
        comp4.add(new Label(""));
        comp4.add(new Label("Lightning Strike Version 2.6 ", 1));
        comp4.add(new Label("A Wavelet Image Decompressor Written in JAVA", 1));
        comp4.add(new Label("Oct. 1996", 1));
        comp4.add(new Label("Infinop Inc.", 1));
        comp4.add(new Label("http://www.infinop.com", 1));
        comp4.add(new Label(""));
        this.add("Center", comp4);
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
