import java.awt.image.ImageObserver;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Canvas;

// 
// Decompiled by Procyon v0.6.0
// 

class ImageCanvas extends Canvas
{
    Image image_;
    ErrorMessage errorDialog;
    Strike oneStrike;
    
    public ImageCanvas(final Image image_, final Strike oneStrike) {
        this.image_ = image_;
        this.oneStrike = oneStrike;
    }
    
    public void setImage(final Image image_) {
        this.image_ = image_;
    }
    
    public void paint(final Graphics graphics) {
        if (this.image_ != null) {
            graphics.drawImage(this.image_, 0, 0, this.oneStrike.displayWidth, this.oneStrike.displayHeight, this.oneStrike.imageArea);
        }
    }
}
