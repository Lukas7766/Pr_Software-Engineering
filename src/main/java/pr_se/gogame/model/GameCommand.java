package pr_se.gogame.model;

public enum GameCommand {

    INIT, NEW_GAME, GAME_WON, CONFIRM_CHOICE,
    GAME_IS_ONGOING, BLACK_STONE_SET, WHITE_STONE_SET, STONE_WAS_CAPTURED, HANDICAP_POS, // Added by Gerald

    //GamePlay Setting Commands
    CONFIG_DEMO_MODE,
    DEBUG_INFO
}
