package pr_se.gogame.model;

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
     * Adds the locations and liberties of the StoneGroup pointed to by the supplied pointer to this StoneGroup
     * @param other StoneGroupPointer to the StoneGroup that is to be merged with this one
     */
    public void mergeWithStoneGroupPtr(StoneGroupPointer other) {
        if(other == null) {
            throw new NullPointerException();
        }

        mergeWithStoneGroup(other.getStoneGroup());

        other.setStoneGroup(this);
    }

    /**
     * Adds the locations and liberties of the supplied StoneGroup to this one
     * @param other StoneGroup that is to be merged with this one
     */
    public void mergeWithStoneGroup(StoneGroup other) {
        if(other == null) {
            throw new NullPointerException();
        }

        if(other.getStoneColor() != stoneColor) {
            throw new IllegalArgumentException("Stone group must be of the same color!");
        }

        addLiberties(other.getLiberties());
        locations.addAll(other.getLocations());
        other.getPointers().forEach(p -> {
            p.setStoneGroup(this);
            addPointer(p);
        });
        other.removeAllPointers();
    }

    /**
     * Adds the supplied Set of liberties to this StoneGroup
     * @param addedLiberties Set of liberties to be added to this StoneGroup
     */
    public void addLiberties(Set<Position> addedLiberties) {
        if(addedLiberties == null) {
            throw new NullPointerException();
        }

        liberties.addAll(addedLiberties);
    }

    /**
     * Removes the supplied Set of liberties from this StoneGroup
     * @param removedLiberties Set of liberties to be removed from this StoneGroup
     */
    public void removeLiberties(Set<Position> removedLiberties) {
        if(removedLiberties == null) {
            throw new NullPointerException();
        }

        liberties.removeAll(removedLiberties);
    }

    /**
     * Adds the supplied liberty to this StoneGroup
     * @param liberty unoccupied Position to be added to this StoneGroup
     */
    public void addLiberty(Position liberty) {
        if(liberty == null) {
            throw new NullPointerException();
        }

        liberties.add(liberty);
    }

    /**
     * Removes the supplied liberty from this StoneGroup
     * @param liberty unoccupied Position to be removed from this StoneGroup
     */
    public void removeLiberty(Position liberty) {
        if(liberty == null) {
            throw new NullPointerException();
        }

        liberties.remove(liberty);
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

    public void removeAllPointers() {
        pointers.removeAll(pointers);
    }

    public Set<StoneGroupPointer> getPointers() {
        return pointers;
    }
}
