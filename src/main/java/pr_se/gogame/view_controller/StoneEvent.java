package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

public class StoneEvent extends GameEvent {
    private final int X;
    private final int Y;

    private final int moveNumber;

    private final StoneColor COLOR;

    public StoneEvent(GameCommand gameCommand, int x, int y, StoneColor c, int moveNumber) {
        super(gameCommand);
        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        if(this.getGameCommand() != GameCommand.STONE_WAS_SET && this.getGameCommand() != GameCommand.STONE_WAS_CAPTURED && this.getGameCommand() != GameCommand.DEBUG_INFO) {
            throw new IllegalArgumentException("StoneEvent must pertain to stones");
        }

        this.X = x;
        this.Y = y;
        this.COLOR = c;
        this.moveNumber = moveNumber;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public StoneColor getColor() {
        return COLOR;
    }
}
