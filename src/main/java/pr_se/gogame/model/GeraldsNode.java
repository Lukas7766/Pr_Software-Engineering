package pr_se.gogame.model;

public class GeraldsNode {
    public enum AbstractSaveToken {

        HANDICAP,

        MOVE,

        PASS,           // SGF saves this as a black or white move with an empty coordinate.

        /*
         * SGF doesn't save this (for Go games, other games have this), but it could be used for the FileSaver to detect
         * that it has to write a !RE[sult] token. If it does, bear in mind that this token saves which color resigned,
         * but the RE-token should contain who won (so the opposite).
         */
        RESIGN
    }

    private GeraldsNode prev;

    private GeraldsNode next;

    private final UndoableCommand command;

    private String comment;

    private final AbstractSaveToken saveToken;

    private final int X;

    private final int Y;

    private final StoneColor color;

    public GeraldsNode(UndoableCommand command, AbstractSaveToken saveToken, StoneColor color, String comment) {
        if(comment == null) {
            throw new NullPointerException("Comment must at least be an empty string!");
        }

        this.command = command;
        this.comment = comment;
        this.saveToken = saveToken;
        this.color = color;
        this.X = -1;
        this.Y = -1;
    }

    public GeraldsNode(UndoableCommand command, AbstractSaveToken saveToken, StoneColor color, int x, int y, String comment) {
        if(comment == null) {
            throw new NullPointerException("Comment must at least be an empty string!");
        }
        this.command = command;
        this.comment = comment;
        this.saveToken = saveToken;
        this.color = color;
        X = x;
        Y = y;
    }

    public UndoableCommand getCommand() {
        return command;
    }

    public GeraldsNode getPrev() {
        return prev;
    }

    public void setPrev(GeraldsNode prev) {
        this.prev = prev;
        prev.next = this;
    }

    public GeraldsNode getNext() {
        return next;
    }

    public void setNext(GeraldsNode next) {
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
        return X;
    }

    public int getY() {
        return Y;
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
}
