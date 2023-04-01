package pr_se.gogame.model;

import java.nio.file.Path;

public interface GameInterface {

    boolean saveGame();

    void newGame();

    boolean importGame(Path path);

    boolean exportGame(Path path);

    void pass();

    void resign();

    void scoreGame();
}
