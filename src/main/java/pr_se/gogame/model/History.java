package pr_se.gogame.model;

import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.helper.Position;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;
import pr_se.gogame.view_controller.observer.GameEvent;

import java.util.*;
import java.util.function.Consumer;

public class History implements Iterable<History.HistoryNode> {
    private final HistoryNode beginning = new HistoryNode(null, HistoryNode.AbstractSaveToken.BEGINNING_OF_HISTORY, null, "");
    private final HistoryNode end = new HistoryNode(null, HistoryNode.AbstractSaveToken.END_OF_HISTORY, null, "");

    private final Game game;

    private HistoryNode current;

    public History(Game game) {
        if(game == null) {
            throw new NullPointerException();
        }
        this.game = game;
        current = beginning;
        current.setNext(end);
    }

    public void gotoBeginning() {
        while(stepBack());
    }

    public void gotoEnd() {
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
        if(!isAtEnd()) {
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
        current.setNext(end);
    }

    public HistoryNode getCurrentNode() {
        return current;
    }

    public boolean isAtEnd() {
        return current.getNext() == end;
    }

    public boolean isAtBeginning() {
        return current.getPrev() == null;
    }

    // Methods from Iterable
    @Override
    public Iterator<HistoryNode> iterator() {
        return new Iterator<>() {

            HistoryNode node = beginning;

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

    // Methods from Object
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
            if(!hn.equals(otherIter.next())) {
                return false;
            }
        }

        return true;
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

    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder("History \n");
        this.forEach(hn -> retVal.append(hn.toString()).append("\n"));
        return retVal.toString();
    }

    // inner classes
    public static class HistoryNode {
        public enum AbstractSaveToken {

            HANDICAP,

            SETUP,

            MOVE,

            PASS,           // SGF saves this as a black or white move with an empty coordinate.

            /*
             * SGF doesn't save this (for Go games, other games have this), but it could be used for the FileSaver to detect
             * that it has to write a !RE[sult] token. If it does, bear in mind that this token saves which color resigned,
             * but the RE-token should contain who won (so the opposite).
             */
            RESIGN,

            SCORED_GAME,

            END_OF_HISTORY,      // removes the need for complicated edge cases

            BEGINNING_OF_HISTORY // no real purpose as yet, but seems sensible for demonstrating intent.
        }

        private HistoryNode prev;

        private HistoryNode next;

        private final UndoableCommand command;

        private String comment;

        private final AbstractSaveToken saveToken;

        private final int x;

        private final int y;

        private final StoneColor color;

        private final Map<Position, MarkShape> marks = new LinkedHashMap<>();

        public HistoryNode(UndoableCommand command, AbstractSaveToken saveToken, StoneColor color, String comment) {
            this(command, saveToken, color, comment, -1, -1);
        }

        public HistoryNode(UndoableCommand command, AbstractSaveToken saveToken, StoneColor color, String comment, int x, int y) {
            if(saveToken == null) {
                throw new NullPointerException();
            }

            this.command = command;
            setComment(comment);
            this.saveToken = saveToken;
            this.color = color;
            this.x = x;
            this.y = y;
        }

        public UndoableCommand getCommand() {
            return command;
        }

        public HistoryNode getPrev() {
            return prev;
        }

        public HistoryNode getNext() {
            return next;
        }

        private void setNext(HistoryNode next) {
            this.next = next;
            next.prev = this;
        }

        // Game-related info
        public String getComment() {
            return comment;
        }

        public AbstractSaveToken getSaveToken() {
            return saveToken;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public StoneColor getColor() {
            return color;
        }

        public void setComment(String comment) {
            if(comment == null) {
                throw new NullPointerException("Comment must at least be an empty string!");
            }
            this.comment = comment;
        }

        public Map<Position, MarkShape> getMarks() {
            return marks;
        }

        public void removeMark(int x, int y) {
            marks.remove(new Position(x, y));
        }

        public void addMark(int x, int y, MarkShape shape) {
            marks.put(new Position(x, y), shape);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HistoryNode that = (HistoryNode) o;
            return x == that.x && y == that.y && comment.equals(that.comment) && saveToken == that.saveToken && color == that.color && marks.equals(that.marks);
        }

        @Override
        public int hashCode() {
            return Objects.hash(command, comment, saveToken, x, y, color, marks);
        }

        @Override
        public String toString() {
            return "HistoryNode " + saveToken + " " + color + " (x = " + x + ", y = " + y + "): " + comment;
        }
    } // public static class HistoryNode
}
