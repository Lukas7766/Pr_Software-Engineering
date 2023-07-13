package pr_se.gogame.model;

import pr_se.gogame.model.helper.*;
import pr_se.gogame.view_controller.observer.GameEvent;

import java.util.*;
import java.util.function.Consumer;

import static pr_se.gogame.model.History.HistoryNode.AbstractSaveToken.HANDICAP;
import static pr_se.gogame.model.History.HistoryNode.AbstractSaveToken.SETUP;

/**
 * Doubly-linked list that keeps track of its current position in the history and discards following nodes on insertion
 *  of a new one
 */
public class History implements Iterable<History.HistoryNode> {
    /**
     * The very first HistoryNode in this History
     */
    private final HistoryNode beginning = new HistoryNode(null, HistoryNode.AbstractSaveToken.BEGINNING_OF_HISTORY, null, "");
    /**
     * The very last HistoryNode in this History
     */
    private final HistoryNode end = new HistoryNode(null, HistoryNode.AbstractSaveToken.END_OF_HISTORY, null, "");

    /**
     * The Game that this History belongs to
     */
    private final Game game;

    /**
     * The currently selected HistoryNode
     */
    private HistoryNode current;

    /**
     * Creates a new History for the supplied Game
     * @param game the Game that this History belongs to
     */
    public History(Game game) {
        if(game == null) {
            throw new NullPointerException();
        }
        this.game = game;
        current = beginning;
        current.setNext(end);
    }

    /**
     * Traverses the history back to the beginning, undoing everything along the way
     */
    public void goToBeginning() {
        while(stepBack());
    }

    /**
     * Traverses the history to the end, redoing everything along the way
     */
    public void goToEnd() {
        while(stepForward());
    }

    /**
     * Goes back by a singly HistoryNode, undoing the current one.
     * @return whether there was a HistoryNode to step back to
     */
    public boolean stepBack() {
        if(!isAtBeginning()) {
            hideAllMarks();
            current.getCommand().undo();
            ListIterator<GameEvent> i = current.getCommand().getUndoEvents().listIterator(current.getCommand().getUndoEvents().size());
            current = current.getPrev();
            while(i.hasPrevious()) {
                game.fireGameEvent(i.previous());
            }
            showAllMarks();

            return true;
        }

        return false;
    }

    /**
     * Goes forward by a singly HistoryNode, redoing the one after the current one.
     * @return whether there was a HistoryNode to step forward to
     */
    public boolean stepForward() {
        if(!isAtEnd()) {
            hideAllMarks();
            current = current.getNext();
            current.getCommand().execute();
            current.getCommand().getExecuteEvents().forEach(game::fireGameEvent);
            showAllMarks();

            return true;
        }

        return false;
    }

    /**
     * Goes to the HistoryNode before the first one that contains a normal move or the last one of no first move
     *  exists.
     */
    public void goBeforeFirstMove() {
        goToFirstMove();
        if(current.getSaveToken() != SETUP && current.getSaveToken() != HANDICAP) {
            stepBack();
        }
    }

    /**
     * Goes to the first HistoryNode that contains a normal move or the last one if no such node exists.
     */
    public void goToFirstMove() {
        goToBeginning();

        do {
            stepForward();
        } while(!isAtEnd() && (current.getSaveToken() == HANDICAP || current.getSaveToken() == SETUP));
    }

    /**
     * Adds the supplied HistoryNode at the current position in the history, discarding any following history.
     * @param addedNode The HistoryNode to be added at the History's current position
     */
    public void addNode(HistoryNode addedNode) {
        if(addedNode == null) {
            throw new NullPointerException();
        }
        if(addedNode == current) {
            throw new IllegalArgumentException("Cannot add the same node again, as that would create a cycle!");
        }

        hideAllMarks();
        current.setNext(addedNode);
        current = current.getNext();
        current.setNext(end);
    }

    /**
     * @return The HistoryNode that this History is currently at
     */
    public HistoryNode getCurrentNode() {
        return current;
    }

    /**
     * @return Whether this History is at its end
     */
    public boolean isAtEnd() {
        return current.getNext() == end;
    }

    /**
     * @return Whether this History is at its beginning
     */
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

    // private methods

    /**
     * Tells the Game to hide all marks of the current HistoryNode
     */
    private void hideAllMarks() {
        current.getMarks().forEach((key, value) -> game.fireGameEvent(new GameEvent(GameCommand.UNMARK, key.getX(), key.getY(), game.getCurMoveNumber())));
    }

    /**
     * Tells the Game to (re-)display all marks of the current HistoryNode
     */
    private void showAllMarks() {
        current.getMarks().forEach((key, value) -> game.mark(key.getX(), key.getY(), value));
    }

    // inner classes

