package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.*;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;

public class NewZealandRuleset implements Ruleset {

    @Override
    public GameResult scoreGame(Game game) {
        return new GameResult();
    }

    @Override
    public boolean getSuicide(StoneGroup existingGroup, StoneGroup addedStone) {
        return existingGroup != addedStone;
    }

    @Override
    public UndoableCommand isKo(Game game) {
        return new UndoableCommand() {
            @Override
            public void execute(final boolean saveEffects) {
                // Returning this equates to telling the caller that ko has not occurred.
            }

            @Override
            public void undo() {
                // Returning this equates to telling the caller that ko has not occurred.
            }
        };
    }

    /**
     * The New Zealand Ruleset allows for free placement of handicap stones.
     * See <a href="https://en.wikipedia.org/wiki/Handicapping_in_Go#Free_placement">...</a>
     *
     * @return false, as this ruleset has manual handicap placement
     */
    @Override
    public boolean setHandicapStones(Game game, StoneColor beginner, int noStones) {
        if(game == null || beginner == null) {
            throw new NullPointerException();
        }

        if(noStones < 0 || noStones > 9) {
            throw new IllegalArgumentException();
        }

        return false;
    }
}
