package pr_se.gogame.view_controller.observer;

import pr_se.gogame.model.helper.GameCommand;
import pr_se.gogame.model.helper.StoneColor;

import static pr_se.gogame.model.helper.GameCommand.*;

/**
 * GameEvent containing info for Game-related view updates
 */
public class GameEvent {

    /**
     * GameCommand containing info on which kind of event this is.
     */
    private final GameCommand gameCommand;

    /**
     * X coordinate starting at the left, where applicable
     */
    private final int x;

    /**
     * Y coordinate starting at the top, where applicable
     */
    private final int y;

    /**
     * Move number at the time of this GameEvent's occurrence
     */
    private final int moveNumber;

    /**
     * StoneColor of this GameEvent pertains to, where applicable
     */
    private final StoneColor color;

    /**
     * Creates a new GameEvent without any additional move information
     * @param gameCommand basic info about the cause of this GameEvent
     */
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

    /**
     * Creates a new GameEvent without a player color (used if no stone was set or removed)
     * @param gameCommand basic info about the cause of this GameEvent
     * @param x X coordinate, starting at the left
     * @param y Y coordinate, starting at the top
     * @param moveNumber move number at the time of this GameEvent occurrence
     */
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


    /**
     * Creates a new GameEvent that contains info about a stone being set
     * @param gameCommand basic info about the cause of this GameEvent
     * @param x X coordinate, starting at the left
     * @param y Y coordinate, starting at the top
     * @param c stone color of the move that caused this GameEvent
     * @param moveNumber move number at the time of this GameEvent occurrence
     */
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

    /**
     * @return the GameCommand containing basic info about the cause of this GameEvent
     */
    public GameCommand getGameCommand() {
        return gameCommand;
    }

    /**
     * @return X coordinate starting at the left, where applicable
     */
    public int getX() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query x coordinate from event without position.");
        }
        return x;
    }

    /**
     * @return Y coordinate starting at the top, where applicable
     */
    public int getY() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query y coordinate from event without position.");
        }
        return y;
    }

    /**
     * @return move number at the time of this GameEvent, where applicable
     */
    public int getMoveNumber() {
        if(!hasPosition()) {
            throw new IllegalStateException("Cannot query move number from non move-related event.");
        }
        return moveNumber;
    }

    /**
     * @return stone color of this GameEvent, where applicable
     */
    public StoneColor getColor() {
        if(!isStoneRelated()) {
            throw new IllegalStateException("Cannot query color from non stone-related event.");
        }
        return color;
    }

    /**
     * @return Whether this GameEvent should have position information or not (done like this to avoid
     *  having to downcast for specific events that extend the interface of GameEvent).
     */
    private boolean hasPosition() {
        return isStoneRelated() || gameCommand == MARK_CIRCLE || gameCommand == MARK_SQUARE || gameCommand == MARK_TRIANGLE|| gameCommand == UNMARK || gameCommand == DEBUG_INFO || gameCommand == HANDICAP_SET || gameCommand == HANDICAP_REMOVED;
    }

    /**
     * @return Whether this GameEvent should have position and color information or not (done like this to avoid
     *  having to downcast for specific events that extend the interface of GameEvent).
     */
    private boolean isStoneRelated() {
        return gameCommand == STONE_WAS_SET || gameCommand == STONE_WAS_REMOVED || gameCommand == SETUP_STONE_SET;
    }

    @Override
    public String toString() {
        return "GameEvent: " + gameCommand;
    }
}
