package pr_se.gogame.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class FileTree {
    private final StartNode start;
    private TreeNode current;

    private Node viewing;

    private final int boardSize;

    private int count;


    //private Node currentBranch;

    public FileTree(int boardSize, String namePlayerBlack, String namePlayerWhite) {
        this.start = new StartNode(boardSize);
        this.boardSize = boardSize;

    }



    public StartNode getStart() {
        return start;
    }

    //TODO: closing bracket
    //TODO: closing bracket when game gets closed with x
    //TODO: maybe a way of having the closing bracket in the string constantly
    //TODO: add helper methods for the others i.e. inserting white etc
    public void addMove(SgfToken moveType, int xCoord, int yCoord) {
        if (current==null){
            this.current = new TreeNode(moveType, calculateCoordinates(xCoord, yCoord));
            current.setPrevious(start);
            start.setNext(current);
            viewing = current;
        }else {
            TreeNode newNode = new TreeNode(moveType, calculateCoordinates(xCoord, yCoord));
            current.setNext(newNode);
            current = newNode;
            viewing = current;
        }
    }

    public void addComment(String comment){
        TreeNode newNode = new TreeNode(SgfToken.C, comment);
        current.setNext(newNode);
        current = newNode;
        viewing = current;
    }

    public void addName(StoneColor playerColor,String name){
        TreeNode newNode;
        switch (playerColor){
            case BLACK -> newNode = new TreeNode(SgfToken.PB, name);
            case WHITE -> newNode = new TreeNode(SgfToken.PW, name);
            default -> throw new IllegalArgumentException("Didnt use a correct player color");
        }

        current.setNext(newNode);
        current = newNode;
        viewing = current;
    }

    public void addKomi(int komi){
        TreeNode newNode = new TreeNode(SgfToken.KM, String.valueOf(komi));
        current.setNext(newNode);
        current = newNode;
        viewing = current;
    }

    /*public static void printGameTree(Node current) {
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
    }*/

    /**
     * Calculates the coordinates for the sgf File
     *
     * @param x column of Stone
     * @param y row of Stone
     * @return The x and y-axis in letter format
     */
    private String calculateCoordinates(int x, int y) {
        return (char) (x + 97) + String.valueOf((char) (96 + (this.boardSize) - y));
    }

    private static String calculateCoordinate(int boardSize, int x, int y) {
        return (char) (x + 97) + String.valueOf((char) (96 + (boardSize) - y));
    }

    //TODO:Test
    public void addStonesBeforeGame(ArrayList<String> coordinatesWhite, ArrayList<String> coordinatesBlack) {
        String bufferWhite = "";
        String bufferBlack = "";
        if (coordinatesWhite.isEmpty()) {
            for (String s : coordinatesBlack) {
                bufferBlack += "[" + s + "]";
            }
            current.setNext(new TreeNode(SgfToken.ABF, bufferBlack));
        } else {
            for (String s : coordinatesBlack) {
                bufferBlack += "[" + s + "]";
            }
            for (String s :
                    coordinatesWhite) {
                bufferWhite += "[" + s + "]";
            }
            current.setNext(new TreeNode(SgfToken.AWF, bufferWhite));
            current.setNext(new TreeNode(SgfToken.AB,bufferBlack));
        }
    }

    /**
     * Prints the current game Tree
     */
    public void printGameTree() {
        Node start = this.start;
        while (start != null){
            if (start.getClass() == StartNode.class){
                System.out.println(start);
                start = start.getNext();
            }else {
                System.out.println("|\n"+start);
                start = start.getNext();
            }
        }
    }

    public TreeNode getCurrent() {
        return current;
    }

    //Methods vor viewing

    public Node viewCurrent(){
        return this.viewing;
    }
    public Node viewNext(){
        this.viewing = viewing.getNext();
        return viewing;
    }

    public Node viewPrev(){
        this.viewing = viewing.getPrevious();
        return viewing;
    }
}
