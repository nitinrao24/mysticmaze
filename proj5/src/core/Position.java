package core;

import java.util.Objects;

public class Position {
    public final int x;
    public final int y;

    public Position(int x, int y) {
        this.x = x; // stores x coordinate
        this.y = y; // stores y coordinate
    }

    public Position shift(int dx, int dy) {
        return new Position(x + dx, y + dy); // returns a new position after moving
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true; // same object means same position
        }

        if (!(other instanceof Position)) {
            return false; // cannot compare Position with a different type
        }

        Position p = (Position) other;
        return x == p.x && y == p.y; // positions match if both coordinates match
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y); // lets Position work in HashSet and HashMap
    }
}
