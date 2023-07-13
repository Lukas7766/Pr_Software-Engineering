package pr_se.gogame.model.helper;

/**
 * Stores game commands that the Game may use for updating the view.
 */
public enum GameCommand {

    /**
     * Used after requesting a new game but before starting it (resets the GUI to the game settings selection mode
     */
    INIT,

    /**
     * Used after actually starting a new game
     */
    NEW_GAME,

    /**
     * Used after a game has been won and thus completed (could also be considered "GAME OVER")
     */
    GAME_WON,

    /**
     * Used after every move, and generally to cause the GUI to update; could be considered the default value.
     */
    UPDATE,

    /**
     * Used to indicate that a handicap position has been set (i.e., that a slot has to be displayed)
     */
    HANDICAP_SET,

    /**
     * Used to indicate that a handicap position has been removed
     */
    HANDICAP_REMOVED,

    /**
     * Used to indicate that a setup stone other than a handicap stone was set.
     */
    SETUP_STONE_SET,

    /**
     * Used to indicate that a stone has been set
     */
    STONE_WAS_SET,

    /**
     * Used to indicate that a stone has been removed
     */
    STONE_WAS_REMOVED,

    /**
     * Used to mark a certain position with a circle
     */
    MARK_CIRCLE,

    /**
     * Used to mark a certain position with a square
     */
    MARK_SQUARE,

    /**
     * Used to mark a certain position with a triangle
     */
    MARK_TRIANGLE,

    /**
     * Used to unmark a certain position
     */
    UNMARK,

    /**
     * Used to trigger debug output
     */
    DEBUG_INFO;

    /**
     * Returns a String representation of this GameCommand
     * @return a String representation of this GameCommand
     */
    @Override
    public String toString() {
        return "GameCommand " + this;
    }
}
