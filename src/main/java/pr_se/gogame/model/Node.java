package pr_se.gogame.model;

public abstract class Node {
    //TODO Write the Node class for file structure
    //TODO: maybe include an index for a node ?

    private Node next;
    private Node previous;

    private String token;

    private BranchNode branchNode = null;

    public Node(SgfToken token, String data) {
        this.token = String.format(token.getValue(),data);
    }

    public void setNext(Node next) {
        next.previous = this;
        this.next = next;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setBranchNode(BranchNode branchNode) {
        branchNode.setPrevious(this);
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
