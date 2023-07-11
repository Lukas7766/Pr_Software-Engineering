package pr_se.gogame.model;

import pr_se.gogame.model.file.FileHandler;
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.ruleset.GameResult;
import pr_se.gogame.model.ruleset.Ruleset;
import pr_se.gogame.view_controller.observer.GameListener;

public interface GameInterface {

    //##################################################################################################################
    //game operations

    /**
     * Makes the game fire an initialisation event cueing listening components to initialise themselves.
     */
    void initGame();


    void newGame(StoneColor startingColor, int size, int handicap, Ruleset ruleset);

    /**
     * Starts a new game.
     * @param startingColor the StoneColor of the starting player
     * @param size          the size of the board
     * @param handicap      how many handicap stones are placed in favor of the beginner
     * @param ruleset       the Ruleset that this Game uses
     * @param letRulesetPlaceHandicapStones whether the Game should let the Ruleset place handicap stones (set to false when loading a game from a file)
     */
    void newGame(StoneColor startingColor, int size, int handicap, Ruleset ruleset, boolean letRulesetPlaceHandicapStones);

    /**
     * Allows the current player to pass. After this, it is the opposite player's turn.
     */
    void pass();

    /**
     * Allows the current player to resign. This causes the opposite player to win.
     */
    void resign() throws IllegalStateException;

    /**
     * This method uses the Game's Ruleset to calculate the score of each player.
     */
    void scoreGame() throws IllegalStateException;

    /**
     * This method places a stone down for the current player at the specified coordinates
     * @param x the x coordinate of the stone, starting at the left
     * @param y the y coordinate of the stone, starting at the top
     */
    boolean playMove(int x, int y);

    boolean playMove(int x, int y, StoneColor color);

    void placeHandicapPosition(int x, int y, boolean placeStone);

    /**
     * This method places a handicap stone down for the beginner player at the specified coordinates. This only works
     * at the beginning of the game.
     *
     * @param x          the x coordinate of the stone, starting at the left
     * @param y          the y coordinate of the stone, starting at the top
     * @param placeStone whether a stone is to be placed. If false, the handicap slot is still placed.
     * @param color      the color of the stone to be placed - if any
     */
    void placeHandicapPosition(int x, int y, boolean placeStone, StoneColor color);


    /**
     * Takes the position for a stone and determines what to do with it based on Game's internal state. Useful for GUI
     * components that determine the position of a placed stone themselves, but nothing else.
     * @param x the x coordinate of the stone, starting at the left
     * @param y the y coordinate of the stone, starting at the top
     */
    void usePosition(int x, int y);


    //##################################################################################################################
    //game settings
    Game.GameState getGameState();

    int getSize();

    int getHandicap();

    double getKomi();

    //##################################################################################################################
    //game information

    int getCurMoveNumber();

    void placeSetupStone(int x, int y, StoneColor color);

    StoneColor getColorAt(int x, int y);

    StoneColor getCurColor();

    Ruleset getRuleset();

    GameResult getGameResult();

    //##################################################################################################################
    // Methods regarding "move metadata"
    String getComment();

    void setComment(String comment);

    void mark(int x, int y, MarkShape shape);

    void unmark(int x, int y);
    //##################################################################################################################
    //Observer pattern
    void addListener(GameListener l);

    void removeListener(GameListener l);

    void setSetupMode(boolean setupMode);

    boolean isSetupMode();

    //##################################################################################################################
    // Methods related to file handling
    FileHandler getFileHandler();

}

