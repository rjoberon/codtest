package lightningstrike;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Component;

// 
// Decompiled by Procyon v0.6.0
// 

public class ThreeDRectangle extends DrawnRectangle
{
    protected static ThreeDBorderStyle _defaultState;
    private ThreeDBorderStyle state;
    
    public ThreeDRectangle(final Component component) {
        this(component, ThreeDRectangle._defaultState, DrawnRectangle._defaultThickness, 0, 0, 0, 0);
    }
    
    public ThreeDRectangle(final Component component, final int n) {
        this(component, ThreeDRectangle._defaultState, n, 0, 0, 0, 0);
    }
    
    public ThreeDRectangle(final Component component, final int n, final int n2, final int n3, final int n4) {
        this(component, ThreeDRectangle._defaultState, DrawnRectangle._defaultThickness, n, n2, n3, n4);
    }
    
    public ThreeDRectangle(final Component component, final int n, final int n2, final int n3, final int n4, final int n5) {
        this(component, ThreeDRectangle._defaultState, n, n2, n3, n4, n5);
    }
    
    public ThreeDRectangle(final Component component, final ThreeDBorderStyle state, final int n, final int n2, final int n3, final int n4, final int n5) {
        super(component, n, n2, n3, n4, n5);
        this.state = state;
    }
    
    public void paint() {
        if (this.state == ThreeDBorderStyle.RAISED) {
            this.paintRaised();
            return;
        }
        this.paintInset();
    }
    
    public void raise() {
        this.state = ThreeDBorderStyle.RAISED;
    }
    
    public void inset() {
        this.state = ThreeDBorderStyle.INSET;
    }
    
    public boolean isRaised() {
        return this.state == ThreeDBorderStyle.RAISED;
    }
    
    public String paramString() {
        return super.paramString() + "," + this.state;
    }
    
    public void paintRaised() {
        final Graphics graphics = super.drawInto.getGraphics();
        if (graphics != null) {
            this.raise();
            this.drawTopLeftLines(graphics, this.brighter());
            this.drawBottomRightLines(graphics, this.getLineColor());
        }
    }
    
    public void paintInset() {
        final Graphics graphics = super.drawInto.getGraphics();
        if (graphics != null) {
            this.inset();
            this.drawTopLeftLines(graphics, this.getLineColor());
            this.drawBottomRightLines(graphics, this.brighter());
        }
    }
    
    private void drawTopLeftLines(final Graphics graphics, final Color color) {
        final int thickness = this.getThickness();
        graphics.setColor(color);
        for (int i = 0; i < thickness; ++i) {
            graphics.drawLine(super.x + i, super.y + i, super.x + super.width - (i + 1), super.y + i);
            graphics.drawLine(super.x + i, super.y + i + 1, super.x + i, super.y + super.height - (i + 1));
        }
    }
    
    private void drawBottomRightLines(final Graphics graphics, final Color color) {
        final int thickness = this.getThickness();
        graphics.setColor(color);
        for (int i = 1; i <= thickness; ++i) {
            graphics.drawLine(super.x + i - 1, super.y + super.height - i, super.x + super.width - i, super.y + super.height - i);
            graphics.drawLine(super.x + super.width - i, super.y + i - 1, super.x + super.width - i, super.y + super.height - i);
        }
    }
    
    static {
        ThreeDRectangle._defaultState = ThreeDBorderStyle.RAISED;
    }
}
