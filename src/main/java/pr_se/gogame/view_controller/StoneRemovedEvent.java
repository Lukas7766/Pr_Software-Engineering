package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;


public class StoneRemovedEvent extends StoneEvent {
    public StoneRemovedEvent(GameCommand gameCommand, int col, int row) {
        super(gameCommand, col, row);
    }
}
