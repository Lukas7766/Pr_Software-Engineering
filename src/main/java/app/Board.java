package app;

import java.util.LinkedList;

/**
 * Controller Dummy (for now)
 */
public class Board {
    private final int SIZE;
    private final LinkedList<GoListener> listeners;

    // Likely to be removed
    private StoneColor curColor = StoneColor.BLACK;

    public Board(int size) {
        this.SIZE = size;
        listeners = new LinkedList<>();
    }

    public void addListener(GoListener l) {
        listeners.add(l);
    }

    public void removeListener(GoListener l) {
        listeners.remove(l);
    }

    public void setStone(int x, int y) {
        System.out.println("Board will set " + curColor + " stone at x " + x + ", y " + y);

        fireStoneSet(x, y, curColor);

        if(curColor == StoneColor.WHITE) {
            curColor = StoneColor.BLACK;
        } else {
            curColor = StoneColor.WHITE;
        }
    }

    public int getSize() {
        return SIZE;
    }

    public StoneColor getCurColor() {
        return curColor;
    }

    private void fireStoneSet(int x, int y, StoneColor c) {
        StoneSetEvent e = new StoneSetEvent(x, y, c);

        for(GoListener l : listeners) {
            l.stoneSet(e);
        }
    }
}
