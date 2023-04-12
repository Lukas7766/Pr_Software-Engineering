package pr_se.gogame.view_controller;

import pr_se.gogame.model.StoneColor;

public class StoneRemovedEvent extends StoneEvent {
    public StoneRemovedEvent(int col, int row) {
        super(col, row);
    }
}
