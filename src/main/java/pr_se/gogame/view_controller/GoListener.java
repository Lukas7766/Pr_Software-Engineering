package pr_se.gogame.view_controller;

import pr_se.gogame.view_controller.StoneSetEvent;

import java.util.EventListener;

public interface GoListener extends EventListener {
    public void stoneSet(StoneSetEvent e);

    public void stoneRemoved(StoneRemovedEvent e);

    public void debugInfoRequested(int x, int y, int StoneGroupPtrNO, int StoneGroupSerialNo);
}
