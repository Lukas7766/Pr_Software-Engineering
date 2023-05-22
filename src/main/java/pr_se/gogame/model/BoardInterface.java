package pr_se.gogame.model;

public interface BoardInterface {
    /**
     * Places a stone on the board, calculating its liberties and adding it to an existing group where applicable, as
     * well as merging groups if necessary.
     * @param color Whether the stone to be placed is StoneColor.BLACK or WHITE
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @param prepareMode Whether this stone is set before the beginning of the game (e.g., as a handicap) or by a (human or AI) player
     * @param save Whether this move is to be saved
     */
    public boolean setStone(int x, int y, StoneColor color, boolean prepareMode, boolean save);

    /**
     *
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @param save Whether this move is to be saved
     */
    public void removeStone(int x, int y, boolean save);

    // Getters
    public int getSize();

    /**
     * Returns the stone color at the specified location
     * @param x Horizontal coordinate from 0 to size-1, starting on the left
     * @param y Vertical coordinate from 0 to size-1, starting on the top
     * @return the stone color at the specified location or null if no stone is set
     */
    public StoneColor getColorAt(int x, int y);

    public Game getGAME();
}
