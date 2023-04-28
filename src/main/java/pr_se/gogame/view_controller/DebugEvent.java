package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;

public class DebugEvent extends StoneEvent {
    private final int groupNo;
    private final int ptrNo;


    public DebugEvent(GameCommand gameCommand, int col, int row, int ptrNo, int groupNo) {
        super(gameCommand, col, row);
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
