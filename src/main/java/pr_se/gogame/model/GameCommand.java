package pr_se.gogame.model;

public enum GameCommand {

    INIT,               // Used after requesting a new game but before starting it (resets the GUI to the game settings selection mode.
    NEW_GAME,           // Used after actually starting a new game
    GAME_WON,           // Used after a game has been won and thus completed (could also be considered "GAME OVER")
    CONFIRM_CHOICE,     // Used when a choice has been confirmed. Might be better suited in the view, rather than the model
    COLOR_HAS_CHANGED,  // Used after every move; is basically the normal state of the game
    STONE_WAS_SET,      // Used to indicate that a stone has been set (also used for indicating handicap slot positions)
    STONE_WAS_CAPTURED, // Used to indicate that a stone has been removed
    DEBUG_INFO          // Used to trigger debug output; TODO: might be removed
}
