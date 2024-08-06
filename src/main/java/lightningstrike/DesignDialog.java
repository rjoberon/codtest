package lightningstrike;

import java.awt.Rectangle;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Dialog;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.BorderLayout;

class DesignDialog extends Dialog {
    Strike parent;
    Button cancel;
    Panel buttonPanel;
    String message;
    public boolean displayNow;

    public DesignDialog(final Strike strike, final String message) {
        super(strike, "Lightning Strike in Java", true);
        this.displayNow = false;
        this.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        this.setBackground(Color.lightGray);
        this.parent = strike;
        this.message = message;
        this.displayNow = true;
        final Rectangle bounds = strike.getBounds();
        this.setLocation(bounds.x + 90, bounds.y + 90);
        this.setSize(380, 200);
        
        final Panel comp = new Panel();
        comp.setBackground(Color.lightGray);
        this.add(comp, BorderLayout.NORTH);
        
        final Panel comp2 = new Panel();
        comp2.setBackground(Color.lightGray);
        this.add(comp2, BorderLayout.EAST);
        
        final Panel comp3 = new Panel();
        comp3.setBackground(Color.lightGray);
        this.add(comp3, BorderLayout.WEST);
        
        this.cancel = new Button("Cancel");
        this.buttonPanel = new Panel();
        this.buttonPanel.setLayout(new FlowLayout());
        this.buttonPanel.add(this.cancel);
        this.add(this.buttonPanel, BorderLayout.SOUTH);
        
        this.add(new ErrorCanvas(message), BorderLayout.CENTER);
        this.setResizable(true);
        
        this.cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayNow = false;
                setVisible(false);
                dispose();
            }
        });
        
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