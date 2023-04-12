package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;
import pr_se.gogame.view_controller.GoListener;
import pr_se.gogame.view_controller.StoneRemovedEvent;
import pr_se.gogame.view_controller.StoneSetEvent;

import static pr_se.gogame.model.StoneColor.*;

import java.util.*;

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
        Set<StoneGroup> surroundingSGs = getSurroundingStoneGroups(x, y);
        System.out.println("No. neighbor groups: " + surroundingSGs.size());

        Set<Position> newStoneLiberties = getLibertiesAt(x, y);
        StoneGroup newGroup = new StoneGroup(color, x, y, newStoneLiberties);

        StoneGroup firstSameColorGroup = null;

        for(StoneGroup sg : surroundingSGs) {
            if(sg != null) {
                sg.removeLiberty(new Position(x, y));
                if(sg.getStoneColor() == color) {
                    if(firstSameColorGroup != null) {
                        firstSameColorGroup.mergeWithStoneGroup(sg);
                    } else {
                        sg.mergeWithStoneGroup(newGroup);
                        firstSameColorGroup = sg;
                    }
                }
            }
        }

        if(firstSameColorGroup == null) {
            firstSameColorGroup = newGroup;
            surroundingSGs.add(newGroup);
        }
        board[x][y] = firstSameColorGroup.getPointers().stream().findFirst().orElseGet(() -> new StoneGroupPointer(newGroup));

        for(StoneGroup sg : surroundingSGs) {
            if ((sg.getStoneColor() != color || sg == firstSameColorGroup) && sg.getLiberties().size() == 0) {
                for (Position p : sg.getLocations()) {
                    removeStone(p.X, p.Y);
                }
            }
        }

        if(board[x][y] == null) {
            System.out.println("SUICIDE DETECTED!!!");
            return;
        }

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
        board[x][y] = null;

        for(StoneGroup sg : getSurroundingStoneGroups(x, y)) {
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
                    for (GoListener l : listeners) {
                        l.debugInfoRequested(i, j, board[i][j].serialNo, board[i][j].getStoneGroup().serialNo);
                    }
                }
            }
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

    private Set<StoneGroup> getSurroundingStoneGroups(int x, int y) {
        if(x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            throw new IllegalArgumentException();
        }

        Set<StoneGroup> existingGroups = new HashSet<>();
        if(y > 0 && board[x][y - 1] != null) {
            existingGroups.add(board[x][y - 1].getStoneGroup());
        }
        if(y < SIZE - 1 && board[x][y + 1] != null) {
            existingGroups.add(board[x][y + 1].getStoneGroup());
        }
        if(x > 0 && board[x - 1][y] != null) {
            existingGroups.add(board[x - 1][y].getStoneGroup());
        }
        if(x < SIZE - 1 && board[x + 1][y] != null) {
            existingGroups.add(board[x + 1][y].getStoneGroup());
        }

        return existingGroups;
    }

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