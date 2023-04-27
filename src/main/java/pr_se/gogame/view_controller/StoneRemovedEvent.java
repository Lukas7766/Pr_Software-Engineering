package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

public class StoneRemovedEvent extends StoneEvent {
    public StoneRemovedEvent(GameCommand gameCommand, int col, int row) {
        super(gameCommand, col, row);
    }
}
