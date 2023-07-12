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

    /**
     * Starts a new game.
     * @param startingColor the StoneColor of the starting player
     * @param size          the size of the board
     * @param handicap      how many handicap stones are placed in favor of the beginner
     * @param ruleset       the Ruleset that this Game uses
     */
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
     * @return whether the move was permitted and thus successful
     */
    boolean playMove(int x, int y);

    /**
     * This method places a stone down for the supplied player color at the specified coordinates
     * @param x the x coordinate of the stone, starting at the left
     * @param y the y coordinate of the stone, starting at the top
     * @param color the StoneColor of the player
     * @return whether the move was permitted and thus successful
     */
    boolean playMove(int x, int y, StoneColor color);

    /**
     * This method places a handicap stone or slot down for the beginner player at the specified coordinates.
     * This only works at the beginning of the game.
     * @param x          the x coordinate of the stone, starting at the left
     * @param y          the y coordinate of the stone, starting at the top
     * @param placeStone whether a stone is to be placed. If false, the handicap slot is still placed.
     */
    void placeHandicapPosition(int x, int y, boolean placeStone);

    /**
     * This method places a handicap stone or slot down for the player of the specified color at the specified
     * coordinates. This only works at the beginning of the game.
     * @param x          the x coordinate of the stone, starting at the left
     * @param y          the y coordinate of the stone, starting at the top
     * @param placeStone whether a stone is to be placed. If false, the handicap slot is still placed.
     * @param color      the color of the stone to be placed, if any
     */
    void placeHandicapPosition(int x, int y, boolean placeStone, StoneColor color);

    /**
     * This method places a setup stone down for the player of the specified color
     * @param x         the x coordinate of the stone, starting at the left
     * @param y         the y coordinate of the stone, starting at the top
     * @param color     the color of the stone to be placed
     */
    void placeSetupStone(int x, int y, StoneColor color);

    /**
     * Takes the position for a stone and determines what to do with it based on Game's internal state. Useful for GUI
     * components that determine the position of a placed stone themselves, but nothing else.
     * @param x the x coordinate of the stone, starting at the left
     * @param y the y coordinate of the stone, starting at the top
     */
    void usePosition(int x, int y);


    //##################################################################################################################
    //game information

    /**
     * @return the current state of this Game
     */
    GameInterface.GameState getGameState();

    /**
     * @return the Game's board size
     */
    int getSize();

    /**
     * @return the current Game's handicap
     */
    int getHandicap();

    /**
     * @return the current Ruleset's komi
     */
    double getKomi();

    /**
     * Returns the StoneColor of the board at the specified coordinates
     * @param x X coordinate starting at the left
     * @param y Y coordinate starting at the top
     * @return the StoneColor of the board at the specified coordinates
     */
    StoneColor getColorAt(int x, int y);

    /**
     * @return the current move number
     */
    int getCurMoveNumber();

    /**
     * @return the StoneColor of the player whose turn it currently is
     */
    StoneColor getCurColor();

    /**
     * @return the Game's current ruleset
     */
    Ruleset getRuleset();

    /**
     * @return the current state of the Game's result
     */
    GameResult getGameResult();

    //##################################################################################################################
    // Methods regarding "move metadata"

    /**
     * @return the comment of the current move
     */
    String getComment();

    /**
     * Sets the comment of the current move
     * @param comment the comment to be added
     */
    void setComment(String comment);

    /**
     * Marks the supplied coordinates with the supplied shape
     * @param x X coordinate starting at the left
     * @param y Y coordinate starting at the top
     * @param shape the desired mark shape
     */
    void mark(int x, int y, MarkShape shape);

    /**
     * Removes any marks from the supplied coordinates
     * @param x X coordinate starting at the left
     * @param y Y coordinate starting at the top
     */
    void unmark(int x, int y);
    //##################################################################################################################
    //Observer pattern

    /**
     * Adds the supplied listener to this Game
     * @param l the listener to be added
     */
    void addListener(GameListener l);

    /**
     * Removes the supplied listener from this Game
     * @param l the listener to be removed
     */
    void removeListener(GameListener l);

    /**
     * Switches the game's mode for placing setup stones on (if true) or off (if false). Does not work if the game
     * is in Handicap Mode.
     * @param setupMode Whether the game's setup mode is supposed to be switched on
     */
    void setSetupMode(boolean setupMode);

    /**
     * @return whether the Game is currently in setup mode
     */
    boolean isSetupMode();

    /**
     * Updates the GUI with debug info.
     */
    void printDebugInfo();

    //##################################################################################################################
    // Methods related to file handling

    /**
     * @return the Game's current FileHandler
     */
    FileHandler getFileHandler();

    /**
     * @return the Game's History
     */
    History getHistory();

    /**
     * Coontains the possible states of a game
     */
    enum GameState {
        /**
         * Used between initialising and starting a new game
         */
        NOT_STARTED_YET,
        /**
         * Used after starting a new game but before the first move can be made
         */
        SETTING_UP,
        /**
         * Used during the game
         */
        RUNNING,
        /**
         * Used after the game has ended
         */
        GAME_OVER
    }
}

