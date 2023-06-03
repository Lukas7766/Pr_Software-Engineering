package pr_se.gogame.model;


import java.util.Arrays;

public class AncientChineseRuleset implements Ruleset {
    private int lastBoardHash;

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
