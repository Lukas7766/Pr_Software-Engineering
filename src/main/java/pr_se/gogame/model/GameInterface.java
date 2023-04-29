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

    int getCurMoveNumber();

    StoneColor getCurColor();

    Ruleset getRuleset();

    FileSaver getFileSaver();

    /*
     *  Note by Gerald: I simply added this to GameInterface so that BoardPane could exclusively talk to Game, reducing
     * coupling. If anyone has a better, more generic idea for such a method, I'm entirely open to suggestions.
     */
    StoneColor getColorAt(int x, int y);

    int getHandicapStoneCounter();

    void setCurMoveNumber(int curMoveNumber);

    void setCurColor(StoneColor c);

    void setHandicapStoneCounter(int counter); // For rulesets with custom, manually placed handicap stones

    void fireGameEvent(GameEvent e);

    void playMove(int x, int y); // TODO: Maybe return boolean for move successful/unsuccessful?

    void placeHandicapStone(int x, int y); // For rulesets with custom handicap stones
}
