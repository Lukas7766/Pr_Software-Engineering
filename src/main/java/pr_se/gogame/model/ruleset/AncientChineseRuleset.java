package pr_se.gogame.model.ruleset;


import pr_se.gogame.model.Game;
import pr_se.gogame.model.StoneColor;
import pr_se.gogame.model.UndoableCommand;

import java.util.Arrays;

public class AncientChineseRuleset implements Ruleset {
    private int lastBoardHash;

    @Override
    public UndoableCommand isKo(Game game) {
        StoneColor[][] boardColor = new StoneColor[game.getSize()][game.getSize()];

        for(int i = 0; i < game.getSize(); i++) {
            for(int j = 0; j < game.getSize(); j++) {
                boardColor[i][j] = game.getColorAt(i, j);
            }
        }

        final int LAST_BOARD_HASH = lastBoardHash;
        final int NEW_BOARD_HASH = Arrays.deepHashCode(boardColor);

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

        return new GameResult(scoreBlack,scoreWhite,null,"");

    }

    @Override
    public void setHandicapStones(Game game, StoneColor beginner , int noStones) {
        if (game == null) {
            throw new IllegalArgumentException("board must not be null");
        }
        if (beginner == null) {
            throw new IllegalArgumentException("beginner must not be null");
        }
        if (noStones < 0 || noStones > 9){
            throw new IllegalArgumentException("noStones must be between 0 and 9");
        }

        final int SIZE = game.getSize();
        final int DIST_FROM_EDGE = 2 + SIZE / 10;

        game.setHandicapStoneCounter(noStones);

        game.placeHandicapPosition(SIZE / 2, SIZE / 2, noStones == 9);
        if(noStones == 9) noStones--;
        game.placeHandicapPosition(SIZE / 2, DIST_FROM_EDGE, noStones == 8);
        game.placeHandicapPosition(SIZE / 2, SIZE - 1 - DIST_FROM_EDGE, noStones == 8);
        if(noStones == 8) noStones -= 2;
        game.placeHandicapPosition(SIZE / 2, SIZE / 2, noStones == 7);
        if(noStones == 7) noStones--;
        game.placeHandicapPosition(SIZE - 1 - DIST_FROM_EDGE, SIZE / 2, noStones == 6);
        game.placeHandicapPosition(DIST_FROM_EDGE, SIZE / 2, noStones == 6);
        if(noStones == 6) noStones -= 2;
        game.placeHandicapPosition(SIZE / 2, SIZE / 2, noStones == 5);
        if(noStones == 5) noStones--;
        game.placeHandicapPosition(DIST_FROM_EDGE, DIST_FROM_EDGE, noStones == 4);
        if(noStones == 4) noStones--;
        game.placeHandicapPosition(SIZE / 2, SIZE / 2, noStones == 3);
        if(noStones == 3) noStones--;
        game.placeHandicapPosition(SIZE - 1 - DIST_FROM_EDGE, DIST_FROM_EDGE, noStones == 2);
        game.placeHandicapPosition(DIST_FROM_EDGE, SIZE - 1 - DIST_FROM_EDGE, noStones == 2);
    }

}
