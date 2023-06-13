package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;

public interface GameInterface {

    //##################################################################################################################
    //game operations

    /**
     * Makes the game fire an initialisation event that notifies listening components so they may initialise themselves.
     */
    void initGame();

    /**
     * Starts a new game.
     * @param startingColor the StoneColor of the starting player
     * @param size the size of the board
     * @param handicap how many handicap stones are placed in favor of the beginner
     */
    void newGame(StoneColor startingColor, int size, int handicap);

    boolean loadGame(Path path);

    boolean saveGame();

    /**
     * Allows the current player to pass. After this, it is the opposite player's turn.
     */
    void pass();

    /**
     * Allows the current player to resign. This causes the opposite player to win.
     */
    void resign();

    /**
     * If moves have to be confirmed, this method confirms them.
     */
    void confirmChoice();

    /**
     * This method uses the Game's Ruleset to calculate the score of each player.
     */
    void scoreGame();

    /**
     * This method places a stone down for the current player at the specified coordinates
     * @param x the x coordinate of the stone, starting at the left
     * @param y the y coordinate of the stone, starting at the top
     */
    void playMove(int x, int y); // TODO: Maybe return boolean for move successful/unsuccessful?

    /**
     * This method places a handicap stone down for the beginner player at the specified coordinates. This only works
     * at the beginning of the game.
     *
     * @param x          the x coordinate of the stone, starting at the left
     * @param y          the y coordinate of the stone, starting at the top
     * @param placeStone
     */
    void placeHandicapPosition(int x, int y, boolean placeStone);


    //##################################################################################################################
    //game settings

    boolean isDemoMode();

    void setDemoMode(boolean demoMode);
    GameCommand getGameState();

    int getSize();

    int getHandicap();

    double getKomi();
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

