package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;

// Todo: Remove this in the final product
public class DebugEvent extends GameEvent {
    private final int groupNo;
    private final int ptrNo;


    public DebugEvent(int col, int row, int ptrNo, int groupNo) {
        super(GameCommand.DEBUG_INFO, col, row, null, 0);
        this.groupNo = groupNo;
        this.ptrNo = ptrNo;
    }

    public int getGroupNo() {
        return groupNo;
    }

    public int getPtrNo() {
        return ptrNo;
    }
}
