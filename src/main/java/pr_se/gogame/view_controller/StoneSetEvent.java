package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

public class StoneSetEvent extends StoneEvent {
    private final StoneColor COLOR;

    private final int moveNumber;

    public StoneSetEvent(GameCommand gameCommand, int col, int row, int moveNumber) {
        super(gameCommand, col, row);

        if(moveNumber < 0 || col < 0 || row < 0) {
            throw new IllegalArgumentException();
        }

        if(gameCommand == null) {
            throw new NullPointerException();
        }

        if(gameCommand == GameCommand.BLACKPLAYS || gameCommand == GameCommand.BLACKHANDICAP) {
            this.COLOR = StoneColor.BLACK;
        } else if(gameCommand == GameCommand.WHITEPLAYS || gameCommand == GameCommand.WHITEHANDICAP) {
            this.COLOR = StoneColor.WHITE;
        } else {
            throw new IllegalArgumentException();
        }

        this.moveNumber = moveNumber;
    }

    public StoneColor getColor() {
        return COLOR;
    }

    public int getMoveNumber() {
        return moveNumber;
    }
}
