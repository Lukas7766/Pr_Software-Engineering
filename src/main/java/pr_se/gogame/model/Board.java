package pr_se.gogame.model;

import pr_se.gogame.view_controller.DebugEvent;
import pr_se.gogame.view_controller.GameEvent;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Model
 * Go Board internal logic
 */
public class Board implements BoardInterface {
    /**
     * the Game that this Board belongs to
     */
    private final Game game;

    /**
     * the number of rows and columns of this board
     */
    private final int size;

    /**
     * the actual board
     */
    private final StoneGroupPointer[][] boardContents;

    private int lastDebugX = -1;
    private int lastDebugY = -1;

    /**
     * Creates a new Board belonging to the specified Game, containing handicap stones of the specified beginner color
     * (only if the Game has a handicap set)
     *
     * @param game the Game that this Board belongs to
     * @param size
     */
    public Board(Game game, int size) {
        this.game = game;
        this.size = size;
        this.boardContents = new StoneGroupPointer[this.size][this.size];
    }

    @Override
    public UndoableCommand setStone(int x, int y, StoneColor color, boolean prepareMode) {
        checkXYCoordinates(x, y);

        if(color == null) {
            throw new NullPointerException();
        }

        if (boardContents[x][y] != null) {
            return null;
        }

        // Get liberties at these x and y coordinates
        Set<Position> newStoneLiberties = getSurroundings(
            x,
            y,
            sgp -> sgp == null,
            (neighborX, neighborY) -> new Position(neighborX, neighborY)
        );
        StoneGroup newGroup = new StoneGroup(color, x, y, newStoneLiberties);

        // Get neighboring stone(group)s at these x and y coordinates
        Set<StoneGroup> surroundingSGs = getSurroundings(
            x,
            y,
            sgp -> sgp != null,
            (neighborX, neighborY) -> boardContents[neighborX][neighborY].getStoneGroup()
        );

        /*
         * Existing group of the same colour with maximum no. of liberties (which is relevant for the suicide-check)
         */
        StoneGroup firstSameColorGroup = surroundingSGs.stream()
            .filter(sg -> sg.getStoneColor() == color)
            .max(Comparator.comparingInt(sg -> sg.getLiberties().size()))
            .orElse(newGroup);

        final List<UndoableCommand> subcommands = new LinkedList<>();

        // Check for suicide
        boolean permittedSuicide = false;
        boolean killAnother = false;
        Set<StoneGroup> otherColorGroups = surroundingSGs.stream().filter(sg -> sg.getStoneColor() != color).collect(Collectors.toSet());

        if (!prepareMode && newGroup.getLiberties().size() == 0 && (newGroup == firstSameColorGroup || firstSameColorGroup.getLiberties().size() == 1)) { // if adding this stone would take away all liberties from the group it's being added to
            if (otherColorGroups.stream().noneMatch(sg -> sg.getLiberties().size() == 1)) { // if there are any groups of the opposite color with only one liberty, the attacker wins and the existing group is removed instead.
                System.out.println("SUICIDE DETECTED!!!");
                if (!game.getRuleset().getSuicide(firstSameColorGroup, newGroup)) {
                    return null;
                }
                System.out.println("Suicide permitted.");
                permittedSuicide = true;
            } else {
                killAnother = true;
            }
        }

        /*
         * Merge newly-connected StoneGroups of the same color and remove the new stone's position from the liberties
         * of all adjacent groups
         */
        Set<StoneGroup> sameColorGroups = new HashSet<>(surroundingSGs);
        sameColorGroups.removeAll(otherColorGroups);
        sameColorGroups.remove(firstSameColorGroup);

        final UndoableCommand UC01_ADD_NEW_TO_FIRST = (firstSameColorGroup != newGroup) ? (firstSameColorGroup.mergeWithStoneGroup(newGroup)) : (null);
        subcommands.add(UC01_ADD_NEW_TO_FIRST);

        for(StoneGroup sg : sameColorGroups) {
            subcommands.add(firstSameColorGroup.mergeWithStoneGroup(sg));
        }

        final UndoableCommand UC03_REMOVE_NEW_POS_FROM_FIRST_LIBERTIES = firstSameColorGroup.removeLiberty(new Position(x, y)); // in case any of the now obsolete, "eaten" stone groups contained this liberty
        subcommands.add(UC03_REMOVE_NEW_POS_FROM_FIRST_LIBERTIES);

        for(StoneGroup sg : otherColorGroups) {
            subcommands.add(sg.removeLiberty(new Position(x, y)));
        }

        final boolean FINAL_PERMITTED_SUICIDE = permittedSuicide;
        final UndoableCommand UC05_PLACE_POINTER = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                if (!FINAL_PERMITTED_SUICIDE) {
                    boardContents[x][y] =
                        firstSameColorGroup.getPointers().stream()
                            .findFirst()
                            .orElseGet(() -> new StoneGroupPointer(newGroup));
                }
            }

