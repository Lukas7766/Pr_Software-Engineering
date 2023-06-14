package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

public class GameEvent {

    private final GameCommand gameCommand;

    private final int X;

    private final int Y;

    private final int moveNumber;

    private final StoneColor COLOR;

    public GameEvent(GameCommand gameCommand) {
        if(gameCommand == null) {
            throw new NullPointerException();
        }

        if(gameCommand == GameCommand.STONE_WAS_SET || gameCommand == GameCommand.STONE_WAS_CAPTURED || gameCommand == GameCommand.DEBUG_INFO) {
            throw new IllegalArgumentException("GameCommand " + gameCommand + " is incompatible with GameCommand-only constructor.");
        }

        this.gameCommand = gameCommand;
        this.X = -1;
        this.Y = -1;
        this.moveNumber = -1;
        this.COLOR = null;
    }

    public GameEvent(GameCommand gameCommand, int x, int y, StoneColor c, int moveNumber) {
        if(gameCommand == null) {
            throw new NullPointerException();
        }

        if(gameCommand != GameCommand.STONE_WAS_SET && gameCommand != GameCommand.STONE_WAS_CAPTURED && gameCommand != GameCommand.DEBUG_INFO) {
            throw new IllegalArgumentException("GameCommand " + gameCommand + " is incompatible with constructor for stone-related events.");
        }

        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        this.gameCommand = gameCommand;
        this.X = x;
        this.Y = y;
        this.COLOR = c;
        this.moveNumber = moveNumber;
    }

    public GameCommand getGameCommand() {
        return gameCommand;
    }

    public int getX() {
        if(!isStoneRelated(this)) {
            throw new IllegalStateException("Cannot query x coordinate from non stone-related event.");
        }
        return X;
    }

    public int getY() {
        if(!isStoneRelated(this)) {
            throw new IllegalStateException("Cannot query y coordinate from non stone-related event.");
        }
        return Y;
    }

    public int getMoveNumber() {
        if(!isStoneRelated(this)) {
            throw new IllegalStateException("Cannot query move number from non stone-related event.");
        }
        return moveNumber;
    }

    public StoneColor getColor() {
        if(!isStoneRelated(this)) {
            throw new IllegalStateException("Cannot query color from non stone-related event.");
        }
        return COLOR;
    }

    private static boolean isStoneRelated(GameEvent e) {
        return e.getGameCommand() == GameCommand.STONE_WAS_SET || e.getGameCommand() == GameCommand.STONE_WAS_CAPTURED || e.getGameCommand() == GameCommand.DEBUG_INFO;
    }
}
