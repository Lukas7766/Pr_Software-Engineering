package pr_se.gogame.model;

import java.util.ArrayList;
import java.util.List;

public class JapaneseRuleset implements Ruleset {

    private int currentKOCnt = 0;
    private Position koMove;

    private boolean[][] visited;
    private List<Position> territory;

    @Override
    public int getKoAmount() {
        return 1;
    }

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

    @Override
    public Position getKoMove() {
        return koMove;
    }

    @Override
    public void resetKoMove() {
        this.koMove = null;
        this.currentKOCnt = 0;
    }

    @Override
    public int[] scoreGame(Board board) {

        if (board == null) throw new IllegalArgumentException();

        int scoreBlack = 0;
        int scoreWhite = 0;

        for (int i = 0; i < board.getBoard().length; i++) {
            for (int j = 0; j < board.getBoard()[i].length; j++) {
                StoneGroupPointer p = board.getBoard()[i][j];
                if (p == null) continue;
                StoneGroup stoneGroup = p.getStoneGroup();
                if (stoneGroup == null) continue;
                if (board.getBoard()[i][j].getStoneGroup().getStoneColor() == StoneColor.BLACK) {
                } else if (board.getBoard()[i][j].getStoneGroup().getStoneColor() == StoneColor.WHITE) {
                }

            }
        }


        for (StoneColor color : StoneColor.values()) {
            if (color == StoneColor.BLACK) {
                scoreBlack += calculateTerritoryPoints(color, board);
            } else if (color == StoneColor.WHITE) {
                scoreWhite += calculateTerritoryPoints(color, board);
            }
        }

        System.out.println("Score Black: " + scoreBlack);
        System.out.println("Score White: " + scoreWhite);

        return new int[]{scoreBlack, scoreWhite};

    }

    //FloodFill Algorithm, source ALGO assignment
    public int calculateTerritoryPoints(StoneColor color, Board board) {
        int boardSize = board.getSize();
        visited = new boolean[boardSize][boardSize];

        int territoryPoints = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {


                if (board.getBoard()[i][j] == null && !visited[i][j]) {

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
        return territoryPoints;
    }

    public void floodFill(Board board, int x, int y) {
        if (x < 0) return;
        if (x >= board.getSize()) return;
        if (y < 0) return;
        if (y >= board.getSize()) return;
        if (visited[x][y]) return;

        visited[x][y] = true;

        if (board.getBoard()[x][y] != null) return;

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
