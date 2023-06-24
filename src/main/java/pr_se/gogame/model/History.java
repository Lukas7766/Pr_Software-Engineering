package pr_se.gogame.model;

import pr_se.gogame.view_controller.observer.GameEvent;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class History implements Iterable<HistoryNode> {

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
            current.getCommand().execute(false);
            current.getCommand().getExecuteEvents().forEach(game::fireGameEvent);

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

    private HistoryNode getFirstMeaningfulNode() {
        HistoryNode first = current;
        while(first.getPrev() != null) {
            first = first.getPrev();
        }
        first = first.getNext();
        return first;
    }

    @Override
    public Iterator<HistoryNode> iterator() {
        return new Iterator<>() {

            HistoryNode node = getFirstMeaningfulNode();

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public HistoryNode next() {
                if(hasNext()) {
                    HistoryNode ret = node;
                    node = node.getNext();
                    return ret;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public void forEach(Consumer<? super HistoryNode> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<HistoryNode> spliterator() {
        return Iterable.super.spliterator();
    }
}
