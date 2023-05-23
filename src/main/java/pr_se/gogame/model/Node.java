package pr_se.gogame.model;

public abstract class Node {
   //TODO: include move number

    private Node next;
    private Node previous;

    private String token;

    //private BranchNode branchNode = null;

    private int moveNumber;

    public Node(SgfToken token, String data) {
        this.token = String.format(token.getValue(),data);
    }

    public Node(SgfToken token, String data,int moveNumber) {
        this.token = String.format(token.getValue(),data);
    }

    public Node(SgfToken token,String coordinates,String letters) {
        this.token = String.format(token.getValue(),coordinates,letters);
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
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

   /* public void setBranchNode(BranchNode branchNode) {
        branchNode.setPrevious(this);
        this.branchNode = branchNode;
    }*/

    public Node getNext() {
        return next;
    }

    public Node getPrevious() {
        return previous;
    }

    public String getToken() {
        return token;
    }

    /*public BranchNode getBranchNode() {
        return branchNode;
    }*/

    @Override
    public String toString() {
        return token;
    }
}
