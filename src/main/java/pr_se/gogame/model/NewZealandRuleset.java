package pr_se.gogame.model;

public class NewZealandRuleset implements Ruleset {

    @Override
    public GameResult scoreGame(Game game) {
        return new GameResult(1, 1, null,"");
    }

    @Override
    public boolean getSuicide(StoneGroup group) {
        return group.getLocations().size() > 1;
    }

    @Override
    public boolean predicateKoMove(int x, int y) {
        return false;
    }

    @Override
    public Position getKoMove() {
        return null;
    }

    @Override
    public void resetKoMove() {

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
