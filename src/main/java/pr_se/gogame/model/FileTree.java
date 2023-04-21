package pr_se.gogame.model;

import java.util.LinkedList;

public class FileTree {
    //TODO: write the file Tree class
    //TODO: use java linked list or make own ?
    private LinkedList<Node> tree = new LinkedList<>();



    //TODO: printing for visualization

    public void printGameTree(Node current){
        if (current == null){
            return;
        }
        if (current.getClass() == BranchNode.class){
            //System.out.print(String Token+"-");
            //printGameTree(current.next);
        }else {
            //System.out.println(current.getToken()+"\n |");
            //printGameTree(current.next);
        }

    }
}
