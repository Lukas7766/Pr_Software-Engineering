package pr_se.gogame.model;

import pr_se.gogame.view_controller.GoListener;
import pr_se.gogame.view_controller.StoneRemovedEvent;
import pr_se.gogame.view_controller.StoneSetEvent;

import static pr_se.gogame.model.RelativeDirection.*;
import static pr_se.gogame.model.StoneColor.*;

import java.util.*;

/**
 * Model Dummy (for now)
 */
public class Board implements BoardInterface {
    private final int SIZE;
    private final LinkedList<GoListener> listeners;

    private final StoneGroupPointer[][] board;

    // Likely to be removed
    private StoneColor curColor = BLACK;

    private int lastDebugX = 0;
    private int lastDebugY = 0;

    public Board(int size) {
        this.SIZE = size;
        listeners = new LinkedList<>();
        this.board = new StoneGroupPointer[SIZE][SIZE];
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

        int newStoneLiberties = 4 - surroundingSGPs.size();
        if(x == 0 || x == SIZE - 1) newStoneLiberties--;
        if(y == 0 || y == SIZE - 1) newStoneLiberties--;

        if(surroundingSGPs.size() > 0) {
            StoneGroupPointer firstSameColorGroupPtr = null;

            StoneGroupPointer abovePtr = surroundingSGPs.get(ABOVE);
            StoneGroup aboveGroup = null;
            if(abovePtr != null) {
                System.out.println("AbovePtr isn't null!");
                aboveGroup = abovePtr.getStoneGroup();
                aboveGroup.removeLiberties(1);
                if(aboveGroup.getStoneColor() == color) {
                    aboveGroup.addLocation(x, y, newStoneLiberties);
                    firstSameColorGroupPtr = abovePtr;
                }
            }

            StoneGroupPointer belowPtr = surroundingSGPs.get(BELOW);
            StoneGroup belowGroup = null;
            if(belowPtr != null && belowPtr != abovePtr) {
                belowGroup = belowPtr.getStoneGroup();
                if(belowGroup != aboveGroup) {
                    belowGroup.removeLiberties(1);
                    if(belowGroup.getStoneColor() == color) {
                        if(firstSameColorGroupPtr != null) {
                            firstSameColorGroupPtr.getStoneGroup().mergeWithStoneGroupPtr(belowPtr);
                        } else {
                            belowGroup.addLocation(x, y, newStoneLiberties);
                            firstSameColorGroupPtr = belowPtr;
                        }
                    }
                }
            }

            StoneGroupPointer leftPtr = surroundingSGPs.get(LEFT);
            StoneGroup leftGroup = null;
            if(leftPtr != null && leftPtr != abovePtr && leftPtr != belowPtr) {
                leftGroup = leftPtr.getStoneGroup();
                if(leftGroup != aboveGroup && leftGroup != belowGroup) {
                    leftGroup.removeLiberties(1);
                    if(leftGroup.getStoneColor() == color) {
                        if(firstSameColorGroupPtr != null) {
                            firstSameColorGroupPtr.getStoneGroup().mergeWithStoneGroupPtr(leftPtr);
                        } else {
                            leftGroup.addLocation(x, y, newStoneLiberties);
                            firstSameColorGroupPtr = leftPtr;
                        }
                    }
                }
            }

            StoneGroupPointer rightPtr = surroundingSGPs.get(RIGHT);
            StoneGroup rightGroup = null;
            if(rightPtr != null && rightPtr != abovePtr && rightPtr != belowPtr && rightPtr != leftPtr) {
                rightGroup = rightPtr.getStoneGroup();
                if(rightGroup != aboveGroup && rightGroup != belowGroup && rightGroup != leftGroup) {
                    rightGroup.removeLiberties(1);
                    if(rightGroup.getStoneColor() == color) {
                        if(firstSameColorGroupPtr != null) {
                            firstSameColorGroupPtr.getStoneGroup().mergeWithStoneGroupPtr(rightPtr);
                        } else {
                            rightGroup.addLocation(x, y, newStoneLiberties);
                            firstSameColorGroupPtr = rightPtr;
                        }
                    }
                }
            }

            if(firstSameColorGroupPtr == null) {
                board[x][y] = new StoneGroupPointer(new StoneGroup(color, x, y, newStoneLiberties));
            } else {
                board[x][y] = firstSameColorGroupPtr;
            }

            StoneGroup newStoneGroup = board[x][y].getStoneGroup();

            HashSet<StoneGroup> uniqueGroups = new HashSet<>();
            uniqueGroups.add(aboveGroup);
            uniqueGroups.add(belowGroup);
            uniqueGroups.add(leftGroup);
            uniqueGroups.add(rightGroup);
            uniqueGroups.remove(null);

            for(StoneGroup sg : uniqueGroups) {
                if(sg.getLiberties() < 0) {
                    throw new IllegalStateException("Less than 0 liberties!");
                } else if(sg.getLiberties() == 0) {
                    for(Position p : sg.getLocations()) {
                        removeStone(p.X, p.Y);
                    }
                }
            }
        } else {
            board[x][y] = new StoneGroupPointer(new StoneGroup(color, x, y, newStoneLiberties));
        }

        if(board[x][y] == null) {
            System.out.println("SUICIDE DETECTED!!!");
            return;
        }

        System.out.println("Board will set " + color + " stone at x " + x + ", y " + y);

        // Update UI
        fireStoneSet(x, y, color);

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
        System.out.println("Board will remove " + board[x][y].getStoneGroup().getStoneColor() + " stone at x " + x + ", y " + y);

        board[x][y] = null;
        fireStoneRemoved(x, y);

        Map<RelativeDirection, StoneGroupPointer> neighbors = getSurroundingStoneGroupPtrs(x, y);
        HashSet<StoneGroupPointer> uniqueNeighborPtrs = new HashSet<>();
        uniqueNeighborPtrs.addAll(neighbors.values());
        HashSet<StoneGroup> uniqueNeighborGroups = new HashSet<>();
        for(StoneGroupPointer sgp : uniqueNeighborPtrs) {
            uniqueNeighborGroups.add(sgp.getStoneGroup());
        }
        for(StoneGroup sg : uniqueNeighborGroups) {
            sg.addLiberties(1);
        }
    }

    public void printDebugInfo(int x, int y) {
        if(board[x][y] != null && !(x == lastDebugX && y == lastDebugY)) {
            System.out.println("Group at " + x + ", " + y + ":");
            System.out.println("Liberties: " + board[x][y].getStoneGroup().getLiberties());
        }

        lastDebugX = x;
        lastDebugY = y;
    }

    // Private methods
    private void fireStoneSet(int x, int y, StoneColor c) {
        StoneSetEvent e = new StoneSetEvent(x, y, c);

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

    // Getters and Setters
    public int getSize() {
        return SIZE;
    }

    public StoneColor getCurColor() {
        return curColor;
    }
}