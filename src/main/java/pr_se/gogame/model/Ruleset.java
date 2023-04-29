package pr_se.gogame.model;

public interface Ruleset {

    /*
     * Just Gerald's two cents:
     * We should consider whether we want to allow for custom rulesets.
     * As a standard ruleset is unlikely to change during gameplay, it seems better not to use instance variables
     * at all and simply implement the behaviour of different rulesets by overriding methods. This would remove
     * the necessity of setters. However, if custom rulesets are to be possible, having instance variables would be
     * necessary to let the user customize a ruleset.
     */
    /** Depending on the rules, suicide is allowed or forbidden. <br>
     * -> set to true if you want to allow it <br>
     * -> set to false if you want to forbid it <br>
     * @param allow (true or false)
     */
    void setSuicide(boolean allow);

    /** Depending on the rules, suicide is allowed or forbidden. This is the default option to forbid suicide. <br>
     * @return false
     */
    default boolean setDefaultSuicide() {
        return false;
    }

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

    /** To prevent endless repetitions or make them pointless, positional repetition is restricted. <br>
     * This method sets the amount of restricted repetitions.
     * @param amount (2 ... n)
     */
    void setKoAmount(int amount);

    /** To prevent endless repetitions or make them pointless, positional repetition is restricted. <br>
     * Two consecutive moves (except passing) may not restore the original position.
     * @return 2
     */
    default int setDefaultKoAmount() {
        return 2;
    }

    /** Evaluation is the central feature of a set of rules; It varies depending on the set of rules.
     *
     */
    void scoreGame();

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
    default void setHandicapStones(Board board, int noStones) {
        return;
    }

    //Kompensationspunkte
}
