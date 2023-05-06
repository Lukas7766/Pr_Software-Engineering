package pr_se.gogame.model;

public class JapaneseRuleset implements Ruleset {

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
    public void scoreGame(Board board) {

        System.out.println();

    }
}
