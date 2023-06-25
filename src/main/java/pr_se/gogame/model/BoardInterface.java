package pr_se.gogame.model;

import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;

public interface BoardInterface {
    /**
     * Places a stone on the board, calculating its liberties and adding it to an existing group where applicable, as
     * well as merging groups if necessary.
     *
     * @param x           Horizontal coordinate from 0 to size-1, starting on the left
     * @param y           Vertical coordinate from 0 to size-1, starting on the top
     * @param color       Whether the stone to be placed is StoneColor.BLACK or WHITE
     * @param prepareMode Whether this stone is set before the beginning of the game (e.g., as a handicap) or by a (human or AI) player
     */
    UndoableCommand setStone(int x, int y, StoneColor color, boolean prepareMode);

    /**
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     */
    UndoableCommand removeStone(int x, int y);

    // Getters
    int getSize();

    /**
     * Returns the stone color at the specified location
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @return the stone color at the specified location or null if no stone is set
     */
    StoneColor getColorAt(int x, int y);
}
