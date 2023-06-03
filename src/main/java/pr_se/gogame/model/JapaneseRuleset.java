package pr_se.gogame.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JapaneseRuleset implements Ruleset {

    /**
     * The amount of ko moves that are done in a game. This will be reset after the KO move is aborted.
     */
    private int currentKOCnt = 0;

    /**
     * The position of the KO move. This will be reset after the KO move is aborted.
     */
    private Position koMove;

    /**
     * This helps to determine if a position is already visited by the floodfill algorithm.
     */
    private boolean[][] visited;

    /**
     * This is a temporary list of positions of a territory. The number of positions in this list will be added to
     * the score of the player if the territory is not surrounded by a different color.
     */
    private List<Position> territory;

    private final int [] boardHashes = new int [2];

    /**
     * The JP ruleset allows only one KO move repetition.
     *
     * @return 1 as only one KO move is allowed.
     */
    @Override
    public int getKoAmount() {
        return 1;
    }

    /**
     * This method predicates if a KO move is done.
     *
     * @param x pass the X-axis of the verifiable move
     * @param y pass the y-axis of the verifiable move
     * @return If the KO move is done the method returns true. If the KO move is not done the method returns false.
     */
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
        System.out.println("checkKoMove X: " + x + ", Y: " + y);
        if(koMove != null) {
            System.out.println("koMove != null");
            if(koMove.X != x || koMove.Y != y) {
                System.out.println("resetKoMove()");
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
        System.out.println("isKo()");

        StoneColor [][] boardColor = new StoneColor[game.getSize()][game.getSize()];

        for(int i = 0; i < game.getSize(); i++) {
            for(int j = 0; j < game.getSize(); j++) {
                boardColor[i][j] = game.getColorAt(i, j);
            }
        }

        final int LAST_BOARD_HASH = boardHashes[0];
        final int NEW_BOARD_HASH = Arrays.deepHashCode(boardColor);

        System.out.print("boardHashes: ");
        for(int i : boardHashes) {
            System.out.print(i + " ");
        }
        System.out.println();
        System.out.println("NEW_BOARD_HASH: " + NEW_BOARD_HASH);

        if(NEW_BOARD_HASH == LAST_BOARD_HASH) {
            return null;
        }

        System.out.println("Not ko");

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                for(int i = 0; i < boardHashes.length - 1; i++) {
                    boardHashes[i] = boardHashes[i + 1];
                }
                boardHashes[boardHashes.length - 1] = NEW_BOARD_HASH;
            }

            @Override
            public void undo() {
                for(int i = boardHashes.length; i > 0; i--) {
                    boardHashes[i] = boardHashes[i - 1];
                }
                boardHashes[0] = LAST_BOARD_HASH;
            }
        };
        ret.execute();

        return ret;
    }

    /**
     * Calculates the score of the game based on the Japanese ruleset. This is done by calculating the territory of each player.
     * The player with the most territory wins. Territory is calculated by using the floodfill algorithm.
     * The algorithm starts at the border of the board. In the first step all empty positions will be located.
     * In the next and final step for every empty position all neighbouring positions will be checked if a different color than the passed is present.
     * If this is the case the territory will be added to the score of the player. If the territory is surrounded by at least one different color it will be ignored.
     * If on the board only one color is present, this player will get all points. If the board is empty, both players will get all points.
     * ++komi ++handicap//ToDo adapt description
     *
     * @param game to calculate the score and define the winner
     * @return an array of size 2, containing the score of black and white
     */
    @Override
    public GameResult scoreGame(Game game) {

        if (game == null) throw new IllegalArgumentException();

        double komi = game.getKomi();
        int handicap = game.getHandicap();

        int capturedStonesBlack = game.getStonesCapturedBy(StoneColor.BLACK);
        int territoryScoreBlack = calculateTerritoryPoints(StoneColor.BLACK, game.getBoard());

        int capturedStonesWhite = game.getStonesCapturedBy(StoneColor.WHITE);
        int territoryScoreWhite = calculateTerritoryPoints(StoneColor.WHITE, game.getBoard());

        double scoreBlack = capturedStonesBlack + territoryScoreBlack + handicap;
        double scoreWhite = capturedStonesWhite + territoryScoreWhite + komi;

        StringBuilder sb = new StringBuilder();
        StoneColor winner = null;

        if (scoreBlack > scoreWhite) {
            winner = StoneColor.BLACK;
            sb.append("Black won!").append("\n\n");
        } else{
            winner = StoneColor.WHITE;
            sb.append("White won!").append("\n\n");
        }

        int captStone = 0;
        int trScore = 0;
        double sc = 0;
        if (winner == StoneColor.WHITE) {
            sb.append("Komi:").append(" ").append(komi).append("\n");
            captStone = capturedStonesWhite;
            trScore = territoryScoreWhite;
            sc = scoreWhite;
        }
        else {
            sb.append("Handicap:").append(" ").append(handicap).append("\n");
            captStone = capturedStonesBlack;
            trScore = territoryScoreBlack;
            sc = scoreBlack;
        }
        sb.append("+ Territory points:").append(" ").append(trScore).append("\n");
        sb.append("+ Captured stones:").append(" ").append(captStone).append("\n\n");
        sb.append("= ").append(sc).append(" points");


        return new GameResult(scoreBlack, scoreWhite, winner, sb.toString());
    }

    //FloodFill Algorithm, source ALGO assignment
    public int calculateTerritoryPoints(StoneColor color, Board board) {
        int boardSize = board.getSize();
        visited = new boolean[boardSize][boardSize];

        int territoryPoints = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {

                if (board.getColorAt(i, j) == null && !visited[i][j]) {

                    boolean occupiedTerritory = true;

                    territory = new ArrayList<>();
                    floodFill(board, i, j);

                    for (Position p : territory) {
                        for (StoneGroup n : board.getNeighbors(p.X, p.Y).stream().toList()) {
                            if (n.getStoneColor() != color) {
                                occupiedTerritory = false;
                                break;
                            }
                        }
                        if (!occupiedTerritory) {
                            break;
                        }
                    }

                    if (occupiedTerritory) {
                        territoryPoints += territory.size();
                    }
                }
            }
        }
        System.out.println("Territory Score " + color + " " + territoryPoints);
        return territoryPoints;
    }


    public void floodFill(Board board, int x, int y) {
        if (x < 0) return;
        if (x >= board.getSize()) return;
        if (y < 0) return;
        if (y >= board.getSize()) return;
        if (visited[x][y]) return;

        visited[x][y] = true;

        if (board.getColorAt(x, y) != null) return;

        territory.add(new Position(x, y));
        floodFill(board, x, y + 1); //top
        floodFill(board, x + 1, y); //right
        floodFill(board, x, y - 1); //bottom
        floodFill(board, x - 1, y); //left
    }

    @Override
    public double getKomi() {
        return 6.5;
    }

}
