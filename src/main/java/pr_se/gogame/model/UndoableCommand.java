package pr_se.gogame.model;

import java.util.LinkedList;
import java.util.List;

public interface UndoableCommand {
    public void execute();

    public void undo();
}
