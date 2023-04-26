package pr_se.gogame.model;

public class BranchNode extends Node{

    //TODO Do I need this
    private Node branchStart;
    public BranchNode(SgfToken token,String data) {
        super(token,data);
    }

    public Node getBranchStart() {
        return branchStart;
    }


}
