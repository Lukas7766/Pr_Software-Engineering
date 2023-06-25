package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JapaneseRuleset implements Ruleset {

    /**
     * This helps to determine if a position is already visited by the flood fill algorithm.
     */
    private boolean[][] visited;

    /**
     * This is a temporary list of positions of a territory. The number of positions in this list will be added to
     * the score of the player if the territory is not surrounded by a different color.
     */
    private List<Position> territory;

    private final int [] boardHashes = new int [getKoAmount()];

    @Override
    public void reset() {
        Arrays.fill(boardHashes, 0);
    }

    @Override
    public UndoableCommand isKo(Game game) {
        StoneColor[][] boardColor = new StoneColor[game.getSize()][game.getSize()];

        for(int i = 0; i < game.getSize(); i++) {
            for(int j = 0; j < game.getSize(); j++) {
                boardColor[i][j] = game.getColorAt(i, j);
            }
        }

        final int LAST_BOARD_HASH = boardHashes[0];
        final int NEW_BOARD_HASH = Arrays.deepHashCode(boardColor);

        if(NEW_BOARD_HASH == LAST_BOARD_HASH) {
            return null;
        }

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                for(int i = 0; i < boardHashes.length - 1; i++) {
                    boardHashes[i] = boardHashes[i + 1];
                }
                boardHashes[boardHashes.length - 1] = NEW_BOARD_HASH;
            }

            @Override
            public void undo() {
                for(int i = boardHashes.length - 1; i > 0; i--) {
                    boardHashes[i] = boardHashes[i - 1];
                }
                boardHashes[0] = LAST_BOARD_HASH;
            }
        };
        ret.execute(true);

        return ret;
    }

    /**
     * Calculates the score of the game based on the Japanese ruleset. This is done by calculating the territory of each player.
     * The player with the most territory wins. Territory is calculated by using the flood fill algorithm.
     * Handicap is added to Black's score count.
     * Komi is added to White's score count.
     *
     * @param game to calculate the score and define the winner
     * @return an array of size 2, containing the score of black and white
     */
    @Override
    public GameResult scoreGame(Game game) {
        if (game == null) {
            throw new NullPointerException();
        }

        double komi = game.getKomi();
        double handicap = game.getHandicap();

        int capturedStonesBlack = game.getStonesCapturedBy(StoneColor.BLACK);
        int territoryScoreBlack = calculateTerritoryPoints(StoneColor.BLACK, game);

        int capturedStonesWhite = game.getStonesCapturedBy(StoneColor.WHITE);
        int territoryScoreWhite = calculateTerritoryPoints(StoneColor.WHITE, game);

        double scoreBlack = capturedStonesBlack + territoryScoreBlack + handicap;
        double scoreWhite = capturedStonesWhite + territoryScoreWhite + komi;

        StringBuilder sb = new StringBuilder();
        StoneColor winner;
        int captStone;
        int trScore;
        double sc;

        if (scoreBlack > scoreWhite) {
            winner = StoneColor.BLACK;
            sb.append(StoneColor.BLACK).append(" won!\n\n");
            sb.append("Handicap: ").append(handicap).append("\n");
            captStone = capturedStonesBlack;
            trScore = territoryScoreBlack;
            sc = scoreBlack;
        } else {
            winner = StoneColor.WHITE;
            sb.append(StoneColor.WHITE).append(" won!\n\n");
            sb.append("Komi: ").append(komi).append("\n");
            captStone = capturedStonesWhite;
            trScore = territoryScoreWhite;
            sc = scoreWhite;
        }

        sb.append("+ Territory points:").append(" ").append(trScore).append("\n");
        sb.append("+ Captured stones:").append(" ").append(captStone).append("\n\n");
        sb.append("= ").append(sc).append(" points");

        return new GameResult(scoreBlack, scoreWhite, winner, sb.toString());
    }

    //FloodFill Algorithm, source: ALGO assignment
    /**
     * The algorithm starts at the border of the board. In the first step, all empty positions will be located.
     * In the next and final step for every empty position all neighbouring positions will be checked if a different
     *  color than the passed one is present.
     * If this is the case the territory will be added to the score of the player. If the territory is surrounded by at
     *  least one different color it will be ignored.
     * If on the board only one color is present, this player will get all points. If the board is empty, both players
     *  will get all points.
     * @param color which color's territory points are to be calculated
     * @param game the Game to be used for calculating the territory points
     * @return how many territory points the given player has on the given board.
     */
    private int calculateTerritoryPoints(StoneColor color, Game game) {
        int boardSize = game.getSize();
        visited = new boolean[boardSize][boardSize];

        int territoryPoints = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {

                if (game.getColorAt(i, j) == null && !visited[i][j]) {

                    boolean occupiedTerritory = true;

                    territory = new ArrayList<>();
                    floodFill(game, i, j);

                    for (Position p : territory) {
                        if(     (p.getX() > 0 && game.getColorAt(p.getX() - 1, p.getY()) == StoneColor.getOpposite(color)) ||
                                (p.getX() < game.getSize() - 1 && game.getColorAt(p.getX() + 1, p.getY()) == StoneColor.getOpposite(color)) ||
                                (p.getY() > 0 && game.getColorAt(p.getX(), p.getY() - 1) == StoneColor.getOpposite(color)) ||
                                (p.getY() < game.getSize() - 1 && game.getColorAt(p.getX(), p.getY() + 1) == StoneColor.getOpposite(color))) {
                            occupiedTerritory = false;
                            break;
                        }
                    }

                    if (occupiedTerritory) {
                        territoryPoints += territory.size();
                    }
                }
            }
        }
        return territoryPoints;
    }


    private void floodFill(Game game, int x, int y) {
        if (x < 0 || y < 0 || x >= game.getSize() || y >= game.getSize() || visited[x][y]) {
            return;
        }

        visited[x][y] = true;

        if (game.getColorAt(x, y) != null) {
            return;
        }

        territory.add(new Position(x, y));
        floodFill(game, x, y + 1); //top
        floodFill(game, x + 1, y); //right
        floodFill(game, x, y - 1); //bottom
        floodFill(game, x - 1, y); //left
    }

    @Override
    public double getKomi() {
        return 6.5;
    }

}
