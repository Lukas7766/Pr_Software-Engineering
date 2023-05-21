package pr_se.gogame.model;

public class TreeNode extends Node{
    public TreeNode(SgfToken token,String data) {
        super(token,data);
    }
    public TreeNode(SgfToken token,String coordinates,String letters) {
        super(token,coordinates,letters);
    }
}
