package pr_se.gogame.view_controller.observer;

import pr_se.gogame.model.helper.GameCommand;

/**
 * Special GameEvent used for Debugging. Separate to avoid removal in case it is no longer desired
 */
public class DebugEvent extends GameEvent {
    /**
     * The serial number of the StoneGroup that this DebugEvent pertains to
     */
    private final int groupNo;

    /**
     * The serial number of the StoneGroupPointer that this DebugEvent pertains to
     */
    private final int ptrNo;


    /**
     * Creates a new DebugEvent
     * @param col X coordinate, starting at the left
     * @param row Y coordinate, starting at the top
     * @param ptrNo serial number of the StoneGroupPointer that this Event pertains to
     * @param groupNo serial number of the StoneGroup that this Event pertains to
     */
    public DebugEvent(int col, int row, int ptrNo, int groupNo) {
        super(GameCommand.DEBUG_INFO, col, row, 0);
        this.groupNo = groupNo;
        this.ptrNo = ptrNo;
    }

    /**
     * @return the serial number of the StoneGroup that this DebugEvent pertains to
     */
    public int getGroupNo() {
        return groupNo;
    }

    /**
     * @return the serial number of the StoneGroupPointer that this DebugEvent pertains to
     */
    public int getPtrNo() {
        return ptrNo;
    }
}
