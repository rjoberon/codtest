// 
// Decompiled by Procyon v0.6.0
// 

public class ThreeDBorderStyle
{
    public static final ThreeDBorderStyle RAISED;
    public static final ThreeDBorderStyle INSET;
    
    public String toString() {
        if (this == ThreeDBorderStyle.RAISED) {
            return this.getClass().getName() + "=RAISED";
        }
        return this.getClass().getName() + "=INSET";
    }
    
    private ThreeDBorderStyle() {
    }
    
    static {
        RAISED = new ThreeDBorderStyle();
        INSET = new ThreeDBorderStyle();
    }
}
