package pr_se.gogame.model;

public class GeraldsNode {

    private GeraldsNode prev;

    private GeraldsNode next;

    private final UndoableCommand command;

    private final String comment;

    public GeraldsNode(UndoableCommand command, String comment) {
        this.command = command;
        this.comment = comment;
    }

    public UndoableCommand getCommand() {
        return command;
    }

    public String getComment() {
        return comment;
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
}
