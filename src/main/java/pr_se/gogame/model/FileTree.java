package pr_se.gogame.model;

import java.util.ArrayList;

public class FileTree {
    /**
     * The start Node of a File tree
     */
    private final StartNode start;
    /**
     * The current node in the tree
     */
    private Node current;

    /**
     * The view in the Tree
     */
    private Node viewing;

    /**
     * The board size of the game board
     */
    private final int boardSize;

    /**
     * The constructor for a File tree
     * @param boardSize The size of the game board
     */
    public FileTree(int boardSize) {
        this.start = new StartNode(boardSize);
        this.current = start;
        current.setPrevious(start);
        this.boardSize = boardSize;
        this.viewing = current;
    }


    /**
     * Adds a Node into the File tree
     * @param token The move that is being saved
     * @param value the coordinates or the text of the Node
     */
    private void addNode(SgfToken token, String value) {
        TreeNode newNode = new TreeNode(token, value);
        current.setNext(newNode);
        newNode.setPrevious(current);
        current = newNode;
        viewing = current;
    }

    /**
     * Adds a move into the File tree
     * @param moveType The token for the move kind
     * @param xCoord the x coordinate for the move
     * @param yCoord the y coordinate for the move
     */
    public void addMove(SgfToken moveType, int xCoord, int yCoord) {
        addNode(moveType, calculateCoordinates(xCoord, yCoord));
    }

    /**
     * adds a stone into the tree
     * @param color The stone color
     * @param xCoord the x coordinate for the move
     * @param yCoord the y coordinate for the move
     */
    public void addStone (StoneColor color, int xCoord, int yCoord){
        switch (color){
            case WHITE -> addMove(SgfToken.W,xCoord,yCoord);
            case BLACK -> addMove(SgfToken.B,xCoord,yCoord);
        }
    }

    /**
     * Adds a comment into the file tree
     * @param comment The comment to add as String
     */
    public void addComment(String comment) {
        addNode(SgfToken.C, comment);
    }

    /**
     * Adds a Label for a coordinate
     * Only the first three letters will be used
     * if the Label ist too long
     * @param label The laben for the coordinate
     */
    public void addLabelForCoordinate(String label) {
        //TODO: which coordinate does this access ? research!
        if (label.length() > 2) {
            addNode(SgfToken.LB, label.substring(0, 2));
        } else {
            addNode(SgfToken.LB, label);
        }
    }

    /**
     * Marks a specific Coordinate
     * @param xCoord the x coordinate for the mark
     * @param yCoord the y coordinate for the mark
     */
    public void markACoordinate(int xCoord, int yCoord) {
        addNode(SgfToken.MA, calculateCoordinates(xCoord, yCoord));
    }


    /**
     * Adds a Name for a player, determined by the stone color
     * @param playerColor the color for the player
     * @param name the name for the player
     */
    public void addName(StoneColor playerColor, String name) {
        switch (playerColor) {
            case BLACK -> addNode(SgfToken.PB, name);
            case WHITE -> addNode(SgfToken.PW, name);
            default -> throw new IllegalArgumentException("Didnt use a correct player color");
        }
    }

    /**
     * Removes a stone by inserting an AE stone on the given coordinate
     * @param xCoord the x coordinate
     * @param yCoord the y coordinate
     */
    public void removeStone(int xCoord, int yCoord){
        addMove(SgfToken.AE,xCoord,yCoord);
    }

    /**
     * Adds the Komi for the game
     * @param komi the komi to be set
     */
    public void addKomi(int komi) {
        addNode(SgfToken.KM, String.valueOf(komi));
    }

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

    public static String calculateCoordinate(int boardSize, int x, int y) {
        return (char) (x + 97) + String.valueOf((char) (96 + (boardSize) - y));
    }

    public void addStonesBeforeGame(ArrayList<String> coordinatesWhite, ArrayList<String> coordinatesBlack) {
        StringBuilder bufferWhite = new StringBuilder();
        StringBuilder bufferBlack = new StringBuilder();
        if (coordinatesWhite.isEmpty()) {
            for (String s : coordinatesBlack) {
                bufferBlack.append("[").append(s).append("]");
            }
            current.setNext(new TreeNode(SgfToken.ABF, bufferBlack.toString()));
        } else {
            for (String s : coordinatesBlack) {
                bufferBlack.append("[").append(s).append("]");
            }
            for (String s :
                    coordinatesWhite) {
                bufferWhite.append("[").append(s).append("]");
            }
            addNode(SgfToken.AWF, bufferWhite.toString());
            addNode(SgfToken.AB, bufferBlack.toString());
        }
    }

    //TODO: add a stonesbeforegamebuffered which saves into two variables and then use a different to call the addstones before game to save them

    /**
     * Prints the current game Tree
     */
    public void printGameTree() {
        Node start = this.start;
        while (start != null) {
            if (start.getClass() == StartNode.class) {
                System.out.println(start);
            } else {
                System.out.println("|\n" + start);
            }
            start = start.getNext();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node temp = start.getNext();
        sb.append(start);
        sb.append(System.lineSeparator());
        int count = 0;
        while (temp != null) {
            if (!temp.getToken().startsWith(";")) {
                sb.append(temp);
                sb.append(System.lineSeparator());
                temp = temp.getNext();
            } else {
                if (count == 4) {
                    if (temp.getNext() == null){
                        sb.append(System.lineSeparator());
                        sb.append(temp);
                        temp = temp.getNext();
                    }else {
                        sb.append(temp);
                        sb.append(System.lineSeparator());
                        count = 0;
                        temp = temp.getNext();
                    }
                } else {
                    sb.append(temp);
                    count++;
                    temp=temp.getNext();
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public Node getCurrent() {
        return current;
    }

    public StartNode getStart() {
        return start;
    }

    //Methods vor viewing

    public Node viewCurrent() {
        return this.viewing;
    }

    public Node viewNext() {
        this.viewing = viewing.getNext();
        return viewing;
    }

    public Node viewPrev() {
        this.viewing = viewing.getPrevious();
        return viewing;
    }
}