    /**
     * Contains information about a point in the History
     */
    public static class HistoryNode {
        /**
         * Specifies what kind of move this HistoryNode contains
         */
        public enum AbstractSaveToken {
            /**
             * Used for placing Handicap Stones
             */
            HANDICAP,

            /**
             * Used for placing setup stones
             */
            SETUP,

            /**
             * Used for normal moves
             */
            MOVE,

            /**
             * Used for pass moves
             */
            PASS,


            /**
             * Used for when a game was resigned
             */
            RESIGN,

            /**
             * Used for when a game was scored
             */
            SCORED_GAME,

            /**
             * Used as a marker for the very end of history
             */
            END_OF_HISTORY,

            /**
             * Used as a marker for the very beginning of history
             */
            BEGINNING_OF_HISTORY //
        }

        /**
         * The previous HistoryNode in the History
         */
        private HistoryNode prev;

        /**
         * The next HistoryNode in the History
         */
        private HistoryNode next;

        /**
         * This HistoryNode's UndoableCommand
         */
        private final UndoableCommand command;

        /**
         * The comment to this HistoryNode's move
         */
        private String comment;

        /**
         * The SaveToken specifying which kind of move this HistoryNode contains
         */
        private final AbstractSaveToken saveToken;

        /**
         * The x coordinate of a move (if any) of this HistoryNode, starting at the left.
         */
        private final int x;

        /**
         * The y coordinate of a move (if any) of this HistoryNode, starting at the top.
         */
        private final int y;

        /**
         * The color who played this HistoryNode's move
         */
        private final StoneColor color;

        /**
         * All marks on the playing field during this move
         */
        private final Map<Position, MarkShape> marks = new LinkedHashMap<>();

        /**
         * Creates a new HistoryNode without a position
         * @param command The UndoableCommand for this HistoryNode
         * @param saveToken The AbstractSaveToken denoting what kind of move this HistoryNode contains
         * @param color The color of this HistoryNode's move
         * @param comment The comment for this HistoryNode
         */
        public HistoryNode(UndoableCommand command, AbstractSaveToken saveToken, StoneColor color, String comment) {
            this(command, saveToken, color, comment, -1, -1);
        }

        /**
         * Creates a new HistoryNode with a position
         * @param command The UndoableCommand for this HistoryNode
         * @param saveToken The AbstractSaveToken denoting what kind of move this HistoryNode contains
         * @param color The color of this HistoryNode's move
         * @param comment The comment for this HistoryNode
         * @param x X coordinate of this HistoryNode, starting at the left
         * @param y Y coordinate of this HistoryNode, starting at the top
         */
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

        /**
         * @return This HistoryNode's UndoableCommand
         */
        public UndoableCommand getCommand() {
            return command;
        }

        /**
         * @return The HistoryNode before this one in the History
         */
        public HistoryNode getPrev() {
            return prev;
        }

        /**
         * @return The HistoryNode after this one in the History
         */
        public HistoryNode getNext() {
            return next;
        }

        /**
         * Links the supplied HistoryNode to be the next one in the History after this HistoryNode
         * @param next the HistoryNode that is supposed to follow this HistoryNode
         */
        private void setNext(HistoryNode next) {
            this.next = next;
            next.prev = this;
        }

        // Game-related info

        /**
         * @return This HistoryNode's move's comment
         */
        public String getComment() {
            return comment;
        }

        /**
         * @return This HistoryNode's AbstractSaveToken
         */
        public AbstractSaveToken getSaveToken() {
            return saveToken;
        }

        /**
         * @return This HistoryNodes X coordinate, starting at the left, if any. -1 if the HistoryNode has no position.
         */
        public int getX() {
            return x;
        }

        /**
         * @return This HistoryNodes Y coordinate, starting at the top, if any. -1 if the HistoryNode has no position.
         */
        public int getY() {
            return y;
        }

        /**
         * @return This HistoryNode's move's player color
         */
        public StoneColor getColor() {
            return color;
        }

        /**
         * Sets the comment for this HistoryNode's move
         * @param comment the comment for this HistoryNodes move
         */
        public void setComment(String comment) {
            if(comment == null) {
                throw new NullPointerException("Comment must at least be an empty string!");
            }
            this.comment = comment;
        }

        /**
         * @return all the marks on the playing field for the current move
         */
        public Map<Position, MarkShape> getMarks() {
            return marks;
        }

        /**
         * Removes a mark at the supplied position
         * @param x X coordinate starting at the left
         * @param y Y coordinate starting at the top
         */
        public void removeMark(int x, int y) {
            marks.remove(new Position(x, y));
        }

        /**
         * Adds a mark at the supplied position
         * @param x X coordinate starting at the left
         * @param y Y coordinate starting at the top
         * @param shape The desired shape of the mark
         */
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
