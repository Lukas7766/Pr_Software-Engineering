package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.Game;
import pr_se.gogame.model.helper.Position;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;

import java.util.*;

public class JapaneseRuleset implements Ruleset {

    /**
     * This helps to determine if a position has already been visited by the flood fill algorithm.
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
            public void execute(final boolean saveEffects) {
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

        double komi = getKomi();
        double handicap = game.getHandicap();

        int capturedStonesBlack = game.getStonesCapturedBy(StoneColor.BLACK);
        int territoryScoreBlack = calculateTerritoryPoints(StoneColor.BLACK, game);

        int capturedStonesWhite = game.getStonesCapturedBy(StoneColor.WHITE);
        int territoryScoreWhite = calculateTerritoryPoints(StoneColor.WHITE, game);

        double scoreBlack = capturedStonesBlack + territoryScoreBlack + handicap;
        double scoreWhite = capturedStonesWhite + territoryScoreWhite + komi;

        GameResult ret = new GameResult();

        StoneColor winner = scoreBlack > scoreWhite ? StoneColor.BLACK : StoneColor.WHITE;

        ret.setWinner(winner);
        ret.setDescription(winner, winner + " won!");
        ret.setDescription(StoneColor.getOpposite(winner), StoneColor.getOpposite(winner) + " lost!");
        ret.addScoreComponent(StoneColor.BLACK, "Handicap", handicap);
        ret.addScoreComponent(StoneColor.WHITE, "Komi", komi);
        ret.addScoreComponent(StoneColor.BLACK, "Territory points", territoryScoreBlack);
        ret.addScoreComponent(StoneColor.WHITE, "Territory points", territoryScoreWhite);
        ret.addScoreComponent(StoneColor.BLACK, "Captured stones", capturedStonesBlack);
        ret.addScoreComponent(StoneColor.WHITE, "Captured stones", capturedStonesWhite);

        return ret;
    }

    // FloodFill Algorithm, source: ALGO assignment
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
        final int boardSize = game.getSize();
        visited = new boolean[boardSize][boardSize];

        int territoryPoints = 0;
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (game.getColorAt(x, y) == null && !visited[x][y]) {
                    territory = new LinkedList<>();
                    if(floodFill(game, color, x, y)) {
                        territoryPoints += territory.size();
                    }
                }
            }
        }

        return territoryPoints;
    }


    /**
     * Starts at the given location and recursively checks all neighboring positions.
     * @param game the Game to be evaluated
     * @param color the color whose territory score is to be calculated
     * @param x the x-coordinate of the search's origin, starting at the left
     * @param y the y-coordinate of the search's origin, starting at the top
     * @return true if an empty area without any bordering stones of the opposite of color was found, false if any stone
     *  of the opposite color was found next to an empty position
     */
    private boolean floodFill(Game game, StoneColor color, int x, int y) {
        if (x < 0 || y < 0 || x >= game.getSize() || y >= game.getSize()) {
            return true;
        }

        // Assertion: The coordinates are valid.

        if(game.getColorAt(x, y) == StoneColor.getOpposite(color)) {
            return false;
        }

        // Assertion: The board at this position is either empty or of the same color.

        if(visited[x][y]) {
            return true;
        }

        visited[x][y] = true;

        if(game.getColorAt(x, y) != null) {
            return true;
        }

        territory.add(new Position(x, y));
        boolean ret = floodFill(game, color, x, y + 1); // bottom

        ret &= floodFill(game, color, x + 1, y); // right           // The singular & is important, as we need to disable short-circuit evaluation.

        ret &= floodFill(game, color, x, y - 1); // top

        ret &= floodFill(game, color, x - 1, y); // left

        return ret; // SonarQube didn't like the singular & in the return value.
    }

    @Override
    public double getKomi() {
        return 6.5;
    }

}
