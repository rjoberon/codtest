package lightningstrike;

import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Canvas;

// 
// Decompiled by Procyon v0.6.0
// 

public class Bargauge extends Canvas
{
    private double percentFill;
    private ThreeDRectangle border;
    private Color fillColor;
    
    public Bargauge(final Color fillColor) {
        this.border = new ThreeDRectangle(this);
        this.setFillColor(fillColor);
    }
    
    public void setFillColor(final Color fillColor) {
        this.fillColor = fillColor;
    }
    
    public void setFillPercent(final double percentFill) {
        this.percentFill = percentFill;
    }
    
    public void resize(final int n, final int n2) {
        this.setBounds(this.getLocation().x, this.getLocation().y, n, n2);
    }
    
    public void setBounds(final int x, final int y, final int n, final int n2) {
        super.setBounds(x, y, n, n2);
        this.border.setSize(n, n2);
    }
    
    public Dimension minimumSize() {
        return this.preferredSize();
    }
    
    public Dimension preferredSize() {
        final int width = this.border.getThickness() * 3;
        return new Dimension(width, width * 4);
    }
    
    public void paint(final Graphics graphics) {
        this.border.raise();
        this.border.paint();
        this.fill();
    }
    
    public void fill() {
        final Graphics graphics = this.getGraphics();
        if (graphics != null && this.percentFill > 0.0) {
            final Rectangle innerBounds = this.border.getInnerBounds();
            int width = innerBounds.width;
            int height = innerBounds.height;
            if (innerBounds.width > innerBounds.height) {
                width *= (int)(this.percentFill / 100.0);
            }
            else {
                height *= (int)(this.percentFill / 100.0);
            }
            graphics.setColor(this.fillColor);
            this.border.clearInterior();
            if (innerBounds.width > innerBounds.height) {
                graphics.fillRect(innerBounds.x, innerBounds.y, width, innerBounds.height);
                return;
            }
            graphics.fillRect(innerBounds.x, innerBounds.y + innerBounds.height - height, innerBounds.width, height);
        }
    }
    
    protected String paramString() {
        final Dimension size = this.getSize();
        return "fill percent=" + this.percentFill + "," + "orientation=" + ((size.width > size.height) ? Orientation.HORIZONTAL : Orientation.VERTICAL) + "," + "color" + this.fillColor;
    }
}
