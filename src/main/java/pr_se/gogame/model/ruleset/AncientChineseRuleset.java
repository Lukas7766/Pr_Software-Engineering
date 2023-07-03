package pr_se.gogame.model.ruleset;


import pr_se.gogame.model.Game;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
            public void execute(final boolean saveEffects) {
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
    public UndoableCommand scoreGame(Game game) {

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

        StoneColor winner = scoreBlack > scoreWhite ? StoneColor.BLACK : StoneColor.WHITE;

        List<UndoableCommand> subcommands = new LinkedList<>();

        GameResult ret = game.getGameResult();

        subcommands.add(ret.setWinner(winner));
        subcommands.add(ret.setDescription(winner, winner + " won!"));
        subcommands.add(ret.setDescription(StoneColor.getOpposite(winner), StoneColor.getOpposite(winner) + " lost!"));
        subcommands.add(ret.addScoreComponent(StoneColor.BLACK, GameResult.PointType.STONES_ON_BOARD, scoreBlack));
        subcommands.add(ret.addScoreComponent(StoneColor.WHITE, GameResult.PointType.STONES_ON_BOARD, scoreWhite));

        return UndoableCommand.of(subcommands);
    }

    @Override
    public boolean setHandicapStones(Game game, StoneColor beginner , int noStones) {
        if (game == null) {
            throw new NullPointerException("game must not be null");
        }
        if (beginner == null) {
            throw new NullPointerException("beginner must not be null");
        }
        if (noStones < Game.MIN_HANDICAP_AMOUNT || noStones > Game.MAX_HANDICAP_AMOUNT){
            throw new IllegalArgumentException("noStones must be between " + Game.MIN_HANDICAP_AMOUNT + " and " + Game.MAX_HANDICAP_AMOUNT);
        }

        final int size = game.getSize();
        final int distFromEdge = 2 + size / 10;

        boolean centerSet = false;

        if(noStones == 9) {
            game.placeHandicapPosition(size / 2, size / 2, true);
            centerSet = true;
            noStones--;
        }
        game.placeHandicapPosition(size / 2, distFromEdge, noStones == 8);
        game.placeHandicapPosition(size / 2, size - 1 - distFromEdge, noStones == 8);
        if(noStones == 8) noStones -= 2;
        if(noStones == 7) {
            game.placeHandicapPosition(size / 2, size / 2, true);
            centerSet = true;
            noStones--;
        }
        game.placeHandicapPosition(size - 1 - distFromEdge, size / 2, noStones == 6);
        game.placeHandicapPosition(distFromEdge, size / 2, noStones == 6);
        if(noStones == 6) noStones -= 2;
        if(!centerSet) {
            game.placeHandicapPosition(size / 2, size / 2, noStones == 5);
        }
        if (noStones == 5) noStones--;
        game.placeHandicapPosition(distFromEdge, distFromEdge, noStones == 4);
        if(noStones == 4) noStones--;
        game.placeHandicapPosition(size / 2, size / 2, noStones == 3);
        if(noStones == 3) noStones--;
        game.placeHandicapPosition(size - 1 - distFromEdge, distFromEdge, noStones == 2);
        game.placeHandicapPosition(distFromEdge, size - 1 - distFromEdge, noStones == 2);

        return true;
    }

}
