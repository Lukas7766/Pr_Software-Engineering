package pr_se.gogame.model;

import javafx.geometry.Pos;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class StoneGroup {
    private final StoneColor stoneColor;

    private final LinkedList<Position> locations;

    private final Set<Position> liberties;

    //private final StoneGroupPointer pointer;

    public StoneGroup(StoneColor stoneColor, int x, int y, Set<Position> liberties) {
        this.stoneColor = stoneColor;
        this.locations = new LinkedList<>();
        this.liberties = liberties;
        locations.add(new Position(x, y));
    }

    public void mergeWithStoneGroupPtr(StoneGroupPointer other) {
        if(other == null) {
            throw new NullPointerException();
        }

        mergeWithStoneGroup(other.getStoneGroup());

        other.setStoneGroup(this);
    }

    public void mergeWithStoneGroup(StoneGroup other) {
        if(other == null) {
            throw new NullPointerException();
        }

        if(other.getStoneColor() != stoneColor) {
            throw new IllegalArgumentException("Stone group must be of same color!");
        }

        addLiberties(other.getLiberties());
        locations.addAll(other.getLocations());
    }

    public void addLiberties(Set<Position> addedLiberties) {
        if(addedLiberties == null) {
            throw new NullPointerException();
        }

        liberties.addAll(addedLiberties);
    }

    public void removeLiberties(Set<Position> removedLiberties) {
        if(removedLiberties == null) {
            throw new NullPointerException();
        }

        liberties.removeAll(removedLiberties);
    }

    public void addLiberty(Position liberty) {
        if(liberty == null) {
            throw new NullPointerException();
        }

        liberties.add(liberty);
    }

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

    /*public StoneGroupPointer getPointer() {
        return pointer;
    }*/
}
