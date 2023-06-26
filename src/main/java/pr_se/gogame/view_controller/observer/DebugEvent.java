package pr_se.gogame.view_controller.observer;

import pr_se.gogame.model.helper.GameCommand;

public class DebugEvent extends GameEvent {
    private final int groupNo;
    private final int ptrNo;


    public DebugEvent(int col, int row, int ptrNo, int groupNo) {
        super(GameCommand.DEBUG_INFO, col, row, 0);
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
