package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;

import java.util.ListIterator;
import java.util.Map;

public class History {

    private final Game game;

    private HistoryNode current;

    public History(Game game) {
        this.game = game;
        current = new HistoryNode(null, null, null, ""); // This solely exists so that the first move can be undone without an edge case.
    }

    public void rewind() {
        while(stepBack());
    }

    public void skipToEnd() {
        while(stepForward());
    }

    public boolean stepBack() {
        if(current.getPrev() != null) {
            System.out.println("Undoing " + current.getComment());
            current.getCommand().undo();
            ListIterator<GameEvent> i = current.getCommand().getUndoEvents().listIterator(current.getCommand().getUndoEvents().size());
            current = current.getPrev();
            while(i.hasPrevious()) {
                game.fireGameEvent(i.previous());
            }

            return true;
        }

        return false;
    }

    public boolean stepForward() {
        if(current.getNext() != null) {
            current = current.getNext();
            System.out.println("Re-Doing " + current.getComment());
            current.getCommand().execute(false);
            current.getCommand().getExecuteEvents().stream().forEach(e -> game.fireGameEvent(e));

            return true;
        }

        return false;
    }

    public void addNode(HistoryNode addedNode) {
        current.setNext(addedNode);
        current = current.getNext();
    }

    public String currentComment() {
        return current.getComment();
    }

    public HistoryNode getCurrentNode() {
        return current;
    }

    public boolean isAtEnd() {
        return current.getNext() == null;
    }

    public boolean isAtBeginning() {
        return current.getPrev() == null;
    }
}
