package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;

import java.util.LinkedList;
import java.util.List;

public abstract class UndoableCommand {

    private final List<GameEvent> executeEvents = new LinkedList<>();

    private final List<GameEvent> undoEvents = new LinkedList<>();

    /**
     * Executes this UndoableCommand
     * @param saveEffects if true, anything that might be saved to a file will actually be saved. If false, it will not
     *                    be saved. This is to prevent corruption of the saved file when stepping through the program.
     *                    This also determines whether any GameEvents to be fired in connection with this method call
     *                    are saved internally, to avoid duplicating them upon re-execution.
     */
    public abstract void execute(boolean saveEffects);

    /**
     * Undoes this UndoableCommand
     */
    public abstract void undo();

    public List<GameEvent> getExecuteEvents() {
        return executeEvents;
    }

    public List<GameEvent> getUndoEvents() {
        return undoEvents;
    }
}
