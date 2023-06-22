package pr_se.gogame.view_controller;

import pr_se.gogame.model.Game;
import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

import static pr_se.gogame.model.GameCommand.*;

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

        if(hasPosition()) {
            throw new IllegalArgumentException("GameCommand " + gameCommand + " is incompatible with GameCommand-only constructor.");
        }

        this.gameCommand = gameCommand;
        this.X = -1;
        this.Y = -1;
        this.moveNumber = -1;
        this.COLOR = null;
    }

    public GameEvent(GameCommand gameCommand, int x, int y, int moveNumber) {
        if(gameCommand == null) {
            throw new NullPointerException("gameCommand was null in GameEvent constructor.");
        }

        this.gameCommand = gameCommand;

        if(!hasPosition() || isStoneRelated()) {
            throw new IllegalArgumentException("GameCommand " + gameCommand + " is incompatible with constructor for position-related events (use constructor with StoneColor instead).");
        }

        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        this.X = x;
        this.Y = y;
        this.moveNumber = moveNumber;
        this.COLOR = null;
    }

    public GameEvent(GameCommand gameCommand, int x, int y, StoneColor c, int moveNumber) {
        if(gameCommand == null) {
            throw new NullPointerException();
        }

        this.gameCommand = gameCommand;

        if(!isStoneRelated()) {
            throw new IllegalArgumentException("GameCommand " + gameCommand + " is incompatible with constructor for stone-related events.");
        }

        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        this.X = x;
        this.Y = y;
        this.COLOR = c;
        this.moveNumber = moveNumber;
    }

    public GameCommand getGameCommand() {
        return gameCommand;
    }

    public int getX() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query x coordinate from event without position.");
        }
        return X;
    }

    public int getY() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query y coordinate from event without position.");
        }
        return Y;
    }

    public int getMoveNumber() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query move number from non move-related event.");
        }
        return moveNumber;
    }

    public StoneColor getColor() {
        if(!isStoneRelated()) {
            throw new IllegalStateException("Cannot query color from non stone-related event.");
        }
        return COLOR;
    }

    private boolean hasPosition() {
        return isStoneRelated() || gameCommand == MARK_CIRCLE || gameCommand == MARK_SQUARE || gameCommand == MARK_TRIANGLE|| gameCommand == UNMARK || gameCommand == DEBUG_INFO;
    }

    private boolean isStoneRelated() {
        return gameCommand == STONE_WAS_SET || gameCommand == STONE_WAS_REMOVED || gameCommand == HANDICAP_SET || gameCommand == SETUP_STONE_SET;
    }

    @Override
    public String toString() {
        return "GameEvent: " + gameCommand;
    }
}
