package pr_se.gogame.model;

import javafx.geometry.Pos;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
    private final LinkedList<Position> locations;

    /**
     * all the free Positions surrounding this StoneGroup
     */
    private final Set<Position> liberties;

    /**
     * all the StoneGroupPointers pointing to this StoneGroup
     */
    private final Set<StoneGroupPointer> pointers;

    // TODO: Remove these debug variables
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
        // TODO: Remove this debug code
        serialNo = nextSerialNo;
        nextSerialNo++;
    }

    /**
     * Adds the locations and liberties of the supplied StoneGroup to this one
     * @param other StoneGroup that is to be merged with this one
     */
    public UndoableCommand mergeWithStoneGroup(StoneGroup other) {
        if(other == null) {
            throw new NullPointerException();
        }

        if(other.getStoneColor() != stoneColor) {
            throw new IllegalArgumentException("Stone group must be of the same color!");
        }

        final LinkedList<Position> OLD_LOCATIONS = new LinkedList<>();
        OLD_LOCATIONS.addAll(locations);
        final HashSet<Position> OLD_LIBERTIES = new HashSet<>();
        OLD_LIBERTIES.addAll(liberties);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                locations.addAll(other.getLocations());
                liberties.addAll(other.getLiberties());
                other.getPointers().forEach(p -> {
                    p.setStoneGroup(StoneGroup.this);
                    addPointer(p);
                });
            }

            @Override
            public void undo() {
                locations.remove(locations);
                locations.addAll(OLD_LOCATIONS);
                liberties.remove(liberties);
                liberties.addAll(OLD_LIBERTIES);
                other.getPointers().forEach(p -> {
                    p.setStoneGroup(other);
                    pointers.remove(p);
                });
            }
        };

        ret.execute();

        return ret;
    }

    /**
     * Adds the supplied liberty to this StoneGroup
     * @param liberty unoccupied Position to be added to this StoneGroup
     */
    public UndoableCommand addLiberty(Position liberty) {
        if(liberty == null) {
            throw new NullPointerException();
        }

        final boolean WAS_CONTAINED = liberties.contains(liberty);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                liberties.add(liberty);
            }

            @Override
            public void undo() {
                if(!WAS_CONTAINED) {
                    liberties.remove(liberty);
                }
            }
        };

        ret.execute();

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

        final boolean WAS_CONTAINED = liberties.contains(liberty);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                liberties.remove(liberty);
            }

            @Override
            public void undo() {
                if(WAS_CONTAINED) {
                    liberties.add(liberty);
                }
            }
        };

        ret.execute();

        return ret;
    }

    // Getters and Setters
    public StoneColor getStoneColor() {
        return stoneColor;
    }

    public LinkedList<Position> getLocations() {
        return locations;
    }

    public Set<Position> getLiberties() {
        return this.liberties;
    }

    public void addPointer(StoneGroupPointer ptr) {
        pointers.add(ptr);
    }

    public Set<StoneGroupPointer> getPointers() {
        return pointers;
    }

    public void removeLocation(Position location) {
        locations.remove(location);
    }
}
