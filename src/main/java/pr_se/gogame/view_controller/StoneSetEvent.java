package pr_se.gogame.view_controller;

import pr_se.gogame.model.StoneColor;
import pr_se.gogame.model.StoneGroupPointer;

public class StoneSetEvent extends StoneEvent {
    private final StoneColor COLOR;

    private final int moveNumber;

    public StoneSetEvent(int col, int row, StoneColor color, int moveNumber) {
        super(col, row);

        if(moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        if(color == null) {
            throw new NullPointerException();
        }
        this.COLOR = color;
        this.moveNumber = moveNumber;
    }

    public StoneColor getColor() {
        return COLOR;
    }

    public int getMoveNumber() {
        return moveNumber;
    }
}
