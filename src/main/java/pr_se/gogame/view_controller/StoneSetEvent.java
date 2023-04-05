package pr_se.gogame.view_controller;

import pr_se.gogame.model.StoneColor;

public class StoneSetEvent extends StoneEvent {
    private final StoneColor COLOR;

    public StoneSetEvent(int col, int row, StoneColor color) {
        super(col, row);
        this.COLOR = color;
    }

    public StoneColor getColor() {
        return COLOR;
    }
}
