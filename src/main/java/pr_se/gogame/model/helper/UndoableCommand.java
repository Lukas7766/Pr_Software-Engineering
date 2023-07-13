package pr_se.gogame.model.helper;

import pr_se.gogame.view_controller.observer.GameEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

/**
 * Template for Commands that can be un- and redone. Contains static convenience methods
 */
public abstract class UndoableCommand {

    /**
     * The GameEvents that are to be fired on (re-)execution of this UndoableCommand
     */
    private final List<GameEvent> executeEvents = new LinkedList<>();

    /**
     * The GameEvents that are to be fired on undoing this UndoableCommand
     */
    private final List<GameEvent> undoEvents = new LinkedList<>();

    /**
     * Executes this UndoableCommand
     */
    public abstract void execute();

    /**
     * Undoes this UndoableCommand
     */
    public abstract void undo();

    /**
     * Returns the list of GameEvents that are to be fired on (re-)execution of this UndoableCommand
     * @return the list of GameEvents that are to be fired on (re-)execution of this UndoableCommand
     */
    public List<GameEvent> getExecuteEvents() {
        return executeEvents;
    }

    /**
     * Returns the list of GameEvents that are to be fired on undoing this UndoableCommand
     * @return the list of GameEvents that are to be fired on undoing this UndoableCommand
     */
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

    /**
     * Generates an UndoableCommand that updates a variable
     * @param updateMethod Consumer that takes the value to be applied (consider this like a pass by reference)
     * @param oldValue The value to be applied upon undoing the returned command
     * @param newValue The value to be applied upon (re-)executing the returned command
     * @return An UndoableCommand that calls the updateMethod with the newValue upon (re-)execution or the oldValue upon being undone
     * @param <T> The type of the variable to be updated
     */
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
