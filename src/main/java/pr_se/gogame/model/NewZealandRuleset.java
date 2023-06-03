package pr_se.gogame.model;

public class NewZealandRuleset implements Ruleset {

    @Override
    public GameResult scoreGame(Game game) {
        return new GameResult(1, 1, null,"");
    }

    @Override
    public boolean getSuicide(StoneGroup existingGroup, StoneGroup addedStone) {
        // return existingGroup.getLocations().size() > 1; // Old check when newGroup used to optimistically be added to firstSameColorGroup
        return existingGroup != addedStone;
    }

    @Override
    public UndoableCommand updateKoMove(int x, int y) {
        return null;
    }

    @Override
    public UndoableCommand checkKoMove(int x, int y) {
        return null;
    }

    @Override
    public boolean isKoMove(int x, int y) {
        return false;
    }

    @Override
    public Position getKoMove() {
        return null;
    }

    private UndoableCommand resetKoMove() {
        return null;
    }

    /**
     * The New Zealand Ruleset allows for free placement of handicap stones. See https://en.wikipedia.org/wiki/Handicapping_in_Go#Free_placement
     * @return false
     */
    @Override
    public boolean hasDefaultHandicapPlacement() {
        return false;
    }

    @Override
    public void setHandicapStones(Game game, StoneColor beginner, int noStones) {
        if(game == null || beginner == null) {
            throw new NullPointerException();
        }

        if(noStones < 0 || noStones > 9) {
            throw new IllegalArgumentException();
        }

        game.setHandicapStoneCounter(noStones);
    }
}
