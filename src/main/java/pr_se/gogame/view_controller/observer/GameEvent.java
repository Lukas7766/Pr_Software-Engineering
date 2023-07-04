package pr_se.gogame.view_controller.observer;

import pr_se.gogame.model.helper.GameCommand;
import pr_se.gogame.model.helper.StoneColor;

import static pr_se.gogame.model.helper.GameCommand.*;

public class GameEvent {

    private final GameCommand gameCommand;

    private final int x;

    private final int y;

    private final int moveNumber;

    private final StoneColor color;

    public GameEvent(GameCommand gameCommand) {
        if(gameCommand == null) {
            throw new NullPointerException();
        }

        if(hasPosition()) {
            throw new IllegalArgumentException(gameCommand + " is incompatible with GameCommand-only constructor.");
        }

        this.gameCommand = gameCommand;
        this.x = -1;
        this.y = -1;
        this.moveNumber = -1;
        this.color = null;
    }

    public GameEvent(GameCommand gameCommand, int x, int y, int moveNumber) {
        if(gameCommand == null) {
            throw new NullPointerException("gameCommand was null in GameEvent constructor.");
        }

        this.gameCommand = gameCommand;

        if(!hasPosition() || isStoneRelated()) {
            throw new IllegalArgumentException(gameCommand + " is incompatible with constructor for position-related events (use constructor with StoneColor instead).");
        }

        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        this.x = x;
        this.y = y;
        this.moveNumber = moveNumber;
        this.color = null;
    }

    public GameEvent(GameCommand gameCommand, int x, int y, StoneColor c, int moveNumber) {
        if(gameCommand == null) {
            throw new NullPointerException();
        }

        this.gameCommand = gameCommand;

        if(!isStoneRelated()) {
            throw new IllegalArgumentException(gameCommand + " is incompatible with constructor for stone-related events.");
        }

        if(x < 0 || y < 0 || moveNumber < 0) {
            throw new IllegalArgumentException();
        }

        this.x = x;
        this.y = y;
        this.color = c;
        this.moveNumber = moveNumber;
    }

    public GameCommand getGameCommand() {
        return gameCommand;
    }

    public int getX() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query x coordinate from event without position.");
        }
        return x;
    }

    public int getY() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query y coordinate from event without position.");
        }
        return y;
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
        return color;
    }

    private boolean hasPosition() {
        return isStoneRelated() || gameCommand == MARK_CIRCLE || gameCommand == MARK_SQUARE || gameCommand == MARK_TRIANGLE|| gameCommand == UNMARK || gameCommand == DEBUG_INFO || gameCommand == HANDICAP_SET || gameCommand == HANDICAP_REMOVED;
    }

    private boolean isStoneRelated() {
        return gameCommand == STONE_WAS_SET || gameCommand == STONE_WAS_REMOVED || gameCommand == SETUP_STONE_SET;
    }

    @Override
    public String toString() {
        return "GameEvent: " + gameCommand;
    }
}
