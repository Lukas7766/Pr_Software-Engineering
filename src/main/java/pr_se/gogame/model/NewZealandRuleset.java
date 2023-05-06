package pr_se.gogame.model;

public class NewZealandRuleset implements Ruleset {

    @Override
    public void scoreGame(Board board) {

    }

    @Override
    public boolean getSuicide(StoneGroup group) {
        return group.getLocations().size() > 1;
    }

    @Override
    public boolean hasDefaultHandicapPlacement() {
        return false;
    }

    @Override
    public void setHandicapStones(Board board, int noStones) {
        board.getGAME().setHandicapStoneCounter(noStones);
    }
}
