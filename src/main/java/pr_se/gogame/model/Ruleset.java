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
     * @return whether the last move was a ko move.
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
        game.setHandicapStoneCounter(noStones);
        switch (noStones) {
            case 9:
                game.placeHandicapStone(SIZE / 2, SIZE / 2);
                noStones--;                                                     // set remaining no. to 8
            case 8:
                game.placeHandicapStone(SIZE / 2, 3);
                game.placeHandicapStone(SIZE / 2, SIZE - 4);
                noStones -= 2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
            default:
                break;
        }

        switch (noStones) {
            case 7:
                game.placeHandicapStone(SIZE / 2, SIZE / 2); // I guess we could just run this anyway, at least if trying to re-occupy a field doesn't throw an exception, but skipping is faster.
                noStones--;
            case 6:
                game.placeHandicapStone(SIZE - 4, SIZE / 2);
                game.placeHandicapStone(3, SIZE / 2);
                noStones -= 2;
            default:
                break;
        }

        switch (noStones) {
            case 5:
                game.placeHandicapStone(SIZE / 2, SIZE / 2);
            case 4:
                game.placeHandicapStone(3, 3);
            case 3:
                game.placeHandicapStone(SIZE - 4, SIZE - 4);
            case 2:
                game.placeHandicapStone(SIZE - 4, 3);
                game.placeHandicapStone(3, SIZE - 4);
            default:
                break;
        }
    }

    /**
     * @return the default komi value for the ruleset
     */
    default double getKomi(){
        return 7.5;
    }
}
