package pr_se.gogame.model;

public interface Ruleset {
    /**
     * Because nothing is ever easy, some rulesets permit suicide, at least if it is collective suicide (apparently this
     * can cause the opponent some inconvenience). See <a href="https://en.wikipedia.org/wiki/Rules_of_Go#Suicide">the
     * Wikipedia article</a>. To check whether suicide is solitary or collective, the ruleset needs the involved
     * StoneGroup.
     *
     * @param existingGroup The group that is about to commit suicide
     * @param addedStone The stone that was added to the existingGroup
     * @return whether the ruleset permits suicide under the given cirucmstances on the board.
     */
    default boolean getSuicide(StoneGroup existingGroup, StoneGroup addedStone) {
        return false;
    }

    /** Ko is a special rule that prevents immediate repetition of position, in which a single stone is captured and
     *  another single stone immediately taken back. <br> Depending on the ruleset a number of allowed repetition is given.
     *  <br> The default value is 2.
     * @return the default value is two as move repetition is allowed twice.
     */
    default int getKoAmount() {
        return 2;
    }

    /**
     * Whether a ko move has just been committed.
     * @param game The Game that is to be checked for ko.
     * @return null if the last move was a ko move, otherwise an UndoableCommand to undo or redo the ko check.
     */
    UndoableCommand isKo(Game game);

    /** This method calculates the score of the game for both players.
     *
     * @return the score of the game for both players in an array of size 2. Index 0 is the score of black, index 1 is the score of white.
     */
    GameResult scoreGame(Game game);

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
     * @param game The game that these handicap stones are to be set for.
     * @param noStones The number of handicap stones to be placed
     */
    default void setHandicapStones(Game game, StoneColor beginner, int noStones) {
        /*
         * This is a default implementation, the ancient Chinese ruleset has a different placement for 3, and the
         *  New-Zealand-Ruleset, among others, permits free placement of handicap stones. That is why a ruleset
         *  may override this.
         */
        if (game == null) {
            throw new IllegalArgumentException("board must not be null");
        }
        if (beginner == null) {
            throw new IllegalArgumentException("beginner must not be null");
        }
        if (noStones < 0 || noStones > 9){
            throw new IllegalArgumentException("noStones must be between 0 and 9");
        }

        final int SIZE = game.getSize();
        final int DIST_FROM_EDGE = 2 + SIZE / 10;
        game.setHandicapStoneCounter(noStones);

        game.placeHandicapPosition(SIZE / 2, SIZE / 2, noStones == 9);
        if(noStones == 9) noStones--;
        game.placeHandicapPosition(SIZE / 2, DIST_FROM_EDGE, noStones == 8);
        game.placeHandicapPosition(SIZE / 2, SIZE - 1 - DIST_FROM_EDGE, noStones == 8);
        if(noStones == 8) noStones -= 2;
        game.placeHandicapPosition(SIZE / 2, SIZE / 2, noStones == 7);
        if(noStones == 7) noStones--;
        game.placeHandicapPosition(SIZE - 1 - DIST_FROM_EDGE, SIZE / 2, noStones == 6);
        game.placeHandicapPosition(DIST_FROM_EDGE, SIZE / 2, noStones == 6);
        if(noStones == 6) noStones -= 2;
        game.placeHandicapPosition(SIZE / 2, SIZE / 2, noStones == 5);
        if(noStones == 5) noStones--;
        game.placeHandicapPosition(DIST_FROM_EDGE, DIST_FROM_EDGE, noStones == 4);
        if(noStones == 4) noStones--;
        game.placeHandicapPosition(SIZE - 1 - DIST_FROM_EDGE, SIZE - 1 - DIST_FROM_EDGE, noStones == 3);
        if(noStones == 3) noStones--;
        game.placeHandicapPosition(SIZE - 1 - DIST_FROM_EDGE, DIST_FROM_EDGE, noStones == 2);
        game.placeHandicapPosition(DIST_FROM_EDGE, SIZE - 1 - DIST_FROM_EDGE, noStones == 2);
    }

    /**
     * Call this method when starting a new game.
     */
    default void reset() {
        return;
    }

    /**
     * @return the default komi value for the ruleset
     */
    default double getKomi(){
        return 7.5;
    }
}
