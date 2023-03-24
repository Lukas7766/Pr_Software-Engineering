package pr_se.gogame;

import java.util.EventListener;

public interface GoListener extends EventListener {
    public void stoneSet(StoneSetEvent e);
}
