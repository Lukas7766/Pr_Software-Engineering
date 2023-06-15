package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;

import java.util.ListIterator;

public class GeraldsHistory {

    private final Game game;

    private final GeraldsNode head;

    private GeraldsNode current;

    public GeraldsHistory(Game game) {
        this.game = game;
        head = new GeraldsNode(null, null);
        current = head;
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
            for(GameEvent e : current.getCommand().getExecuteEvents()) {
                game.fireGameEvent(e);
            }

            return true;
        }

        return false;
    }

    public void addNode(GeraldsNode addedNode) {
        current.setNext(addedNode);
        current = current.getNext();
    }

    public String currentComment() {
        return current.getComment();
    }
}
