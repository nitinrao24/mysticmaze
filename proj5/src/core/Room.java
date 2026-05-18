package core;

public class Room {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // All the getter methods for the room's properties and boundaries.

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLeft() {
        return x;
    }

    public int getRight() {
        return x + width - 1;
    }

    public int getBottom() {
        return y;
    }

    public int getTop() {
        return y + height - 1;
    }

    public int getCenterX() {
        return x + width / 2;
    }

    public int getCenterY() {
        return y + height / 2;
    }

    // The following method will return true if this room overlaps or comes within 1 tile of another room and will return false if otherwise.

    public boolean overlaps(Room other, int buffer) {
        return this.getLeft() - buffer <= other.getRight() && this.getRight() + buffer >= other.getLeft()
                && this.getBottom() - buffer <= other.getTop() && this.getTop() + buffer >= other.getBottom();
    }

}
