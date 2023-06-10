package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;

public class GameEvent {

    private final GameCommand gameCommand;

    public GameEvent(GameCommand gameCommand){
        this.gameCommand = gameCommand;
    }

    public GameCommand getGameCommand() {
        return gameCommand;
    }
}
