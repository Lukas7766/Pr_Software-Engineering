package pr_se.gogame.model.helper;

import pr_se.gogame.view_controller.observer.GameEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public abstract class UndoableCommand {

    private final List<GameEvent> executeEvents = new LinkedList<>();

    private final List<GameEvent> undoEvents = new LinkedList<>();

    /**
     * Executes this UndoableCommand
     * @param saveEffects If true, any action that should only be taken upon the very first execution of this command
     *                    will be taken (so that the command can be used, which reduces code duplication significantly).
     */
    public abstract void execute(final boolean saveEffects);

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

    /**
     * Wraps several UndoableCommands into a single one
     * @param subcommands the list of UndoableCommands to be wrapped into one
     * @return a new UndoableCommand which executes all subcommands and undoes them in reverse order
     */
    public static UndoableCommand of(final List<UndoableCommand> subcommands) {
        if(subcommands == null) {
            throw new NullPointerException();
        }

        List<UndoableCommand> finalSubcommands = List.copyOf(subcommands);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(final boolean saveEffects) {
                finalSubcommands.forEach(c -> c.execute(saveEffects));
            }

            @Override
            public void undo() {
                // Undoing it the other way round just in case.
                ListIterator<UndoableCommand> i = finalSubcommands.listIterator(finalSubcommands.size());
                while(i.hasPrevious()) {
                    UndoableCommand c = i.previous();
                    c.undo();
                }
            }
        };

        finalSubcommands.forEach(c -> {
            ret.getExecuteEvents().addAll(c.getExecuteEvents());
            ret.getUndoEvents().addAll(c.getUndoEvents());
        });

        return ret;
    }
}
