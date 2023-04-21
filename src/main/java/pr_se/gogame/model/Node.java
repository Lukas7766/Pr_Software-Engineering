package pr_se.gogame.model;

public abstract class Node {
    //TODO Write the Node class for file structure
    private Node next;
    private Node previous;

    private String token;

    private BranchNode branchNode = null;

    public Node(String token) {
        this.token = token;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setBranchNode(BranchNode branchNode) {
        this.branchNode = branchNode;
    }

    public Node getNext() {
        return next;
    }

    public Node getPrevious() {
        return previous;
    }

    public String getToken() {
        return token;
    }

    public BranchNode getBranchNode() {
        return branchNode;
    }
}
