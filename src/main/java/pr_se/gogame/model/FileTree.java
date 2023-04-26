package pr_se.gogame.model;

import java.util.LinkedList;

public class FileTree {
    private StartNode start;
    private Node last;

    private Node currentBranch;

    public StartNode getStart() {
        return start;
    }

    public void addNode(Node node){
        //TODO: insert at the start
        //TODO: insert a branchnode
        //TODO: insert normally
        /*if (start == null && node.getClass() == StartNode.class){
            this.start = node; //warum geht nix diese
        }*/
    }


    public void setStart(StartNode start) {
        this.start = start;
        this.last = start;
    }

    public static void printGameTree(Node current) {
        if (current.getClass() == StartNode.class && current.getNext() != null) {
            System.out.println(current.getToken()+"\n|");
            printGameTree(current.getNext());
            return;
        }

        if (current.getBranchNode() == null && current.getClass() == BranchNode.class) {
            System.out.println("-" + current.getToken()); // "Branch closed"
            return;
        }

        if (current.getClass() == BranchNode.class || current.getBranchNode() != null) {
            if (current.getClass() == BranchNode.class) {
                System.out.print("-" + current.getToken());
            } else {
                System.out.print(current.getToken());
            }
            printGameTree(current.getBranchNode());
            if (current.getNext() != null) {
                printGameTree(current.getNext());
            }
        } else {
            if (current.getPrevious().getBranchNode() != null) {
                System.out.println("|");
            }
            if (current.getNext() != null) {
                System.out.println(current.getToken() + "\n|");
                printGameTree(current.getNext());
            } else {
                System.out.println(current.getToken());
            }
        }
    }
}
