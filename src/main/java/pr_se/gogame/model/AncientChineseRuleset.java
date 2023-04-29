package pr_se.gogame.model;

public class AncientChineseRuleset implements Ruleset {

    @Override
    public void scoreGame() {

    }

    @Override
    public boolean hasDefaultHandicapPlacement() {
        return false;
    }

    @Override
    public void setHandicapStones(Board board, int noStones) {
        switch (noStones) {
            case 9:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true);
                noStones--;                                                     // set remaining no. to 8
            case 8:
                board.setStone(board.getSize() / 2, 3, board.getGAME().getCurColor(), true);
                board.setStone(board.getSize() / 2, board.getSize() - 4, board.getGAME().getCurColor(), true);
                noStones -= 2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
            default:
                break;
        }

        switch (noStones) {
            case 7:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true); // I guess we could just run this anyway, at least if trying to re-occupy a field doesn't throw an exception, but skipping is faster.
                noStones--;
            case 6:
                board.setStone(board.getSize() - 4, board.getSize() / 2, board.getGAME().getCurColor(), true);
                board.setStone(3, board.getSize() / 2, board.getGAME().getCurColor(), true);
                noStones -= 2;
            default:
                break;
        }

        switch (noStones) {
            case 5:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true);
            case 4:
                board.setStone(3, 3, board.getGAME().getCurColor(), true);
            case 3:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true);
            case 2:
                board.setStone(board.getSize() - 4, 3, board.getGAME().getCurColor(), true);
                board.setStone(3, board.getSize() - 4, board.getGAME().getCurColor(), true);
            default:
                break;
        }
    }
}
