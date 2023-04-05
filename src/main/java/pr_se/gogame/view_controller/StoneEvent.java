package pr_se.gogame.view_controller;

import pr_se.gogame.model.StoneColor;

public abstract class StoneEvent {
    private final int COL;
    private final int ROW;

    protected StoneEvent(int col, int row) {
        this.COL = col;
        this.ROW = row;
    }

    public int getCol() {
        return COL;
    }

    public int getRow() {
        return ROW;
    }
}
