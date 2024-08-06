import java.awt.Color;
import java.awt.Font;
import java.awt.List;

// 
// Decompiled by Procyon v0.6.0
// 

class winSelector extends List
{
    public winSelector(final int rows, final String[] array) {
        super(rows, false);
        this.setFont(new Font("TimesRoman", 0, 12));
        this.setBackground(Color.lightGray);
        for (int i = 0; i < array.length; ++i) {
            this.addItem(array[i]);
        }
    }
}
