package pr_se.gogame.model;

import pr_se.gogame.view_controller.observer.GameEvent;

import java.util.*;
import java.util.function.Consumer;

public class History implements Iterable<HistoryNode> {

    private final Game game;

    private HistoryNode current;

    public History(Game game) {
        if(game == null) {
            throw new NullPointerException();
        }
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
        if(addedNode == null) {
            throw new NullPointerException();
        }
        if(addedNode == current) {
            throw new IllegalArgumentException("Cannot add the same node again, as that would create a cycle!");
        }

        current.setNext(addedNode);
        current = current.getNext();
    }

    public String getCurrentComment() {
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

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        History h = (History)o;

        Iterator<HistoryNode> otherIter = h.iterator();
        for(HistoryNode hn : this) {
            if(!otherIter.hasNext()) {
                return false;
            }
            if(!hn.equals(otherIter.next())) {
                return false;
            }
        }

        return !otherIter.hasNext();
    }

    @Override
    public int hashCode() {
        List<Object> valueList = new LinkedList<>();

        for (HistoryNode historyNode : this) {
            valueList.add(historyNode);
        }

        valueList.add(game);

        return Objects.hash(valueList.toArray());
    }

    // Interface Iterable
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

    // Overridden Methods from Object
    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder("History \n");
        this.forEach(hn -> retVal.append(hn.toString()).append("\n"));
        return retVal.toString();
    }
}
