package pr_se.gogame.model;

import java.util.LinkedList;

public class StoneGroup {
    private final StoneColor stoneColor;

    private final LinkedList<Position> locations;

    private int liberties;

    public StoneGroup(StoneColor stoneColor, int x, int y, int liberties) {
        this.stoneColor = stoneColor;
        this.locations = new LinkedList<>();
        this.liberties = 0;
        addLocation(x, y, liberties);
    }

    public void addLocation(int x, int y, int addedLiberties) {
        // removeLiberties(1); // TODO: Unsure whether this should be done in here as part of updating the liberties.
        addLiberties(addedLiberties);
        locations.add(new Position(x, y));
    }

    public void mergeWithStoneGroupPtr(StoneGroupPointer other) {
        if(other == null) {
            throw new NullPointerException();
        }

        if(other.getStoneGroup().getStoneColor() != stoneColor) {
            throw new IllegalArgumentException("Stone group must be of same color!");
        }

        addLiberties(other.getStoneGroup().getLiberties());
        this.locations.addAll(other.getStoneGroup().getLocations());
        other.setStoneGroup(this);
    }

    public void addLiberties(int addedLiberties) {
        liberties += addedLiberties;
    }

    public void removeLiberties(int removedLiberties) {
        liberties -= removedLiberties;
    }

    // Getters and Setters
    public StoneColor getStoneColor() {
        return stoneColor;
    }

    public LinkedList<Position> getLocations() {
        return locations;
    }

    public int getLiberties() {
        return liberties;
    }

    public void setLiberties(int liberties) {
        this.liberties = liberties;
    }
}
