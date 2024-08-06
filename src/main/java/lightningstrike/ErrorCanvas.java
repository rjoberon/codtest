package lightningstrike;

import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Canvas;

// 
// Decompiled by Procyon v0.6.0
// 

class ErrorCanvas extends Canvas
{
    String message;
    
    public ErrorCanvas(final String message) {
        setSize(300, 200);
        this.message = message;
    }
    
    public void paint(final Graphics graphics) {
        final Dimension size = this.getSize();
        graphics.setColor(Color.yellow);
        final int[] xpoints = new int[4];
        final int[] ypoints = new int[4];
        ypoints[0] = (xpoints[0] = 10);
        xpoints[1] = size.width / 3;
        ypoints[1] = 3 * size.height / 4;
        xpoints[2] = size.width - 10;
        ypoints[2] = 10;
        ypoints[3] = (xpoints[3] = 10);
        graphics.fillPolygon(new Polygon(xpoints, ypoints, 4));
        graphics.setColor(Color.black);
        graphics.drawString(this.message, 20, size.height / 2 - 10);
    }
}
