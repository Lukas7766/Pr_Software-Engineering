package pr_se.gogame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AncientChineseRuleset implements Ruleset {

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

    public Position getKoMove() {
        return koMove;
    }

    @Override
    public void resetKoMove() {
        this.koMove = null;
        this.currentKOCnt = 0;
    }

    @Override
    public void scoreGame(Board board) {

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
                    scoreBlack++;
                } else if (board.getBoard()[i][j].getStoneGroup().getStoneColor() == StoneColor.WHITE) {
                    scoreWhite++;
                }

            }
        }


        System.out.println(calculateTerritory(StoneColor.BLACK, board));
        System.out.println("Score Board:");

        System.out.println("Score Black: " + scoreBlack);
        System.out.println("Score White: " + scoreWhite);

    }

    //FloodFill Algorithm
    public int calculateTerritory(StoneColor color, Board board) {
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
                        for (StoneGroup n : (Set<StoneGroup>) board.getNeighbors(p.X, p.Y).stream().collect(Collectors.toList())) {
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
    public boolean hasDefaultHandicapPlacement() {
        return false;
    }

    @Override
    public void setHandicapStones(Board board, int noStones) {
        switch (noStones) {
            case 9:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true);
                noStones--;                                                     // set remaining no. to 8
            case 8:
                board.setStone(board.getSize() / 2, 3, board.getGAME().getCurColor(), true);
                board.setStone(board.getSize() / 2, board.getSize() - 4, board.getGAME().getCurColor(), true);
                noStones -= 2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
            default:
                break;
        }

        switch (noStones) {
            case 7:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true); // I guess we could just run this anyway, at least if trying to re-occupy a field doesn't throw an exception, but skipping is faster.
                noStones--;
            case 6:
                board.setStone(board.getSize() - 4, board.getSize() / 2, board.getGAME().getCurColor(), true);
                board.setStone(3, board.getSize() / 2, board.getGAME().getCurColor(), true);
                noStones -= 2;
            default:
                break;
        }

        switch (noStones) {
            case 5:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true);
            case 4:
                board.setStone(3, 3, board.getGAME().getCurColor(), true);
            case 3:
                board.setStone(board.getSize() / 2, board.getSize() / 2, board.getGAME().getCurColor(), true);
            case 2:
                board.setStone(board.getSize() - 4, 3, board.getGAME().getCurColor(), true);
                board.setStone(3, board.getSize() - 4, board.getGAME().getCurColor(), true);
            default:
                break;
        }
    }
}
