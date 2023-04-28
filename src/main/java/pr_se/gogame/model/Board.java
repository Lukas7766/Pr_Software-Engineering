package pr_se.gogame.model;

import pr_se.gogame.view_controller.DebugEvent;
import pr_se.gogame.view_controller.StoneRemovedEvent;
import pr_se.gogame.view_controller.StoneSetEvent;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

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

    //TODO: Move this elsewere ?
    private FileSaver fileSaver;

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
        this.fileSaver = new FileSaver("Black","White",String.valueOf(SIZE));

        int komi = game.getKomi(); // temporary variable; komi is only needed by the board here (if at all - see next comment)

        /*
        * Handle handicap stones (After research, I don't think that komi actually means "number of handicap stones").
        *
        * TODO: This is a default implementation, the ancient Chinese ruleset has a different placement for 3, and the
        *  New-Zealand-Ruleset, among others, permits free placement of handicap stones. Thus, it should be possible
        *  for a ruleset to override this.
        */
        switch (komi) {
            case 9:
                setStone(SIZE/2, SIZE/2, beginner, true);
                komi--;                                                     // set remaining no. to 8
            case 8:
                setStone(SIZE / 2, 3, beginner, true);
                setStone(SIZE / 2, SIZE - 4, beginner, true);
                komi-=2;                                                    // skip the central placement of handicap stone 7 by setting remaining no. to 6
            default: break;
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
            default: break;
        }


    }

    @Override
    public void setStone(int x, int y, StoneColor color, boolean prepareMode) {
        // Are the coordinates invalid?
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        // Is the space already occupied?
        if (board[x][y] != null) {
            return; // TODO: throw a custom exception?
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

        StoneGroup firstSameColorGroup = null;

        for (StoneGroup sg : surroundingSGs) {
            if (sg != null) {
                sg.removeLiberty(new Position(x, y));
                if (sg.getStoneColor() == color) {
                    if (firstSameColorGroup != null) {
                        firstSameColorGroup.mergeWithStoneGroup(sg);
                    } else {
                        sg.mergeWithStoneGroup(newGroup);
                        firstSameColorGroup = sg;
                    }
                }
            }
        }

        if (firstSameColorGroup == null) {
            firstSameColorGroup = newGroup;
            surroundingSGs.add(newGroup);
        }
        board[x][y] =
                firstSameColorGroup.getPointers().stream().findFirst().orElseGet(() -> new StoneGroupPointer(newGroup));

        if (!prepareMode) {
            for (StoneGroup sg : surroundingSGs) {
                if ((sg.getStoneColor() != color || sg == firstSameColorGroup) && sg.getLiberties().size() == 0) {
                    for (Position p : sg.getLocations()) {
                        removeStone(p.X, p.Y);
                    }
                }
            }

            // TODO: Call ruleset method instead, because some rulesets (e.g., New Zealand) permit suicide.
            if (board[x][y] == null) {
                System.out.println("SUICIDE DETECTED!!!");
                if(!GAME.getRuleset().getSuicide()) {
                    return;
                }
            }

            String saveCol = color == BLACK ? "B" : "W";
            fileSaver.addStone(saveCol, x, y);
            // Update UI
            fireStoneSet(x, y, color);

            GAME.setCurMoveNumber(GAME.getCurMoveNumber() + 1);

            // Update current player color
            // TODO: Eventually do this in GAME to begin with
            if(GAME.getCurColor() == WHITE) {
                GAME.setCurColor(BLACK);
            } else {
                GAME.setCurColor(WHITE);
            }
        }
    }

    @Override
    public void removeStone(int x, int y) {
        board[x][y] = null;
        fileSaver.removeStone(x,y);

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

    // Private methods

    /**
     * Notifies all listeners that a stone has been set.
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @param c the StoneColor of the stone that has been set
     */
    private void fireStoneSet(int x, int y, StoneColor c) {
        GameCommand gc = GameCommand.BLACKPLAYS;
        if(c == WHITE) {
            gc = GameCommand.WHITEPLAYS;
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

    public boolean saveFile(Path path){
       return fileSaver.saveFile(path);
    }

    public boolean importFile(Path path){
        return FileSaver.importFile(path);
    }

    // Getters and Setters
    public int getSize() {
        return SIZE;
    }

    public StoneColor getColorAt(int x, int y) {
        if(board[x][y] != null) {
            return board[x][y].getStoneGroup().getStoneColor();
        } else {
            return null;
        }
    }

    public Game getGAME() {
        return GAME;
    }

    public int getSIZE() {
        return SIZE;
    }

    public StoneGroupPointer[][] getBoard() {
        return board;
    }

    public int getLastDebugX() {
        return lastDebugX;
    }

    public int getLastDebugY() {
        return lastDebugY;
    }
}