            @Override
            public void undo() {
                boardContents[x][y] = null;
            }
        };
        UC05_PLACE_POINTER.execute(true);
        subcommands.add(UC05_PLACE_POINTER);

        final boolean FINAL_KILL_ANOTHER = killAnother;
        final boolean PREPARE_MODE = prepareMode;
        final StoneColor COLOR = color;

        final UndoableCommand UC06_REMOVE_CAPTURED_STONES = new UndoableCommand() {
            final LinkedList<UndoableCommand> UC06_01_REMOVE_STONE_COMMANDS = new LinkedList<>();
            UndoableCommand uC06_02_addCapturedStonesCommand = null;

            @Override
            public void execute(boolean saveEffects) {
                if (!PREPARE_MODE) {

                    for (StoneGroup sg : surroundingSGs) {
                        if ((sg.getStoneColor() != COLOR || !FINAL_KILL_ANOTHER) && sg.getLiberties().size() == 0) {
                            int captured = 0;
                            for (Position p : sg.getLocations()) {
                                UndoableCommand tmpCmd = removeStone(p.x, p.y);
                                UC06_01_REMOVE_STONE_COMMANDS.add(tmpCmd);
                                if(saveEffects) {
                                    getExecuteEvents().addAll(tmpCmd.getExecuteEvents());
                                    getUndoEvents().addAll(tmpCmd.getUndoEvents());
                                }
                                captured++;
                            }
                            uC06_02_addCapturedStonesCommand = game.addCapturedStones(COLOR, captured);
                        }
                    }
                }

                // Update UI if possible
                if (!FINAL_PERMITTED_SUICIDE && saveEffects) {
                    getExecuteEvents().add(new GameEvent(GameCommand.STONE_WAS_SET, x, y, COLOR, game.getCurMoveNumber()));
                    getUndoEvents().add(new GameEvent(GameCommand.STONE_WAS_REMOVED, x, y, null, game.getCurMoveNumber()));
                }
            }

            @Override
            public void undo() {
                for(UndoableCommand c : UC06_01_REMOVE_STONE_COMMANDS) {
                    c.undo();
                }

                if(uC06_02_addCapturedStonesCommand != null) {
                    uC06_02_addCapturedStonesCommand.undo();
                }
            }
        };
        UC06_REMOVE_CAPTURED_STONES.execute(true);
        subcommands.add(UC06_REMOVE_CAPTURED_STONES);

        final List<UndoableCommand> SUBCOMMANDS = Collections.unmodifiableList(subcommands);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                for(UndoableCommand c : SUBCOMMANDS) {
                    if(c != null) {
                        c.execute(saveEffects);
                    }
                }
            }

            @Override
            public void undo() {
                // Undoing it the other way round just in case.
                ListIterator<UndoableCommand> i = SUBCOMMANDS.listIterator(SUBCOMMANDS.size());
                while(i.hasPrevious()) {
                    UndoableCommand c = i.previous();
                    if(c != null) {
                        c.undo();
                    }
                }
            }
        };
        // No execute() this time, as we've already executed the subcommands piecemeal.
        ret.getExecuteEvents().addAll(UC06_REMOVE_CAPTURED_STONES.getExecuteEvents());
        ret.getUndoEvents().addAll(UC06_REMOVE_CAPTURED_STONES.getUndoEvents());

        return ret;
    }

    @Override
    public UndoableCommand removeStone(int x, int y) {
        checkXYCoordinates(x, y);

        final StoneGroupPointer BOARD_AT_XY_PREVIOUSLY = boardContents[x][y];

        UndoableCommand ret = new UndoableCommand() {

            final List<UndoableCommand> ADD_LIBERTY_COMMANDS = new LinkedList<>();

            @Override
            public void execute(boolean saveEffects) {
                boardContents[x][y] = null;

                Set<StoneGroup> surroundingSGs = getSurroundings(
                    x,
                    y,
                    sgp -> sgp != null,
                    (neighborX, neighborY) -> boardContents[neighborX][neighborY].getStoneGroup()
                );
                for (StoneGroup sg : surroundingSGs) {
                    ADD_LIBERTY_COMMANDS.add(sg.addLiberty(new Position(x, y)));
                }

                // Update UI
                if(saveEffects) {
                    getExecuteEvents().add(new GameEvent(GameCommand.STONE_WAS_REMOVED, x, y, null, game.getCurMoveNumber()));
                    getUndoEvents().add(new GameEvent(GameCommand.STONE_WAS_SET, x, y, BOARD_AT_XY_PREVIOUSLY.getStoneGroup().getStoneColor(), game.getCurMoveNumber()));
                }
            }

            @Override
            public void undo() {
                boardContents[x][y] = BOARD_AT_XY_PREVIOUSLY;

                for(UndoableCommand c : ADD_LIBERTY_COMMANDS) {
                    c.undo();
                }
            }
        };
        ret.execute(true);

        return ret;
    }

    // Private methods

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
        checkXYCoordinates(x, y);

        Set surroundings = new HashSet<>();

        if (y > 0 && check.test(boardContents[x][y - 1])) {
            surroundings.add(conversion.apply(x, y - 1));
        }
        if (y < size - 1 && check.test(boardContents[x][y + 1])) {
            surroundings.add(conversion.apply(x, y + 1));
        }
        if (x > 0 && check.test(boardContents[x - 1][y])) {
            surroundings.add(conversion.apply(x - 1, y));
        }
        if (x < size - 1 && check.test(boardContents[x + 1][y])) {
            surroundings.add(conversion.apply(x + 1, y));
        }

        return surroundings;
    }

    // Getters and Setters
    @Override
    public int getSize() {
        return size;
    }

    @Override
    public StoneColor getColorAt(int x, int y) {
        checkXYCoordinates(x, y);

        if (boardContents[x][y] != null) {
            return boardContents[x][y].getStoneGroup().getStoneColor();
        } else {
            return null;
        }
    }

    // TODO: Remove these debug methods
    public void printDebugInfo(int x, int y) {
        checkXYCoordinates(x, y);

        if (boardContents[x][y] != null && !(x == lastDebugX && y == lastDebugY)) {
            System.out.println("Group at " + x + ", " + y + ":");
            System.out.println("Liberties: " + boardContents[x][y].getStoneGroup().getLiberties().size());
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (boardContents[i][j] != null) {
                    DebugEvent e = new DebugEvent(i, j, boardContents[i][j].serialNo, boardContents[i][j].getStoneGroup().serialNo);
                    game.fireGameEvent(e);
                }
            }
        }

        lastDebugX = x;
        lastDebugY = y;
    }

    /**
     * Tests whether these x and y coordinates are outside the bounds of the playing field
     * @param x x coordinate starting at the left
     * @param y y coordinate starting at the top
     * @return whether these x and y coordinates are outside the playing field.
     */
    private void checkXYCoordinates(int x, int y) throws IllegalArgumentException {
        if(x < 0 || y < 0 || x >= size || y >= size) {
            throw new IllegalArgumentException("Coordinates X=" + x + ", Y=" + y + " are out of bounds for board.");
        }
    }
}