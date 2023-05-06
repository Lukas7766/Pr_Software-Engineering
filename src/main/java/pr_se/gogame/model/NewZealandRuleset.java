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

    @Override
    public boolean hasDefaultHandicapPlacement() {
        return false;
    }

    @Override
    public void setHandicapStones(Board board, int noStones) {
        board.getGAME().setHandicapStoneCounter(noStones);
    }
}
