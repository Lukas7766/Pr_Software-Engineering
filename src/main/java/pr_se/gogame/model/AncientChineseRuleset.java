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
            public void execute(boolean saveEffects) {
                lastBoardHash = NEW_BOARD_HASH;
            }

            @Override
            public void undo() {
                lastBoardHash = LAST_BOARD_HASH;
            }
        };
        ret.execute(true);

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
                game.placeHandicapPosition(game.getSize() / 2, game.getSize() / 2, true);
                noStones--;                                                     // set remaining no. to 8
            case 8:
                game.placeHandicapPosition(game.getSize() / 2, 3, true);
                game.placeHandicapPosition(game.getSize() / 2, game.getSize() - 4, true);
                noStones -= 2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
            default:
                break;
        }

        switch (noStones) {
            case 7:
                game.placeHandicapPosition(game.getSize() / 2, game.getSize() / 2, true); // I guess we could just run this anyway, at least if trying to re-occupy a field doesn't throw an exception, but skipping is faster.
                noStones--;
            case 6:
                game.placeHandicapPosition(game.getSize() - 4, game.getSize() / 2, true);
                game.placeHandicapPosition(3, game.getSize() / 2, true);
                noStones -= 2;
            default:
                break;
        }

        switch (noStones) {
            case 5:
                game.placeHandicapPosition(game.getSize() / 2, game.getSize() / 2, true);
            case 4:
                game.placeHandicapPosition(3, 3, true);
            case 3:
                game.placeHandicapPosition(game.getSize() / 2, game.getSize() / 2, true);
            case 2:
                game.placeHandicapPosition(game.getSize() - 4, 3, true);
                game.placeHandicapPosition(3, game.getSize() - 4, true);
            default:
                break;
        }
    }

}
