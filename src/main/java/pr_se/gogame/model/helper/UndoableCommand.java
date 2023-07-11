package pr_se.gogame.model.helper;

import pr_se.gogame.view_controller.observer.GameEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public abstract class UndoableCommand {

    private final List<GameEvent> executeEvents = new LinkedList<>();

    private final List<GameEvent> undoEvents = new LinkedList<>();

    /**
     * Executes this UndoableCommand
     */
    public abstract void execute();

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
            public void execute() {
                finalSubcommands.forEach(UndoableCommand::execute);
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

    public static <T> UndoableCommand updateValue(final Consumer<T> updateMethod, final T oldValue, final T newValue) {
        if(updateMethod == null) {
            throw new NullPointerException();
        }

        return new UndoableCommand() {
            @Override
            public void execute() {
                updateMethod.accept(newValue);
            }

            @Override
            public void undo() {
                updateMethod.accept(oldValue);
            }
        };
    }
}
