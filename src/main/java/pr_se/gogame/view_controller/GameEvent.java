package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;

public class GameEvent {

    private final GameCommand gameCommand;
    private final int size;
    private final int komi;

    public GameEvent(GameCommand gameCommand){
        this(gameCommand,-1,-1);
    }

    public GameEvent(GameCommand gameCommand, int size, int komi){
        this.size = size;
        this.komi = komi;
        this.gameCommand = gameCommand;
    }

    public int getKomi() {
        return komi;
    }

    public int getSize() {
        return size;
    }

    public GameCommand getGameCommand() {
        return gameCommand;
    }
}
