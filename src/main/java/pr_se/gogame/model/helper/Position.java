package pr_se.gogame.model.helper;

import java.util.Objects;

/**
 * Contains an absolute position on the Go board
 */
public class Position {

    /**
     * X coordinate of this Position, starting at the left
     */
    private final int x;

    /**
     * Y coordinate of this Position, starting at the top
     */
    private final int y;

    /**
     * Creates a new Position
     * @param x X coordinate of this Position, starting at the left
     * @param y Y coordinate of this Position, starting at the top
     */
    public Position(int x, int y) {
        if(x < 0 || y < 0) {
            throw new IllegalArgumentException();
        }
        this.x = x;
        this.y = y;
    }

    // Object's methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return getX() == position.getX() && getY() == position.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }

    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ")";
    }

    /**
     * Returns this Position's X coordinate
     * @return this Position's X coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns this Position's Y coordinate
     * @return this Position's Y coordinate
     */
    public int getY() {
        return y;
    }
}
