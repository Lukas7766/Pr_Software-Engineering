package pr_se.gogame.model;

import java.util.LinkedList;

public class FileTree {
    //TODO: write the file Tree class
    //TODO: use java linked list or make own ?
    private StartNode start;

    public StartNode getStart() {
        return start;
    }

    public void setStart(StartNode start) {
        this.start = start;
    }

    //TODO: printing for visualization

    public static void printGameTree(Node current){
        if (current == null){
            System.out.println("BranchClosed");
            return;
        }
//TODO: this dabsolutely does not work
        if (current.getClass() == BranchNode.class && current.getBranchNode() == null){
            System.out.print(current.getToken()+"Branch closed\n");
            return;
        }

        if (current.getClass() == BranchNode.class || current.getBranchNode() != null){
            System.out.print(current.getToken()+"-");
            printGameTree(current.getBranchNode());
        }else {
            System.out.println(current.getToken()+"\n |");
            printGameTree(current.getNext());
        }

    }
}
