package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameListener;
import pr_se.gogame.view_controller.GoListener;

import java.nio.file.Path;

public interface GameInterface {

    boolean saveGame();

    void newGame();
    void newGame(int size, int komi);

    boolean importGame(Path path);

    boolean exportGame(Path path);

    void pass();

    void resign();

    void scoreGame();

    int getSize();

    int getKomi();

    void addListener(GameListener l);

    void removeListener(GameListener l);
}
