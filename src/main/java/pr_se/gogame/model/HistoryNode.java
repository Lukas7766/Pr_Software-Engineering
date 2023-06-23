package pr_se.gogame.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class HistoryNode {
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
        RESIGN
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
        this.command = command;
        setComment(comment);
        this.saveToken = saveToken;
        this.color = color;
        this.x = -1;
        this.y = -1;
    }

    public HistoryNode(UndoableCommand command, AbstractSaveToken saveToken, StoneColor color, int x, int y, String comment) {
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

    public void setPrev(HistoryNode prev) {
        this.prev = prev;
        prev.next = this;
    }

    public HistoryNode getNext() {
        return next;
    }

    public void setNext(HistoryNode next) {
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
}
