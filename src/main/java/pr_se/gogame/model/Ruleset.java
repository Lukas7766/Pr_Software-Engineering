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

    /** KO is a special rule that prevents immediate repetition of position, in which a single stone is captured and
     *  another single stone immediately taken back. <br> Depending on the ruleset a number of allowed repetition is given.
     *  <br> The default value is 2.
     * @return the default value two as move repetition is allowed twice.
     */
    default int getKoAmount() {
        return 2;
    }

    /** This method predicates if the current move matches the KO criteria.
     *
     * @param x pass the X-axis of the verifiable move
     * @param y pass the y-axis of the verifiable move
     * @return true if a KO move was tried or return false if it isn't a KO move
     */
    boolean predicateKoMove(int x, int y);

    /** The non-repeatable KO move is stored in Position.
     *
     * @return the position of the non-repeatable KO move
     */
    Position getKoMove();

    /**
     * This method resets the KO move.
     */
    void resetKoMove();

    /** This method calculates the score of the game for both players.
     *
     * @return the score of the game for both players in an array of size 2. Index 0 is the score of black, index 1 is the score of white.
     */
    int[] scoreGame(Board board);

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

    /**
     * @return the default komi value for the ruleset
     */
    default double getKomi(){
        return 7.5;
    }
}
