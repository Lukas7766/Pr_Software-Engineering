package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;

public interface GameInterface {

    boolean saveGame(Path path);

    void initGame();
    void newGame(GameCommand gameCommand, int size, int komi);

    boolean importGame(Path path);

    boolean exportGame(Path path);

    void pass();

    void resign();

    void scoreGame();

    int getSize();

    int getKomi();

    void addListener(GameListener l);

    void removeListener(GameListener l);

    GameCommand getGameState();

    void confirmChoice();

    Board getBoard();

    void fireGameEvent(GameEvent e);
}
