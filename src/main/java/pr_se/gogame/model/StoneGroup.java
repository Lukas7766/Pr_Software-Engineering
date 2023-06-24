package pr_se.gogame.model;

import java.util.*;

/**
 * Model
 * Represents a group of stones on the Go Board, containing the stones' positions and the liberties of the group
 */
public class StoneGroup {
    /**
     * Color of this StoneGroup
     */
    private final StoneColor stoneColor;

    /**
     * all the Positions where stones of this StoneGroup are located
     */
    private final List<Position> locations;

    /**
     * all the free Positions surrounding this StoneGroup
     */
    private final Set<Position> liberties;

    /**
     * all the StoneGroupPointers pointing to this StoneGroup
     */
    private final Set<StoneGroupPointer> pointers;

    private static int nextSerialNo = 0;
    public final int serialNo;

    /**
     * Creates a StoneGroup of the specified color at the specified coordinates with the specified liberties
     * @param stoneColor the color of the StoneGroup
     * @param x Horizontal coordinate on the Board from 0 to size-1, starting on the left
     * @param y Vertical coordinate on the Board from 0 to size-1, starting on the top
     * @param liberties Set of all Positions next to this StoneGroup that are free
     */
    public StoneGroup(StoneColor stoneColor, int x, int y, Set<Position> liberties) {
        this.stoneColor = stoneColor;
        this.locations = new LinkedList<>();
        this.liberties = liberties;
        this.pointers = new HashSet<>();

        locations.add(new Position(x, y));

        serialNo = nextSerialNo;
        nextSerialNo++;
    }

    /**
     * Adds the locations and liberties of the supplied StoneGroup to this one
     * @param other StoneGroup that is to be merged with this one
     */
    public UndoableCommand mergeWithStoneGroup(final StoneGroup other) {
        if(other == null) {
            throw new NullPointerException();
        }

        if(other.getStoneColor() != stoneColor) {
            throw new IllegalArgumentException("Stone group must be of the same color!");
        }

        final List<Position> oldLocations = List.copyOf(locations);
        final List<Position> newLocations = List.copyOf(other.getLocations());
        final Set<Position> oldLiberties = Set.copyOf(liberties);
        final Set<Position> newLiberties = Set.copyOf(other.getLiberties());

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                locations.addAll(newLocations);
                liberties.addAll(newLiberties);
                other.getPointers().forEach(p -> {
                    p.setStoneGroup(StoneGroup.this);
                    pointers.add(p);
                });
            }

            @Override
            public void undo() {
                locations.clear();
                locations.addAll(oldLocations);
                liberties.clear();
                liberties.addAll(oldLiberties);
                other.getPointers().forEach(p -> {
                    p.setStoneGroup(other);
                    pointers.remove(p);
                });
            }
        };

        ret.execute(true);

        return ret;
    }

    /**
     * Adds the supplied liberty to this StoneGroup
     * @param liberty unoccupied Position to be added to this StoneGroup
     */
    public UndoableCommand addLiberty(final Position liberty) {
        if(liberty == null) {
            throw new NullPointerException();
        }

        final boolean wasContained = liberties.contains(liberty);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                liberties.add(liberty);
            }

            @Override
            public void undo() {
                if(!wasContained) {
                    liberties.remove(liberty);
                }
            }
        };

        ret.execute(true);

        return ret;
    }

    /**
     * Removes the supplied liberty from this StoneGroup
     * @param liberty unoccupied Position to be removed from this StoneGroup
     */
    public UndoableCommand removeLiberty(Position liberty) {
        if(liberty == null) {
            throw new NullPointerException();
        }

        final boolean wasContained = liberties.contains(liberty);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                liberties.remove(liberty);
            }

            @Override
            public void undo() {
                if(wasContained) {
                    liberties.add(liberty);
                }
            }
        };

        ret.execute(true);

        return ret;
    }

    // Getters and Setters
    public StoneColor getStoneColor() {
        return stoneColor;
    }

    public List<Position> getLocations() {
        return locations;
    }

    public Set<Position> getLiberties() {
        return this.liberties;
    }

    public void addPointer(StoneGroupPointer ptr) {
        if(ptr == null) {
            throw new NullPointerException();
        }

        pointers.add(ptr);
    }

    public Set<StoneGroupPointer> getPointers() {
        return pointers;
    }

    public static void resetDebug() {
        nextSerialNo = 0;
    }
}
