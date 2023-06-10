package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

public class StoneEvent extends GameEvent {
    private final int X;
    private final int Y;

    private final int moveNumber;

    private final StoneColor COLOR;

    protected StoneEvent(GameCommand gameCommand, int x, int y, int moveNumber) {
        super(gameCommand);
        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        this.X = x;
        this.Y = y;
        this.moveNumber = moveNumber;

        if(gameCommand == GameCommand.BLACK_HAS_CAPTURED || gameCommand == GameCommand.WHITE_HAS_CAPTURED || gameCommand == GameCommand.DEBUG_INFO) {
            this.COLOR = null;
        } else if(gameCommand == GameCommand.BLACK_PLAYS || gameCommand == GameCommand.BLACK_HANDICAP) {
            this.COLOR = StoneColor.BLACK;
        } else if(gameCommand == GameCommand.WHITE_PLAYS || gameCommand == GameCommand.WHITE_HANDICAP) {
            this.COLOR = StoneColor.WHITE;
        } else {
            throw new IllegalArgumentException();
        }
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
