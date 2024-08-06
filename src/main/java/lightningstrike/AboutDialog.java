package lightningstrike;

import java.awt.*;
import java.awt.event.*;

class AboutDialog extends Dialog {
    Strike parent;
    Button cancel;
    Panel buttonPanel;
    public boolean displayNow;

    public AboutDialog(final Strike strike) {
        super(strike, "About Lightning Strike", true);
        this.displayNow = false;
        this.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        this.setBackground(Color.lightGray);
        this.parent = strike;
        this.displayNow = true;
        final Rectangle bounds = strike.getBounds();
        this.setLocation(bounds.x + 90, bounds.y + 90);
        this.setSize(480, 220);

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

        final Panel comp4 = new Panel();
        comp4.setLayout(new GridLayout(7, 1));
        this.add(comp4, BorderLayout.CENTER);

        comp4.add(new Label(""));
        comp4.add(new Label("Lightning Strike Version 2.6 ", 1));
        comp4.add(new Label("A Wavelet Image Decompressor Written in JAVA", 1));
        comp4.add(new Label("Oct. 1996", 1));
        comp4.add(new Label("Infinop Inc.", 1));
        comp4.add(new Label("http://www.infinop.com", 1));
        comp4.add(new Label(""));

        // Add action listener to the cancel button
        this.cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        // Add window listener to handle window closing event
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                dispose();
            }
        });
    }
}
