package pr_se.gogame.model;

public enum GameCommand {

    INIT, WHITE_STARTS, BLACK_STARTS, WHITE_WON, BLACK_WON, CONFIRM_CHOICE,
    BLACK_PLAYS, WHITE_PLAYS, BLACK_STONE_SET, WHITE_STONE_SET, STONE_WAS_CAPTURED, HANDICAP_POS, // Added by Gerald

    //GamePlay Setting Commands
    CONFIG_DEMO_MODE,
    DEBUG_INFO
}
