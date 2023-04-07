package pr_se.gogame.model;

import pr_se.gogame.view_controller.GoListener;
import pr_se.gogame.view_controller.StoneRemovedEvent;
import pr_se.gogame.view_controller.StoneSetEvent;

import static pr_se.gogame.model.RelativeDirection.*;
import static pr_se.gogame.model.StoneColor.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Model Dummy (for now)
 */
public class Board implements BoardInterface {
    private final int SIZE;
    private final LinkedList<GoListener> listeners;

    private final StoneGroupPointer[][] board;

    // TODO: Should this be moved to game?
    private int moveNumber;

    // Likely to be removed (or definitely moved to game).
    private StoneColor curColor = BLACK;

    private int lastDebugX = 0;
    private int lastDebugY = 0;

    public Board(int size) {
        this.SIZE = size;
        listeners = new LinkedList<>();
        this.board = new StoneGroupPointer[SIZE][SIZE];
        moveNumber = 1;
    }

    @Override
    public void addListener(GoListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(GoListener l) {
        listeners.remove(l);
    }

    @Override
    public void setStone(int x, int y, StoneColor color) {
        // Are the coordinates invalid?
        if(x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        // Is the space already occupied?
        if(board[x][y] != null) {
            return; // TODO: throw a custom exception?
        }

        // Get neighbors
        Map<RelativeDirection, StoneGroupPointer> surroundingSGPs = getSurroundingStoneGroupPtrs(x, y);
        System.out.println("No. neighbors: " + surroundingSGPs.size());

        Set<Position> newStoneLiberties = getLibertiesAt(x, y);
        StoneGroup newGroup = new StoneGroup(color, x, y, newStoneLiberties);

        // Are there even neighbors?
        StoneGroupPointer firstSameColorGroupPtr = null;
        Set<StoneGroupPointer> checkedPointers = new HashSet<>();
        Set<StoneGroup> checkedGroups = new HashSet<>();

        for(StoneGroupPointer sgp : surroundingSGPs.values()) {
            if(sgp != null & !checkedPointers.contains(sgp)) {
                StoneGroup curGroup = sgp.getStoneGroup();
                if(!checkedGroups.contains(curGroup)) {
                    curGroup.removeLiberty(new Position(x, y));
                    if(curGroup.getStoneColor() == color) {
                        if(firstSameColorGroupPtr != null) {
                            firstSameColorGroupPtr.getStoneGroup().mergeWithStoneGroupPtr(sgp);
                        } else {
                            curGroup.mergeWithStoneGroup(newGroup);
                            firstSameColorGroupPtr = sgp;
                        }
                    }
                    checkedGroups.add(curGroup);
                }
                checkedPointers.add(sgp);
            }
        }

        if(firstSameColorGroupPtr == null) {
            board[x][y] = new StoneGroupPointer(newGroup);
        } else {
            board[x][y] = firstSameColorGroupPtr;
        }

        for(StoneGroup sg : checkedGroups) {
            if ((sg.getStoneColor() != color || sg == firstSameColorGroupPtr.getStoneGroup()) && sg.getLiberties().size() == 0) {
                for (Position p : sg.getLocations()) {
                    removeStone(p.X, p.Y);
                }
            }
        }

        if(board[x][y] == null) {
            System.out.println("SUICIDE DETECTED!!!");
            return;
        }

        // System.out.println("Board will set " + color + " stone at x " + x + ", y " + y);

        // Update UI
        fireStoneSet(x, y, color);

        moveNumber++;

        // Update current player color
        // TODO: Remove and delegate to Game
        if(color == WHITE) {
            curColor = BLACK;
        } else {
            curColor = WHITE;
        }
    }

    @Override
    public void removeStone(int x, int y) {
        // System.out.println("Board will remove " + board[x][y].getStoneGroup().getStoneColor() + " stone at x " + x + ", y " + y);

        board[x][y] = null;

        Map<RelativeDirection, StoneGroupPointer> neighbors = getSurroundingStoneGroupPtrs(x, y);
        HashSet<StoneGroup> uniqueNeighborGroups = new HashSet<>(
            neighbors.values()
                .stream()
                .map(p -> p.getStoneGroup())
                .collect(Collectors.toSet()));

        for(StoneGroup sg : uniqueNeighborGroups) {
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

        lastDebugX = x;
        lastDebugY = y;
    }

    // Private methods
    private void fireStoneSet(int x, int y, StoneColor c) {
        StoneSetEvent e = new StoneSetEvent(x, y, c, moveNumber);

        for(GoListener l : listeners) {
            l.stoneSet(e);
        }
    }

    private void fireStoneRemoved(int x, int y) {
        StoneRemovedEvent e = new StoneRemovedEvent(x, y);

        for(GoListener l : listeners) {
            l.stoneRemoved(e);
        }
    }

    private Map<RelativeDirection, StoneGroupPointer> getSurroundingStoneGroupPtrs(int x, int y) {
        if(x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        Map<RelativeDirection, StoneGroupPointer> existingPtrs = new EnumMap<>(RelativeDirection.class);
        if(y > 0 && board[x][y - 1] != null) {
            existingPtrs.put(ABOVE, board[x][y - 1]);
        }
        if(y < SIZE - 1 && board[x][y + 1] != null) {
            existingPtrs.put(BELOW, board[x][y + 1]);
        }
        if(x > 0 && board[x - 1][y] != null) {
            existingPtrs.put(LEFT, board[x - 1][y]);
        }
        if(x < SIZE - 1 && board[x + 1][y] != null) {
            existingPtrs.put(RIGHT, board[x + 1][y]);
        }

        return existingPtrs;
    }

    /*private Map<Position, StoneGroupPointer> getSurroundingStoneGroupPtrs(int x, int y) {
        if(x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        Map<Position, StoneGroupPointer> existingPtrs = new HashMap<>();
        if(y > 0 && board[x][y - 1] != null) {
            existingPtrs.put(new Position(x, y - 1), board[x][y - 1]);
        }
        if(y < SIZE - 1 && board[x][y + 1] != null) {
            existingPtrs.put(new Position(x, y + 1), board[x][y + 1]);
        }
        if(x > 0 && board[x - 1][y] != null) {
            existingPtrs.put(new Position(x - 1, y), board[x - 1][y]);
        }
        if(x < SIZE - 1 && board[x + 1][y] != null) {
            existingPtrs.put(new Position(x + 1, y), board[x + 1][y]);
        }

        return existingPtrs;
    }*/

    private StoneGroup getStoneGroupAt(int x, int y) {
        if(x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        if(board[x][y] != null) {
            return board[x][y].getStoneGroup();
        }

        return null;
    }

    private Set<Position> getLibertiesAt(int x, int y) {
        if(x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        Set<Position> liberties = new HashSet<>();

        if(y > 0 && board[x][y - 1] == null) liberties.add(new Position(x, y - 1));
        if(y < SIZE - 1 && board[x][y + 1] == null) liberties.add(new Position(x, y + 1));
        if(x > 0 && board[x - 1][y] == null) liberties.add(new Position(x - 1, y));
        if(x < SIZE - 1 && board[x + 1][y] == null) liberties.add(new Position(x + 1, y));

        return liberties;
    }

    // Getters and Setters
    public int getSize() {
        return SIZE;
    }

    public StoneColor getCurColor() {
        return curColor;
    }
}