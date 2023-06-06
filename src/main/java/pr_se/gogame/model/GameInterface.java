package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;

public interface GameInterface {

    //##################################################################################################################
    //game operations
    void initGame();
    void newGame(GameCommand gameCommand, int size, int handicap);
    boolean loadGame(Path path);

    boolean saveGame();

    void pass();

    void resign();

    void confirmChoice();

    void scoreGame();

    void playMove(int x, int y); // TODO: Maybe return boolean for move successful/unsuccessful?

    void placeHandicapStone(int x, int y); // For rulesets with custom handicap stones


    //##################################################################################################################
    //game settings

    boolean isDemoMode();

    void setDemoMode(boolean demoMode);
    GameCommand getGameState();

    int getSize();

    int getHandicap();

    double getKomi();

    boolean isConfirmationNeeded();

    void setConfirmationNeeded(boolean needed);

    boolean isShowMoveNumbers();

    void setShowMoveNumbers(boolean show);

    boolean isShowCoordinates();

    void setShowCoordinates(boolean show);
    //##################################################################################################################
    //game information

    int getCurMoveNumber();

    int getStonesCapturedBy(StoneColor color);

    UndoableCommand addCapturedStones(StoneColor color, int amount);

    StoneColor getColorAt(int x, int y);

    StoneColor getCurColor();

    Board getBoard();

    Ruleset getRuleset();

    FileTree getFileTree();

    /*
     *  Note by Gerald: I simply added this to GameInterface so that BoardPane could exclusively talk to Game, reducing
     * coupling. If anyone has a better, more generic idea for such a method, I'm entirely open to suggestions.
     */

    int getHandicapStoneCounter();

    //void fireGameEvent(GameEvent e); //delete for getting fireGameEvent package private

    double getScore(StoneColor color);

    GameResult getGameResult();

    //##################################################################################################################
    //Oberserver pattern
    void addListener(GameListener l);

    void removeListener(GameListener l);

    void setHandicapStoneCounter(int noStones);

    String getGraphicsPath();

    void setGraphicsPath(String path);

    Path getSavePath();

    void setSavePath(Path path);
}

