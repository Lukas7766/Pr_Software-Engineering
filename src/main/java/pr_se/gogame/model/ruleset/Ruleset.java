package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.*;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;

public interface Ruleset {
    /**
     * Because nothing is ever easy, some rulesets permit suicide, at least if it is collective suicide (apparently this
     * can cause the opponent some inconvenience). See <a href="https://en.wikipedia.org/wiki/Rules_of_Go#Suicide">the
     * Wikipedia article</a>. To check whether suicide is solitary or collective, the ruleset needs the involved
     * StoneGroup.
     *
     * @param existingGroup The group that is about to commit suicide
     * @param addedStone The stone that was added to the existingGroup
     * @return whether the ruleset permits suicide under the given circumstances on the board.
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

    /**
     * This method calculates the score of the game for both players.
     *
     * @return
     */
    UndoableCommand scoreGame(Game game);

    /**
     * Places custom handicap stones according to the ruleset, either by calling the Game.setHandicapStone method
     * for automatic placement, or by setting the handicap stone counter of Game for manual placement.
     *
     * @param game     The game that these handicap stones are to be set for.
     * @param noStones The number of handicap stones to be placed
     * @return true if the ruleset has automatic handicap placement, false if it has manual placement.
     */
    default boolean setHandicapStones(Game game, StoneColor beginner, int noStones) {
        /*
         * This is a default implementation, the ancient Chinese ruleset has a different placement for 3, and the
         *  New-Zealand-Ruleset, among others, permits free placement of handicap stones. Thus, a ruleset may override
         *  this.
         */
        if (game == null) {
            throw new NullPointerException("game must not be null");
        }
        if (beginner == null) {
            throw new NullPointerException("beginner must not be null");
        }
        if (noStones < Game.MIN_HANDICAP_AMOUNT || noStones > Game.MAX_HANDICAP_AMOUNT){
            throw new IllegalArgumentException("noStones must be between " + Game.MIN_HANDICAP_AMOUNT + " and " + Game.MAX_HANDICAP_AMOUNT);
        }

        final int size = game.getSize();
        final int distFromEdge = 2 + size / 10;

        boolean centerSet = false;

        if(noStones == 9) {
            game.placeHandicapPosition(size / 2, size / 2, true);
            centerSet = true;
            noStones--;
        }
        game.placeHandicapPosition(size / 2, distFromEdge, noStones == 8);
        game.placeHandicapPosition(size / 2, size - 1 - distFromEdge, noStones == 8);
        if(noStones == 8) noStones -= 2;
        if(noStones == 7) {
            game.placeHandicapPosition(size / 2, size / 2, true);
            centerSet = true;
            noStones--;
        }
        game.placeHandicapPosition(size - 1 - distFromEdge, size / 2, noStones == 6);
        game.placeHandicapPosition(distFromEdge, size / 2, noStones == 6);
        if(noStones == 6) noStones -= 2;
        if(!centerSet) {
            game.placeHandicapPosition(size / 2, size / 2, noStones == 5);
        }
        if (noStones == 5) noStones--;
        game.placeHandicapPosition(distFromEdge, distFromEdge, noStones == 4);
        if(noStones == 4) noStones--;
        game.placeHandicapPosition(size - 1 - distFromEdge, size - 1 - distFromEdge, noStones == 3);
        if(noStones == 3) noStones--;
        game.placeHandicapPosition(size - 1 - distFromEdge, distFromEdge, noStones == 2);
        game.placeHandicapPosition(distFromEdge, size - 1 - distFromEdge, noStones == 2);

        return true;
    }

    /**
     * Call this method when starting a new game. This causes the ruleset to reset any private bookkeeping.
     */
    default void reset() {
    }

    /**
     * @return the default komi value for the ruleset
     */
    default double getKomi(){
        return 7.5;
    }
}
