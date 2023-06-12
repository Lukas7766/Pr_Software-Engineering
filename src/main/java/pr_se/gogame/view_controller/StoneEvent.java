package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

public class StoneEvent extends GameEvent {
    private final int X;
    private final int Y;

    private final int moveNumber;

    private final StoneColor COLOR;

    public StoneEvent(GameCommand gameCommand, int x, int y, int moveNumber) {
        super(gameCommand);
        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        this.X = x;
        this.Y = y;
        this.moveNumber = moveNumber;

        switch(gameCommand) {
            case BLACK_HAS_CAPTURED:
            case WHITE_HAS_CAPTURED:
            case HANDICAP_POS:
            case DEBUG_INFO:
                this.COLOR = null;
                break;

            case BLACK_STONE_SET:
                this.COLOR = StoneColor.BLACK;
                break;

            case WHITE_STONE_SET:
                this.COLOR = StoneColor.WHITE;
                break;

            default:
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
