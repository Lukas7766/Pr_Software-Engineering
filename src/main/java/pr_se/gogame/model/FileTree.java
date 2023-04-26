package pr_se.gogame.model;

import java.util.LinkedList;

public class FileTree {
    //TODO: write the file Tree class
    private StartNode start;

    public StartNode getStart() {
        return start;
    }

    public void setStart(StartNode start) {
        this.start = start;
    }

    //TODO: printing for visualization

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
