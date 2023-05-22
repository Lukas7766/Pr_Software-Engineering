package pr_se.gogame.model;


public class AncientChineseRuleset implements Ruleset {

    private int currentKOCnt = 0;
    private Position koMove;

    @Override
    public boolean predicateKoMove(int x, int y) {

        System.out.println("predicateKoMove X:" + x + " Y: " + y);

        if (this.getKoAmount() > currentKOCnt) {
            currentKOCnt++;
            return false;
        }
        if (koMove == null) koMove = new Position(x, y);

        return koMove.X == x && koMove.Y == y;

    }

    public Position getKoMove() {
        return koMove;
    }

    @Override
    public void resetKoMove() {
        this.koMove = null;
        this.currentKOCnt = 0;
    }

    @Override
    public GameResult scoreGame(Game game) {

        if (game == null) throw new IllegalArgumentException();
        Board board = game.getBoard();

        int scoreBlack = 0;
        int scoreWhite = 0;

        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getColorAt(i, j) == StoneColor.BLACK) {
                    scoreBlack++;
                } else if (board.getColorAt(i, j) == StoneColor.WHITE) {
                    scoreWhite++;
                }

            }
        }

        System.out.println("Score Black: " + scoreBlack);
        System.out.println("Score White: " + scoreWhite);
        return new GameResult(1,1,null,"");

    }



    @Override
    public boolean hasDefaultHandicapPlacement() {
        return false;
    }

    @Override
    public void setHandicapStones(Board board, StoneColor beginner , int noStones) {
        switch (noStones) {
            case 9:
                board.setStone(board.getSize() / 2, board.getSize() / 2, beginner, true, true);
                noStones--;                                                     // set remaining no. to 8
            case 8:
                board.setStone(board.getSize() / 2, 3, board.getGAME().getCurColor(), true, true);
                board.setStone(board.getSize() / 2, board.getSize() - 4, beginner, true, true);
                noStones -= 2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
            default:
                break;
        }

        switch (noStones) {
            case 7:
                board.setStone(board.getSize() / 2, board.getSize() / 2, beginner, true, true); // I guess we could just run this anyway, at least if trying to re-occupy a field doesn't throw an exception, but skipping is faster.
                noStones--;
            case 6:
                board.setStone(board.getSize() - 4, board.getSize() / 2, beginner, true, true);
                board.setStone(3, board.getSize() / 2, beginner, true, true);
                noStones -= 2;
            default:
                break;
        }

        switch (noStones) {
            case 5:
                board.setStone(board.getSize() / 2, board.getSize() / 2, beginner, true, true);
            case 4:
                board.setStone(3, 3, board.getGAME().getCurColor(), true, true);
            case 3:
                board.setStone(board.getSize() / 2, board.getSize() / 2, beginner, true, true);
            case 2:
                board.setStone(board.getSize() - 4, 3, beginner, true, true);
                board.setStone(3, board.getSize() - 4, beginner, true, true);
            default:
                break;
        }
    }

}