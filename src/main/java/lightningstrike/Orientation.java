// 
// Decompiled by Procyon v0.6.0
// 

public class Orientation
{
    public static final Orientation BAD;
    public static final Orientation NORTH;
    public static final Orientation SOUTH;
    public static final Orientation EAST;
    public static final Orientation WEST;
    public static final Orientation CENTER;
    public static final Orientation TOP;
    public static final Orientation LEFT;
    public static final Orientation RIGHT;
    public static final Orientation BOTTOM;
    public static final Orientation HORIZONTAL;
    public static final Orientation VERTICAL;
    
    public static Orientation fromString(final String s) {
        Orientation orientation = Orientation.BAD;
        if (s.equals("NORTH") || s.equals("north")) {
            orientation = Orientation.NORTH;
        }
        else if (s.equals("SOUTH") || s.equals("south")) {
            orientation = Orientation.SOUTH;
        }
        else if (s.equals("EAST") || s.equals("east")) {
            orientation = Orientation.EAST;
        }
        else if (s.equals("WEST") || s.equals("west")) {
            orientation = Orientation.WEST;
        }
        else if (s.equals("CENTER") || s.equals("center")) {
            orientation = Orientation.CENTER;
        }
        else if (s.equals("TOP") || s.equals("top")) {
            orientation = Orientation.TOP;
        }
        else if (s.equals("LEFT") || s.equals("left")) {
            orientation = Orientation.LEFT;
        }
        else if (s.equals("RIGHT") || s.equals("right")) {
            orientation = Orientation.RIGHT;
        }
        else if (s.equals("BOTTOM") || s.equals("bottom")) {
            orientation = Orientation.BOTTOM;
        }
        else if (s.equals("VERTICAL") || s.equals("vertical")) {
            orientation = Orientation.VERTICAL;
        }
        else if (s.equals("HORIZONTAL") || s.equals("horizontal")) {
            orientation = Orientation.HORIZONTAL;
        }
        return orientation;
    }
    
    public String toString() {
        String s = new String();
        if (this == Orientation.NORTH) {
            s = this.getClass().getName() + "=NORTH";
        }
        else if (this == Orientation.SOUTH) {
            s = this.getClass().getName() + "=SOUTH";
        }
        else if (this == Orientation.EAST) {
            s = this.getClass().getName() + "=EAST";
        }
        else if (this == Orientation.WEST) {
            s = this.getClass().getName() + "=WEST";
        }
        else if (this == Orientation.CENTER) {
            s = this.getClass().getName() + "=CENTER";
        }
        else if (this == Orientation.TOP) {
            s = this.getClass().getName() + "=TOP";
        }
        else if (this == Orientation.LEFT) {
            s = this.getClass().getName() + "=LEFT";
        }
        else if (this == Orientation.RIGHT) {
            s = this.getClass().getName() + "=RIGHT";
        }
        else if (this == Orientation.BOTTOM) {
            s = this.getClass().getName() + "=BOTTOM";
        }
        else if (this == Orientation.HORIZONTAL) {
            s = this.getClass().getName() + "=HORIZONTAL";
        }
        else if (this == Orientation.VERTICAL) {
            s = this.getClass().getName() + "=VERTICAL";
        }
        else if (this == Orientation.BAD) {
            s = this.getClass().getName() + "=BAD";
        }
        return s;
    }
    
    private Orientation() {
    }
    
    static {
        BAD = new Orientation();
        NORTH = new Orientation();
        SOUTH = new Orientation();
        EAST = new Orientation();
        WEST = new Orientation();
        CENTER = new Orientation();
        TOP = new Orientation();
        LEFT = new Orientation();
        RIGHT = new Orientation();
        BOTTOM = new Orientation();
        HORIZONTAL = new Orientation();
        VERTICAL = new Orientation();
    }
}
