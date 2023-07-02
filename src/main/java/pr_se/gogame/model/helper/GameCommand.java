package pr_se.gogame.model.helper;

public enum GameCommand {

    INIT,               // Used after requesting a new game but before starting it (resets the GUI to the game settings selection mode
    NEW_GAME,           // Used after actually starting a new game
    GAME_WON,           // Used after a game has been won and thus completed (could also be considered "GAME OVER")
    UPDATE,             // Used after every move, and generally to cause the GUI to update; is basically the normal state of the game
    HANDICAP_SET,       // Used to indicate that a handicap position has been set (if the GameEvent's color is null, this means that only the slot is to be displayed)
    HANDICAP_REMOVED,    // Used to indicate that a handicap position has been removed
    SETUP_STONE_SET,    // Used to indicate that a setup stone other than a handicap stone was set.
    STONE_WAS_SET,      // Used to indicate that a stone has been set
    STONE_WAS_REMOVED,  // Used to indicate that a stone has been removed
    MARK_CIRCLE,        // Used to mark a certain position with a circle
    MARK_SQUARE,        // Used to mark a certain position with a square
    MARK_TRIANGLE,      // Used to mark a certain position with a triangle
    UNMARK,             // Used to unmark a certain position
    DEBUG_INFO;         // Used to trigger debug output

    @Override
    public String toString() {
        return "GameCommand " + this;
    }
}
