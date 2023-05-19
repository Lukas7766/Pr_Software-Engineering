package pr_se.gogame.model;

import pr_se.gogame.view_controller.DebugEvent;
import pr_se.gogame.view_controller.StoneRemovedEvent;
import pr_se.gogame.view_controller.StoneSetEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;


/**
 * Model
 * Go Board internal logic
 */
public class Board implements BoardInterface {
    /**
     * the Game that this Board belongs to
     */
    private final Game GAME;

    /**
     * the number of rows and columns of this board
     */
    private final int SIZE;

    /**
     * the actual board
     */
    private final StoneGroupPointer[][] board;

    private int lastDebugX = -1;
    private int lastDebugY = -1;

    /**
     * Creates a new Board belonging to the specified Game, containing handicap stones of the specified beginner color
     * (only if the Game has a handicap set)
     *
     * @param game     the Game that this Board belongs to
     * @param beginner which color player gets to start (handicap stones will be of this color)
     */
    public Board(Game game, StoneColor beginner) {
        this.GAME = game;
        this.SIZE = game.getSize();
        this.board = new StoneGroupPointer[SIZE][SIZE];

        int handicap = this.GAME.getHandicap(); // temporary variable; handicap will eventually need to be replaced with a simple number of handicap stones, as handicap has nothing to do with handicap stones.


        this.GAME.getRuleset().setHandicapStones(this, beginner, handicap);
    }

    @Override
    public boolean setStone(int x, int y, StoneColor color, boolean prepareMode) {
        // Are the coordinates invalid?
        if (areInvalidXYCoordinates(x, y)) {
            throw new IllegalArgumentException("Coordinates X=" + x + ", Y=" + y + "are out of bounds for board");
        }

        // is the StoneColor invalid?
        if(color == null) {
            throw new NullPointerException();
        }

        // Is the space already occupied?
        if (board[x][y] != null) {
            return false; // TODO: throw a custom exception? Illegalargument? Generic Exception? Or maybe not if we output it to the user?
        }

        //prevent KO
        if(GAME.getRuleset().getKoMove() != null) {
            if(GAME.getRuleset().predicateKoMove(x,y)) {
                System.out.println("KO move is not allowed");
                return false;
            } else {
                GAME.getRuleset().resetKoMove();
            }
        }

        // Get neighbors at these x and y coordinates
        Set<StoneGroup> surroundingSGs = getSurroundings(
                x,
                y,
                (sgp) -> sgp != null,
                (neighborX, neighborY) -> board[neighborX][neighborY].getStoneGroup()
        );

        // Get liberties at these x and y coordinates
        Set<Position> newStoneLiberties = getSurroundings(
                x,
                y,
                (sgp) -> sgp == null,
                (neighborX, neighborY) -> new Position(neighborX, neighborY)
        );
        StoneGroup newGroup = new StoneGroup(color, x, y, newStoneLiberties);

        Set<StoneGroup> removableSGs = new HashSet<>();

        StoneGroup firstSameColorGroup = null;

        /*
         * Merge groups of the same color as the new stone
         */
        for (StoneGroup sg : surroundingSGs) {
            sg.removeLiberty(new Position(x, y));
            if (sg.getStoneColor() == color) {
                if (firstSameColorGroup != null) {
                    firstSameColorGroup.mergeWithStoneGroup(sg);
                    removableSGs.add(sg);
                } else {
                    sg.mergeWithStoneGroup(newGroup);
                    removableSGs.add(newGroup);
                    firstSameColorGroup = sg;
                    System.out.println("Found group of same colour!");
                }
            }
        }

        surroundingSGs.removeAll(removableSGs);

        if (firstSameColorGroup == null) {
            firstSameColorGroup = newGroup;
            surroundingSGs.add(newGroup);
        }

        boolean permittedSuicide = false;
        boolean killAnother = false;

        Set<StoneGroup> otherColorGroups = surroundingSGs.stream().filter(sg -> sg.getStoneColor() != color).collect(Collectors.toSet());

        if (!prepareMode && firstSameColorGroup.getLiberties().size() == 0) {
            if (otherColorGroups.stream().noneMatch(sg -> sg.getLiberties().size() == 0)) { // if there are any groups of the opposite color with 0 liberties, the attacker wins and the existing group is removed instead.
                System.out.println("SUICIDE DETECTED!!!");
                if (!GAME.getRuleset().getSuicide(firstSameColorGroup)) {//SeWa, ToDo challenge idea of getSuicide
                    Position pos = new Position(x, y);
                    firstSameColorGroup.removeLocation(pos);
                    for (StoneGroup sg : surroundingSGs) {
                        sg.addLiberty(pos);
                    }
                    return false;
                }
                permittedSuicide = true;
            } else {
                if (!GAME.getRuleset().predicateKoMove(x,y)) {
                    killAnother = true;
                } else {
                    System.out.println("KO move is not allowed");
                    return false;
                }
            }
        }

        if (!permittedSuicide) {
            System.out.println("Placing stone down at " + x + ", " + y);
            board[x][y] =
                    firstSameColorGroup.getPointers().stream().findFirst().orElseGet(() -> new StoneGroupPointer(newGroup));
        }

        if (!prepareMode) {

            for (StoneGroup sg : surroundingSGs) {
                if ((sg.getStoneColor() != color || (sg == firstSameColorGroup && !killAnother)) && sg.getLiberties().size() == 0) {
                    int captured = 0;
                    for (Position p : sg.getLocations()) {
                        removeStone(p.X, p.Y);
                        captured++;
                        System.out.println("remove: " + p.X + " / " + p.Y);
                    }
                    GAME.addCapturedStones(color, captured);
                }
            }

            String saveCol = color == BLACK ? "B" : "W";
            GAME.getFileSaver().addStone(saveCol, x, y);
        }

        // Update UI if possible
        if (!permittedSuicide) {
            fireStoneSet(x, y, color, prepareMode);
        }

        System.out.println();

        return true;
    }

