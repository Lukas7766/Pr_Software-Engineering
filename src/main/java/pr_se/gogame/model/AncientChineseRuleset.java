package pr_se.gogame.model;


import java.util.Arrays;

public class AncientChineseRuleset implements Ruleset {

    private int currentKOCnt = 0;
    private Position koMove;
    private int lastBoardHash;

    @Override
    public UndoableCommand updateKoMove(int x, int y) {

        System.out.println("updateKoMove X: " + x + " Y: " + y);

        final int KO_AMOUNT = this.getKoAmount();
        final int OLD_KO_CNT = currentKOCnt;
        final Position OLD_KO_MOVE = koMove;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                if (KO_AMOUNT > OLD_KO_CNT) {
                    currentKOCnt++; // TODO: If this causes issues, maybe change to "OLD_KO_CNT + 1"?
                } else if (koMove == null) {
                    koMove = new Position(x, y);
                }
            }

            @Override
            public void undo() {
                koMove = OLD_KO_MOVE;
                currentKOCnt = OLD_KO_CNT;
            }
        };
        ret.execute();


        return ret;
    }

    @Override
    public UndoableCommand checkKoMove(int x, int y) {
        if(koMove != null) {
            if(koMove.X != x || koMove.Y != y) {
                return resetKoMove();
            }
            return null;
        }

        return null;
    }

    @Override
    public boolean isKoMove(int x, int y) {
        return koMove != null && koMove.X == x && koMove.Y == y;
    }

    @Override
    public UndoableCommand resetKoMove() {
        final Position OLD_KO_MOVE = this.koMove;
        final int OLD_KO_CNT = this.currentKOCnt;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                koMove = null;
                currentKOCnt = 0;
            }

            @Override
            public void undo() {
                currentKOCnt = OLD_KO_CNT;
                koMove = OLD_KO_MOVE;
            }
        };
        ret.execute();

        return ret;
    }

    @Override
    public UndoableCommand isKo(Game game) {
        StoneColor [][] boardColor = new StoneColor[game.getSize()][game.getSize()];

        for(int i = 0; i < game.getSize(); i++) {
            for(int j = 0; j < game.getSize(); j++) {
                boardColor[i][j] = game.getColorAt(i, j);
            }
        }

        final int LAST_BOARD_HASH = lastBoardHash;
        final int NEW_BOARD_HASH = Arrays.hashCode(boardColor);

        if(NEW_BOARD_HASH == LAST_BOARD_HASH) {
            return null;
        }

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                lastBoardHash = NEW_BOARD_HASH;
            }

            @Override
            public void undo() {
                lastBoardHash = LAST_BOARD_HASH;
            }
        };
        ret.execute();

        return ret;
    }

    @Override
    public GameResult scoreGame(Game game) {

        if (game == null) throw new IllegalArgumentException();

        int scoreBlack = 0;
        int scoreWhite = 0;

        for (int i = 0; i < game.getSize(); i++) {
            for (int j = 0; j < game.getSize(); j++) {
                if (game.getColorAt(i, j) == StoneColor.BLACK) {
                    scoreBlack++;
                } else if (game.getColorAt(i, j) == StoneColor.WHITE) {
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
    public void setHandicapStones(Game game, StoneColor beginner , int noStones) {
        game.setHandicapStoneCounter(noStones);
        switch (noStones) {
            case 9:
                game.placeHandicapStone(game.getSize() / 2, game.getSize() / 2);
                noStones--;                                                     // set remaining no. to 8
            case 8:
                game.placeHandicapStone(game.getSize() / 2, 3);
                game.placeHandicapStone(game.getSize() / 2, game.getSize() - 4);
                noStones -= 2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
            default:
                break;
        }

        switch (noStones) {
            case 7:
                game.placeHandicapStone(game.getSize() / 2, game.getSize() / 2); // I guess we could just run this anyway, at least if trying to re-occupy a field doesn't throw an exception, but skipping is faster.
                noStones--;
            case 6:
                game.placeHandicapStone(game.getSize() - 4, game.getSize() / 2);
                game.placeHandicapStone(3, game.getSize() / 2);
                noStones -= 2;
            default:
                break;
        }

        switch (noStones) {
            case 5:
                game.placeHandicapStone(game.getSize() / 2, game.getSize() / 2);
            case 4:
                game.placeHandicapStone(3, 3);
            case 3:
                game.placeHandicapStone(game.getSize() / 2, game.getSize() / 2);
            case 2:
                game.placeHandicapStone(game.getSize() - 4, 3);
                game.placeHandicapStone(3, game.getSize() - 4);
            default:
                break;
        }
    }

}
