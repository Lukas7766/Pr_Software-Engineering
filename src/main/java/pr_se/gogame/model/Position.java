package pr_se.gogame.model;

import javafx.geometry.Pos;

import java.util.Objects;

public class Position /*implements Comparable<Position>*/ {
    public final int X;
    public final int Y;

    public Position(int X, int Y) {
        this.X = X;
        this.Y = Y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return X == position.X && Y == position.Y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(X, Y);
    }

    @Override
    public String toString() {
        return "(" + X + ", " + Y + ")";
    }

    /*@Override
    public int compareTo(Position p) {
        int retVal = this.Y - p.Y;
        if(retVal == 0) {
            retVal = this.X - p.X;
        }

        return retVal;
    }*/
}