    @Override
    public void removeStone(int x, int y) {
        if(areInvalidXYCoordinates(x, y)) {
            throw new IllegalArgumentException("Coordinates X=" + x + ", Y=" + y + "are out of bounds for board");
        }

        board[x][y] = null;
        GAME.getFileSaver().removeStone(x, y);

        Set<StoneGroup> surroundingSGs = getSurroundings(
                x,
                y,
                (sgp) -> sgp != null,
                (neighborX, neighborY) -> board[neighborX][neighborY].getStoneGroup()
        );

        for (StoneGroup sg : surroundingSGs) {
            sg.addLiberty(new Position(x, y));
        }

        // Update UI
        fireStoneRemoved(x, y);
    }

    // Private methods

    /**
     * Notifies all listeners that a stone has been set.
     *
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @param c the StoneColor of the stone that has been set
     */
    private void fireStoneSet(int x, int y, StoneColor c, boolean prepareMode) {
        GameCommand gc = GameCommand.BLACK_PLAYS;

        if (prepareMode) {
            if (c == BLACK) {
                gc = GameCommand.BLACK_HANDICAP;
            } else {
                gc = GameCommand.WHITE_HANDICAP;
            }
        } else {
            if (c == WHITE) {
                gc = GameCommand.WHITE_PLAYS;
            }
        }
        System.out.println("cur move number: "+GAME.getCurMoveNumber());
        StoneSetEvent e = new StoneSetEvent(gc, x, y, GAME.getCurMoveNumber());
        GAME.fireGameEvent(e);
    }

    /**
     * Notifies all listeners that a stone has been removed.
     *
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     */
    private void fireStoneRemoved(int x, int y) {
        GameCommand gc = GameCommand.BLACK_HAS_CAPTURED;
        if (GAME.getCurColor() == WHITE) {
            gc = GameCommand.WHITE_HAS_CAPTURED;
        }
        StoneRemovedEvent e = new StoneRemovedEvent(gc, x, y);

        GAME.fireGameEvent(e);
    }

