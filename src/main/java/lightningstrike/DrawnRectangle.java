package lightningstrike;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

// 
// Decompiled by Procyon v0.6.0
// 

public class DrawnRectangle extends Rectangle
{
    protected static int _defaultThickness;
    protected Component drawInto;
    private int thick;
    private Color lineColor;
    private Color fillColor;
    
    public DrawnRectangle(final Component component) {
        this(component, DrawnRectangle._defaultThickness, 0, 0, 0, 0);
    }
    
    public DrawnRectangle(final Component component, final int n) {
        this(component, n, 0, 0, 0, 0);
    }
    
    public DrawnRectangle(final Component component, final int n, final int n2, final int n3, final int n4) {
        this(component, DrawnRectangle._defaultThickness, n, n2, n3, n4);
    }
    
    public DrawnRectangle(final Component drawInto, final int thick, final int x, final int y, final int width, final int height) {
        this.drawInto = drawInto;
        this.thick = thick;
        this.reshape(x, y, width, height);
    }
    
    public Component component() {
        return this.drawInto;
    }
    
    public int getThickness() {
        return this.thick;
    }
    
    public void setThickness(final int thick) {
        this.thick = thick;
    }
    
    public void setLineColor(final Color lineColor) {
        this.lineColor = lineColor;
    }
    
    public void setFillColor(final Color fillColor) {
        this.fillColor = fillColor;
    }
    
    public void fill() {
        this.fill(this.getFillColor());
    }
    
    public Color getLineColor() {
        if (this.lineColor == null) {
            this.lineColor = this.drawInto.getBackground().darker().darker().darker();
        }
        return this.lineColor;
    }
    
    public Color getFillColor() {
        if (this.fillColor == null) {
            this.fillColor = this.drawInto.getBackground();
        }
        return this.fillColor;
    }
    
    public Rectangle getInnerBounds() {
        return new Rectangle(super.x + this.thick, super.y + this.thick, super.width - this.thick * 2, super.height - this.thick * 2);
    }
    
    public void paint() {
        this.paintFlat(this.drawInto.getGraphics(), this.getLineColor());
    }
    
    private void paintFlat(final Graphics graphics, final Color color) {
        if (graphics != null) {
            graphics.setColor(color);
            for (int i = 0; i < this.thick; ++i) {
                graphics.drawRect(super.x + i, super.y + i, super.width - i * 2 - 1, super.height - i * 2 - 1);
            }
        }
    }
    
    public void clearInterior() {
        this.fill(this.drawInto.getBackground());
    }
    
    public void clearExterior() {
        this.paintFlat(this.drawInto.getGraphics(), this.drawInto.getBackground());
    }
    
    public void clear() {
        this.clearExterior();
        this.clearInterior();
    }
    
    public void fill(final Color color) {
        final Graphics graphics = this.drawInto.getGraphics();
        if (graphics != null) {
            final Rectangle innerBounds = this.getInnerBounds();
            graphics.setColor(color);
            graphics.fillRect(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height);
            this.setFillColor(color);
        }
    }
    
    public String toString() {
        return super.toString() + "[" + this.paramString() + "]";
    }
    
    public String paramString() {
        return "color=" + this.getLineColor() + ",thickness=" + this.thick + ",fillColor=" + this.getFillColor();
    }
    
    protected Color brighter() {
        return this.getLineColor().brighter().brighter().brighter().brighter();
    }
    
    static {
        DrawnRectangle._defaultThickness = 2;
    }
}
