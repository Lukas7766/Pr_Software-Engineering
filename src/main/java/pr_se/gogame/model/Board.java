package pr_se.gogame.model;

import pr_se.gogame.model.helper.GameCommand;
import pr_se.gogame.model.helper.Position;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;
import pr_se.gogame.view_controller.observer.DebugEvent;
import pr_se.gogame.view_controller.observer.GameEvent;

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

    /**
     * Creates a new Board belonging to the specified Game, containing handicap stones of the specified beginner color
     * (only if the Game has a handicap set)
     *
     * @param game the Game that this Board belongs to
     * @param size the size of the board
     */
    public Board(Game game, int size) {
        this.game = game;
        this.size = size;
        this.boardContents = new StoneGroupPointer[this.size][this.size];
        StoneGroup.resetDebug();
    }

    @Override
    public UndoableCommand setStone(int x, int y, final StoneColor color, final boolean prepareMode) {
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
            Objects::isNull,
            Position::new
        );
        StoneGroup newGroup = new StoneGroup(color, x, y, newStoneLiberties);

        // Get neighboring stone(group)s at these x and y coordinates
        Set<StoneGroup> surroundingSGs = getSurroundings(
            x,
            y,
            Objects::nonNull,
            (neighborX, neighborY) -> boardContents[neighborX][neighborY].getStoneGroup()
        );

        /*
         * Existing group of the same colour with maximum no. of liberties (which is relevant for the suicide-check)
         */
        StoneGroup firstSameColorGroup = surroundingSGs.stream()
            .filter(sg -> sg.getStoneColor() == color)
            .max(Comparator.comparingInt(sg -> sg.getLiberties().size()))
            .orElse(newGroup);

        // Check for suicide
        boolean permittedSuicide = false;
        Set<StoneGroup> otherColorGroups = surroundingSGs.stream()
            .filter(sg -> sg.getStoneColor() != color)
            .collect(Collectors.toSet());

        if (!prepareMode && newGroup.getLiberties().isEmpty() && firstSameColorGroup.getLiberties().size() <= 1) { // if adding this stone would take away all liberties from the group it's being added to
            if (otherColorGroups.stream().noneMatch(sg -> sg.getLiberties().size() == 1)) { // if there are any groups of the opposite color with only one liberty, the attacker wins and the existing group is removed instead.
                if (!game.getRuleset().getSuicide(firstSameColorGroup, newGroup)) {
                    return null;
                }
                permittedSuicide = true;
            }
        }

        /*
         * Merge newly connected StoneGroups of the same color and remove the new stone's position from the liberties
         * of all adjacent groups
         */
        Set<StoneGroup> sameColorGroups = new HashSet<>(surroundingSGs);
        sameColorGroups.removeAll(otherColorGroups);
        sameColorGroups.remove(firstSameColorGroup);

        final List<UndoableCommand> subcommands = new LinkedList<>();

        final UndoableCommand uc01AddNewToFirst = (firstSameColorGroup != newGroup) ? (firstSameColorGroup.mergeWithStoneGroup(newGroup)) : (null);
        if(uc01AddNewToFirst != null) {
            subcommands.add(uc01AddNewToFirst);
        }

        for(StoneGroup sg : sameColorGroups) {
            subcommands.add(firstSameColorGroup.mergeWithStoneGroup(sg));
        }

        final UndoableCommand uc02RemoveNewPosFromFirstLiberties = firstSameColorGroup.removeLiberty(new Position(x, y)); // in case any of the now obsolete, "eaten" stone groups contained this liberty
        subcommands.add(uc02RemoveNewPosFromFirstLiberties);

        for(StoneGroup sg : otherColorGroups) {
            subcommands.add(sg.removeLiberty(new Position(x, y)));
        }

        final UndoableCommand uc03PlacePointer = (permittedSuicide) ? (null) : new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                boardContents[x][y] =
                    firstSameColorGroup.getPointers().stream()
                        .findFirst()
                        .orElseGet(() -> new StoneGroupPointer(newGroup));
            }

            @Override
            public void undo() {
                boardContents[x][y] = null;
            }
        };
        if(uc03PlacePointer != null) {
            uc03PlacePointer.execute(true);
            subcommands.add(uc03PlacePointer);
        }

        final boolean finalPermittedSuicide = permittedSuicide;

        UndoableCommand uc04RemoveCapturedStones = (prepareMode) ? (null) : new UndoableCommand() {
            final LinkedList<UndoableCommand> uc0401RemoveStoneCommands = new LinkedList<>();
            UndoableCommand uC0402AddCapturedStonesCommand = null;

            @Override
            public void execute(boolean saveEffects) {
                for (StoneGroup sg : surroundingSGs) {
                    if ((sg.getStoneColor() != color || finalPermittedSuicide) && sg.getLiberties().isEmpty()) {
                        for (Position p : sg.getLocations()) {
                            UndoableCommand tmpCmd = removeStone(p.getX(), p.getY());
                            uc0401RemoveStoneCommands.add(tmpCmd);
                            if(saveEffects) {
                                getExecuteEvents().addAll(tmpCmd.getExecuteEvents());
                                getUndoEvents().addAll(tmpCmd.getUndoEvents());
                            }
                        }
                        uC0402AddCapturedStonesCommand = game.addCapturedStones(color, sg.getLocations().size());
                    }
                }
            }

            @Override
            public void undo() {
                if(uC0402AddCapturedStonesCommand != null) {
                    uC0402AddCapturedStonesCommand.undo();
                }

                for(UndoableCommand c : uc0401RemoveStoneCommands) {
                    c.undo();
                }
            }
        };
        if(uc04RemoveCapturedStones != null) {
            uc04RemoveCapturedStones.execute(true);
            subcommands.add(uc04RemoveCapturedStones);
        }

        final List<UndoableCommand> finalSubCommands = List.copyOf(subcommands);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                for(UndoableCommand c : finalSubCommands) {
                    if(c != null) {
                        c.execute(saveEffects);
                    }
                }
            }

            @Override
            public void undo() {
                // Undoing it the other way round just in case.
                ListIterator<UndoableCommand> i = finalSubCommands.listIterator(finalSubCommands.size());
                while(i.hasPrevious()) {
                    UndoableCommand c = i.previous();
                    if(c != null) {
                        c.undo();
                    }
                }
            }
        };
        // No execute() this time, as we've already executed the subcommands piecemeal.
        if(uc04RemoveCapturedStones != null) {
            ret.getExecuteEvents().addAll(uc04RemoveCapturedStones.getExecuteEvents());
            ret.getUndoEvents().addAll(uc04RemoveCapturedStones.getUndoEvents());
        }
        if(!permittedSuicide) {
            ret.getExecuteEvents().add(new GameEvent(GameCommand.STONE_WAS_SET, x, y, color, game.getCurMoveNumber()));
            ret.getUndoEvents().add(new GameEvent(GameCommand.STONE_WAS_REMOVED, x, y, null, game.getCurMoveNumber()));
        }


        return ret;
    }

    @Override
    public UndoableCommand removeStone(int x, int y) {
        checkXYCoordinates(x, y);

        final StoneGroupPointer boardAtXyPreviously = boardContents[x][y];

        UndoableCommand ret = new UndoableCommand() {

            final List<UndoableCommand> addLibertyCommands = new LinkedList<>();

            @Override
            public void execute(boolean saveEffects) {
                boardContents[x][y] = null;

                Set<StoneGroup> surroundingSGs = getSurroundings(
                    x,
                    y,
                        Objects::nonNull,
                    (neighborX, neighborY) -> boardContents[neighborX][neighborY].getStoneGroup()
                );
                for (StoneGroup sg : surroundingSGs) {
                    addLibertyCommands.add(sg.addLiberty(new Position(x, y)));
                }

                // Update UI
                if(saveEffects) {
                    getExecuteEvents().add(new GameEvent(GameCommand.STONE_WAS_REMOVED, x, y, null, game.getCurMoveNumber()));
                    if(boardAtXyPreviously != null) {
                        getUndoEvents().add(new GameEvent(GameCommand.STONE_WAS_SET, x, y, boardAtXyPreviously.getStoneGroup().getStoneColor(), game.getCurMoveNumber()));
                    }
                }
            }

            @Override
            public void undo() {
                boardContents[x][y] = boardAtXyPreviously;

                for(UndoableCommand c : addLibertyCommands) {
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

    public void printDebugInfo(int x, int y) {
        checkXYCoordinates(x, y);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (boardContents[i][j] != null) {
                    DebugEvent e = new DebugEvent(i, j, boardContents[i][j].serialNo, boardContents[i][j].getStoneGroup().serialNo);
                    game.fireGameEvent(e);
                }
            }
        }

    }

    /**
     * Tests whether these x and y coordinates are outside the bounds of the playing field
     * @param x x coordinate starting at the left
     * @param y y coordinate starting at the top
     */
    private void checkXYCoordinates(int x, int y) throws IllegalArgumentException {
        if(x < 0 || y < 0 || x >= size || y >= size) {
            throw new IllegalArgumentException("Coordinates X=" + x + ", Y=" + y + " are out of bounds for board.");
        }
    }
}