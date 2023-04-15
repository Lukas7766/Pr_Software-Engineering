package pr_se.gogame.model;

import pr_se.gogame.view_controller.GoListener;

public interface BoardInterface {
    public void addListener(GoListener l);

    public void removeListener(GoListener l);

    /**
     *
     * @param color
     * @param x: Horizontal coordinate from 0 to size-1, starting on the left
     * @param y: Vertical coordinate from 0 to size-1, starting on the top
     */
    public void setStone(int x, int y, StoneColor color, boolean prepareMode);

    public void removeStone(int x, int y);
}
