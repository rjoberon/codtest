package internal.lightningstrike.applet;
import java.awt.*;
import java.awt.event.*;

public class propertyDialog extends Dialog {
    private Label text6;
    public boolean displayNow;
    private Button Cancel;
    private Panel buttonPanel;
    private Panel centralPanel;
    private Label text;
    private Label text1;
    private Label text2;
    private Label text3;
    private Label text4;
    private Label text5;
//    private Strike parent;

    public propertyDialog(final Strike strike, final String title, final String str, final int i, final boolean b, final short n, final short n2, final float f) {
        super(strike, title, true);
        this.displayNow = false;
        this.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        this.setBackground(Color.lightGray);
  //      this.parent = strike;
        this.displayNow = true;
        final Rectangle bounds = strike.getBounds();
        super.setLocation(bounds.x + 90, bounds.y + 90);
        super.setSize(440, 290);
        this.Cancel = new Button("CANCEL");
        this.Cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        (this.buttonPanel = new Panel()).setLayout(new FlowLayout());
        this.buttonPanel.add(this.Cancel);
        this.add("South", this.buttonPanel);
        (this.centralPanel = new Panel()).setLayout(new GridLayout(8, 1, 2, 2));
        this.text = new Label(" ", Label.CENTER);
        this.centralPanel.add(this.text);
        this.text1 = new Label("File  Name: " + str, Label.CENTER);
        this.centralPanel.add(this.text1);
        if (b) {
            this.text2 = new Label("Image Size: " + n + " X " + n2 + " True Color", Label.CENTER);
        } else {
            this.text2 = new Label("Image Size: " + n + " X " + n2 + " Grey Image", Label.CENTER);
        }
        this.centralPanel.add(this.text2);
        if (b) {
            this.text3 = new Label("Original Image(24 bit) Data Size: " + n * n2 * 3 + " bytes", Label.CENTER);
        } else {
            this.text3 = new Label("Original Image Data Size: " + n * n2 + " bytes", Label.CENTER);
        }
        this.centralPanel.add(this.text3);
        this.text4 = new Label("Compressed Image Data Size: " + i + " bytes", Label.CENTER);
        this.centralPanel.add(this.text4);
        if (b) {
            this.text5 = new Label("Compression Ratio: " + n * n2 * 3 / (float) i, Label.CENTER);
        } else {
            this.text5 = new Label("Compression Ratio: " + n * n2 / (float) i, Label.CENTER);
        }
        this.centralPanel.add(this.text5);
        this.text6 = new Label("Decompress time: " + f + " Sec.", Label.CENTER);
        this.centralPanel.add(this.text6);
        this.add("Center", this.centralPanel);
        this.setResizable(true);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }
}