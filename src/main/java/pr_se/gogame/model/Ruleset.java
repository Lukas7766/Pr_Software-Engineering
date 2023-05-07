package pr_se.gogame.model;

public interface Ruleset {
    /**
     * Because nothing is ever easy, some rulesets permit suicide, at least if it is collective suicide (apparently this
     * can cause the opponent some inconvenience). See https://en.wikipedia.org/wiki/Rules_of_Go#Suicide. To check
     * whether suicide is solitary or collective, the ruleset needs to see the board.
     *
     * @param group The group that is about to commit suicide
     * @return whether the ruleset permits suicide under the given cirucmstances on the board.
     */
    default boolean getSuicide(StoneGroup group) {
        return false;
    }

    default int getKoAmount() {
        return 2;
    }

    boolean predicateKoMove(int x, int y);

    Position getKoMove();

    void resetKoMove();

    /** Evaluation is the central feature of a set of rules; It varies depending on the set of rules.
     *7.1 A game is played until both parties agree that it is
     * finished.
     * 7.2 During the game, if one player resigns, the game
     * is finished.
     * 7.3 If both players pass one after the other, the game
     * is finished.
     */
    void scoreGame(Board board);

    /**
     * @return whether the RuleSet uses the default placement of handicap stones or not
     */
    default boolean hasDefaultHandicapPlacement() {
        return true;
    }

    /**
     * Places custom handicap stones according to the ruleset, either by calling the Game.setHandicapStone method
     * for automatic placement, or by setting the handicap stone counter of Game for manual placement.
     *
     * @param board The board that these handicap stones are to be set for (this is used to get the game, as well).
     * @param noStones The number of handicap stones to be placed
     */
    default void setHandicapStones(Board board, int noStones) {}


    default double getKomi(){
        return 7.5;
    }
}
