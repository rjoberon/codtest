package lightningstrike;

import java.awt.Rectangle;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// 
// Decompiled by Procyon v0.6.0
// 

class ErrorMessage extends Dialog {
    Strike parent;
    Button Cancel;
    Panel buttonPanel;
    String message;
    public boolean displayNow;

    public ErrorMessage(final Strike strike, final String message) {
        super(strike, "Lightning Strike Error !", false);
        this.displayNow = false;
        this.setBackground(Color.lightGray);
        this.parent = strike;
        this.message = message;
        this.displayNow = true;
        final Rectangle bounds = strike.getBounds();
        super.setLocation(bounds.x + 90, bounds.y + 90);
        super.setSize(280, 200);
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
        this.add("Center", new ErrorCanvas(message));
        this.setResizable(true);

        // Add ActionListener for the Cancel button
        this.Cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayNow = false;
                setVisible(false);
                dispose();
            }
        });

        // Add WindowListener for the window closing event
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                displayNow = false;
                setVisible(false);
                dispose();
            }
        });
    }
}
