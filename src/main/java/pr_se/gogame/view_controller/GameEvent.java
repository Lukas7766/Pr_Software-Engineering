package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;

public class GameEvent {

    private final GameCommand gameCommand;
    private final int size;
    private final int handicap;

    public GameEvent(GameCommand gameCommand){
        this(gameCommand,-1,-1);
    }

    public GameEvent(GameCommand gameCommand, int size, int handicap){
        this.size = size;
        this.handicap = handicap;
        this.gameCommand = gameCommand;
    }

    public int getHandicap() {
        return handicap;
    }

    public int getSize() {
        return size;
    }

    public GameCommand getGameCommand() {
        return gameCommand;
    }
}
