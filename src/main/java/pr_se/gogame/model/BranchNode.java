package pr_se.gogame.model;

public class BranchNode extends Node{

    //TODO Do I need this
    private Node branchStart;
    public BranchNode(String token) {
        super(token);
    }

    public Node getBranchStart() {
        return branchStart;
    }


}
