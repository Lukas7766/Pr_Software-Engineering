package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

public class StoneSetEvent extends StoneEvent {


    public StoneSetEvent(GameCommand gameCommand, int col, int row, int moveNumber) {
        super(gameCommand, col, row, moveNumber);
    }
}
