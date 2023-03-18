package app;

public class StoneSetEvent {

    private final int COL;
    private final int ROW;

    private final StoneColor COLOR;

    public StoneSetEvent(int col, int row, StoneColor color) {
        this.COL = col;
        this.ROW = row;
        this.COLOR = color;
    }

    public int getCol() {
        return COL;
    }

    public int getRow() {
        return ROW;
    }

    public StoneColor getColor() {
        return COLOR;
    }
}
