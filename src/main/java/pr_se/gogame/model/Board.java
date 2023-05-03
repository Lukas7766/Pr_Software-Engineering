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

    private int lastDebugX = 0;
    private int lastDebugY = 0;

    /**
     * Creates a new Board belonging to the specified Game, containing handicap stones of the specified beginner color
     * (only if the Game has a handicap set)
     * @param game the Game that this Board belongs to
     * @param beginner which color player gets to start (handicap stones will be of this color)
     */
    public Board(Game game, StoneColor beginner) {
        this.GAME = game;
        this.SIZE = game.getSize();
        this.board = new StoneGroupPointer[SIZE][SIZE];

        int komi = this.GAME.getHandicap(); // temporary variable; komi will eventually need to be replaced with a simple number of handicap stones, as komi has nothing to do with handicap stones.

        if(this.GAME.getRuleset().hasDefaultHandicapPlacement()) {
            /*
             * This is a default implementation, the ancient Chinese ruleset has a different placement for 3, and the
             *  New-Zealand-Ruleset, among others, permits free placement of handicap stones. That is why a ruleset
             *  may override this.
             */
            switch (komi) {
                case 9:
                    setStone(SIZE / 2, SIZE / 2, beginner, true);
                    komi--;                                                     // set remaining no. to 8
                case 8:
                    setStone(SIZE / 2, 3, beginner, true);
                    setStone(SIZE / 2, SIZE - 4, beginner, true);
                    komi -= 2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
                default:
                    break;
            }

            switch (komi) {
                case 7:
                    setStone(SIZE / 2, SIZE / 2, beginner, true); // I guess we could just run this anyway, at least if trying to re-occupy a field doesn't throw an exception, but skipping is faster.
                    komi--;
                case 6:
                    setStone(SIZE - 4, SIZE / 2, beginner, true);
                    setStone(3, SIZE / 2, beginner, true);
                    komi -= 2;
                default:
                    break;
            }

            switch (komi) {
                case 5:
                    setStone(SIZE / 2, SIZE / 2, beginner, true);
                case 4:
                    setStone(3, 3, beginner, true);
                case 3:
                    setStone(SIZE - 4, SIZE - 4, beginner, true);
                case 2:
                    setStone(SIZE - 4, 3, beginner, true);
                    setStone(3, SIZE - 4, beginner, true);
                default:
                    break;
            }
        } else {
            /*
             * Lets the Ruleset place its own handicap stones.
             */
            this.GAME.getRuleset().setHandicapStones(this, komi);
        }


    }

    @Override
    public boolean setStone(int x, int y, StoneColor color, boolean prepareMode) { // TODO: Maybe return boolean for move successful/unsuccessful?
        // Are the coordinates invalid?
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        // Is the space already occupied?
        if (board[x][y] != null) {
            return false; // TODO: throw a custom exception?
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
            if(otherColorGroups.stream().noneMatch(sg -> sg.getLiberties().size() == 0)) { // if there are any groups of the opposite color with 0 liberties, the attacker wins and the existing group is removed instead.
                System.out.println("SUICIDE DETECTED!!!");
                if (!GAME.getRuleset().getSuicide(firstSameColorGroup)) {
                    Position pos = new Position(x, y);
                    firstSameColorGroup.removeLocation(pos);
                    for (StoneGroup sg : surroundingSGs) {
                        sg.addLiberty(pos);
                    }
                    return false;
                }
                permittedSuicide = true;
            } else {
                killAnother = true; // TODO: This sort of thing is exactly what ko is about, so this might be a good place to check for ko.
            }
        }

        if(!permittedSuicide) {
            System.out.println("Placing stone down at " + x + ", " + y);
            board[x][y] =
                    firstSameColorGroup.getPointers().stream().findFirst().orElseGet(() -> new StoneGroupPointer(newGroup));
        }

        if(!prepareMode) {
            for (StoneGroup sg : surroundingSGs) {
                if ((sg.getStoneColor() != color || (sg == firstSameColorGroup && !killAnother)) && sg.getLiberties().size() == 0) {
                    for (Position p : sg.getLocations()) {
                        removeStone(p.X, p.Y);
                    }
                }
            }

            String saveCol = color == BLACK ? "B" : "W";
            GAME.getFileSaver().addStone(saveCol, x, y);
        }

        if(board[x][y] == null) {
            System.out.println("Stone has gone!");
        }

        // Update UI if possible
        if(!permittedSuicide) {
            fireStoneSet(x, y, color, prepareMode);
        }

        System.out.println();

        return true;
    }

    @Override
    public void removeStone(int x, int y) {
        board[x][y] = null;
        GAME.getFileSaver().removeStone(x,y);

        Set<StoneGroup> surroundingSGs = getSurroundings(
                x,
                y,
                (sgp) -> sgp != null,
                (neighborX, neighborY) -> board[neighborX][neighborY].getStoneGroup()
        );

        for(StoneGroup sg : surroundingSGs) {
            sg.addLiberty(new Position(x, y));
        }

        // Update UI
        fireStoneRemoved(x, y);
    }

    // Private methods

    /**
     * Notifies all listeners that a stone has been set.
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @param c the StoneColor of the stone that has been set
     */
    private void fireStoneSet(int x, int y, StoneColor c, boolean prepareMode) {
        GameCommand gc = GameCommand.BLACKPLAYS;

        if(prepareMode) {
            if(c == BLACK) {
                gc = GameCommand.BLACKHANDICAP;
            } else {
                gc = GameCommand.WHITEHANDICAP;
            }
        } else {
            if (c == WHITE) {
                gc = GameCommand.WHITEPLAYS;
            }
        }
        StoneSetEvent e = new StoneSetEvent(gc, x, y, GAME.getCurMoveNumber());

        GAME.fireGameEvent(e);
    }

    /**
     * Notifies all listeners that a stone has been removed.
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     */
    private void fireStoneRemoved(int x, int y) {
        GameCommand gc = GameCommand.BLACKREMOVED;
        if(GAME.getCurColor() == WHITE) {
            gc = GameCommand.WHITEREMOVED;
        }
        StoneRemovedEvent e = new StoneRemovedEvent(gc, x, y);

        GAME.fireGameEvent(e);
    }

    /**
     * Checks the space above, below, to the right and left of the one marked by x and y for StoneGroupPointers
     * fulfilling the predicate check, returning a Set of at most four elements that have been converted by conversion.
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @param check the condition that a surrounding tile has to fulfill to be added ot the returned Set
     * @param conversion a BiFunction taking an x and y coordinate from this method and returning something caller-defined based on those coordinates
     * @return a Set of at most four unique elements converted by conversion that are above, below, to the left and right of the provided x and y coordinate and fulfill check
     */
    private Set getSurroundings(int x, int y, Predicate<StoneGroupPointer> check, BiFunction<Integer, Integer, ?> conversion) {
        if(x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        Set surroundings = new HashSet<>();

        if(y > 0 && check.test(board[x][y - 1])) {
            surroundings.add(conversion.apply(x, y - 1));
        }
        if(y < SIZE - 1 && check.test(board[x][y + 1])) {
            surroundings.add(conversion.apply(x, y + 1));
        }
        if(x > 0 && check.test(board[x - 1][y])) {
            surroundings.add(conversion.apply(x - 1, y));
        }
        if(x < SIZE - 1 && check.test(board[x + 1][y])) {
            surroundings.add(conversion.apply(x + 1, y));
        }

        return surroundings;
    }

    // Getters and Setters
    public int getSize() {
        return SIZE;
    }

    public StoneColor getColorAt(int x, int y) {
        if(board[x][y] != null) {
            System.out.println(board[x][y]);
            return board[x][y].getStoneGroup().getStoneColor();
        } else {
            return null;
        }
    }

    public Game getGAME() {
        return GAME;
    }

    public StoneGroupPointer[][] getBoard() {
        return board;
    }

    // TODO: Remove these debug methods

    public int getLastDebugX() {
        return lastDebugX;
    }

    public int getLastDebugY() {
        return lastDebugY;
    }

    public void printDebugInfo(int x, int y) {
        if(board[x][y] != null && !(x == lastDebugX && y == lastDebugY)) {
            System.out.println("Group at " + x + ", " + y + ":");
            System.out.println("Liberties: " + board[x][y].getStoneGroup().getLiberties().size());
        }

        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                if(board[i][j] != null) {
                    DebugEvent e = new DebugEvent(GameCommand.DEBUGINFO, i, j, board[i][j].serialNo, board[i][j].getStoneGroup().serialNo);
                    GAME.fireGameEvent(e);
                }
            }
        }

        lastDebugX = x;
        lastDebugY = y;
    }
}