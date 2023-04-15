package pr_se.gogame.model;

import pr_se.gogame.view_controller.GoListener;

public interface BoardInterface {
    /**
     * Adds the supplied GoListener
     * @param l GoListener that handles StoneSetEvents and StoneRemovedEvents
     */
    public void addListener(GoListener l);

    /**
     * Removes the supplied GoListener
     * @param l GoListener that has previously been added with addListener()
     */
    public void removeListener(GoListener l);

    /**
     * Places a stone on the board, calculating its liberties and adding it to an existing group where applicable, as
     * well as merging groups if necessary.
     * @param color Whether the stone to be placed is StoneColor.BLACK or WHITE
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @param prepareMode Whether this stone is set before the beginning of the game (e.g., as a handicap) or by a (human or AI) player
     */
    public void setStone(int x, int y, StoneColor color, boolean prepareMode);

    /**
     *
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     */
    public void removeStone(int x, int y);
}