    public Set<StoneGroup> getNeighbors(int x, int y) {
        return getSurroundings(
                x,
                y,
                (sgp) -> sgp != null,
                (neighborX, neighborY) -> board[neighborX][neighborY].getStoneGroup()
        );
    }

    /**
     * Checks the space above, below, to the right and left of the one marked by x and y for StoneGroupPointers
     * fulfilling the predicate check, returning a Set of at most four elements that have been converted by conversion.
     *
     * @param x          Horizontal coordinate from 0 to size-1, starting on the left
     * @param y          Vertical coordinate from 0 to size-1, starting on the top
     * @param check      the condition that a surrounding tile has to fulfill to be added ot the returned Set
     * @param conversion a BiFunction taking an x and y coordinate from this method and returning something caller-defined based on those coordinates
     * @return a Set of at most four unique elements converted by conversion that are above, below, to the left and right of the provided x and y coordinate and fulfill check
     */
    private Set getSurroundings(int x, int y, Predicate<StoneGroupPointer> check, BiFunction<Integer, Integer, ?> conversion) {
        if (areInvalidXYCoordinates(x, y)) {
            throw new IllegalArgumentException("Coordinates X=" + x + ", Y=" + y + "are out of bounds for board");
        }

        Set surroundings = new HashSet<>();

        if (y > 0 && check.test(board[x][y - 1])) {
            surroundings.add(conversion.apply(x, y - 1));
        }
        if (y < SIZE - 1 && check.test(board[x][y + 1])) {
            surroundings.add(conversion.apply(x, y + 1));
        }
        if (x > 0 && check.test(board[x - 1][y])) {
            surroundings.add(conversion.apply(x - 1, y));
        }
        if (x < SIZE - 1 && check.test(board[x + 1][y])) {
            surroundings.add(conversion.apply(x + 1, y));
        }

        return surroundings;
    }

    // Getters and Setters
    public int getSize() {
        return SIZE;
    }

    public StoneColor getColorAt(int x, int y) {
        if(areInvalidXYCoordinates(x, y)) {
            throw new IllegalArgumentException("Coordinates X=" + x + ", Y=" + y + "are out of bounds for board");
        }

        if (board[x][y] != null) {
            return board[x][y].getStoneGroup().getStoneColor();
        } else {
            return null;
        }
    }

    public Game getGAME() {
        return GAME;
    }

    /*public StoneGroupPointer[][] getBoard() {
        return board;
    }*/

    // TODO: Remove these debug methods
    public void printDebugInfo(int x, int y) {
        if(areInvalidXYCoordinates(x, y)) {
            throw new IllegalArgumentException("Coordinates X=" + x + ", Y=" + y + "are out of bounds for board");
        }

        if (board[x][y] != null && !(x == lastDebugX && y == lastDebugY)) {
            System.out.println("Group at " + x + ", " + y + ":");
            System.out.println("Liberties: " + board[x][y].getStoneGroup().getLiberties().size());
        }

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != null) {
                    DebugEvent e = new DebugEvent(GameCommand.DEBUG_INFO, i, j, board[i][j].serialNo, board[i][j].getStoneGroup().serialNo);
                    GAME.fireGameEvent(e);
                }
            }
        }

        lastDebugX = x;
        lastDebugY = y;
    }

    /**
     * Tests whether these x and y coordinates are within the bounds of the playing field
     * @param x x coordinate starting at the left
     * @param y y coordinate starting at the top
     * @return whether these x and y coordinates are outside the playing field.
     */
    private boolean areInvalidXYCoordinates(int x, int y) {
        return x < 0 || y < 0 || x >= SIZE || y >= SIZE;
    }
}