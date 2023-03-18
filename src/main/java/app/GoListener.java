package app;

import java.util.EventListener;

public interface GoListener extends EventListener {
    public void stoneSet(StoneSetEvent e);
}
