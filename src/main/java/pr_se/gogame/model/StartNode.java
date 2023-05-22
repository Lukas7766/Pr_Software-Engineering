package pr_se.gogame.model;

public class StartNode extends Node {
    public StartNode(int boardSize) {
        super(SgfToken.START,String.valueOf(boardSize));
    }
}
