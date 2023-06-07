package pr_se.gogame.model;

public interface UndoableCommand {
    public void execute(boolean saveEffects); // TODO: We'll probably need a parameter for execute() to prevent it from re-saving an action that is simply being repeated.

    /*
     * TODO: The aforementioned parameter MIGHT be necessary here as well, although not as strictly, as one would never
     *  save when undoing to begin with (discarding the old "future" when going back would be handled by the
     *  File tree, right?).
     */
    public void undo();
}
