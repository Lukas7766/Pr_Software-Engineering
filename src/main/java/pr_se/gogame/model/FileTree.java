package pr_se.gogame.model;

import java.io.File;
import java.nio.file.Path;
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
     * Buffer for the white Handicap stones
     */
    private ArrayList<String> whiteBuffer = new ArrayList<>();

    /**
     * Buffer for the black HandicapStones
     */
    private ArrayList<String> blackBuffer = new ArrayList<>();

    private FileHandler fileHandler = new FileHandler();

    /**
     * The constructor for a File tree
     *
     * @param boardSize The size of the game board
     */
    public FileTree(int boardSize) {
        this.start = new StartNode(boardSize);
        this.current = start;
        current.setPrevious(start);
        this.boardSize = boardSize;
        this.viewing = current;
    }


    public FileTree(int boardSize,String namePlayerBlack,String namePlayerWhite) {
        this(boardSize);
        addName(StoneColor.BLACK,namePlayerBlack);
        addName(StoneColor.WHITE,namePlayerWhite);
    }

    /**
     * Adds a Node into the File tree
     *
     * @param token       The move that is being saved
     * @param coordinates the coordinates or the text of the Node
     */
    private void addNode(SgfToken token, String coordinates) {
        TreeNode newNode = new TreeNode(token, coordinates);
        current.setNext(newNode);
        newNode.setPrevious(current);
        current = newNode;
        viewing = current;
    }

    /**
     * Adds a Node into the File tree
     *
     * @param token       The move that is being saved
     * @param coordinates the coordinates or the text of the Node
     */
    private void addNode(SgfToken token, String coordinates, String letters) {
        TreeNode newNode = new TreeNode(token, coordinates, letters);
        current.setNext(newNode);
        newNode.setPrevious(current);
        current = newNode;
        viewing = current;//TODO DONT CHANGE VIEWING IF VIEWING ISNT CURRENT
    }

    /**
     * Adds a move into the File tree
     *
     * @param moveType The token for the move kind
     * @param xCoord   the x coordinate for the move
     * @param yCoord   the y coordinate for the move
     */
    public void addMove(SgfToken moveType, int xCoord, int yCoord) {
        addNode(moveType, calculateCoordinates(xCoord, yCoord));
    }

    /**
     * adds a stone into the tree
     *
     * @param color  The stone color
     * @param xCoord the x coordinate for the move
     * @param yCoord the y coordinate for the move
     */
    public void addStone(StoneColor color, int xCoord, int yCoord) {
        switch (color) {
            case WHITE -> addMove(SgfToken.W, xCoord, yCoord);
            case BLACK -> addMove(SgfToken.B, xCoord, yCoord);
        }
    }

    /**
     * Adds a comment into the file tree
     *
     * @param comment The comment to add as String
     */
    public void addComment(String comment) {
        addNode(SgfToken.C, comment);
    }

    /**
     * Adds a Label for a coordinate
     * Only the first three letters will be used
     * if the Label ist too long
     *
     * @param label The laben for the coordinate
     */
    public void addLabelForCoordinate(String label, int xCoord, int yCoord) {
        if (label.length() > 2) {
            addNode(SgfToken.LB, label.substring(0, 2));
        } else {
            addNode(SgfToken.LB, label);
        }
    }

    /**
     * Marks a specific Coordinate
     *
     * @param xCoord the x coordinate for the mark
     * @param yCoord the y coordinate for the mark
     */
    public void markACoordinate(int xCoord, int yCoord) {
        addNode(SgfToken.MA, calculateCoordinates(xCoord, yCoord));
    }


    /**
     * Adds a Name for a player, determined by the stone color
     *
     * @param playerColor the color for the player
     * @param name        the name for the player
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
     *
     * @param xCoord the x coordinate
     * @param yCoord the y coordinate
     */
    public void removeStone(int xCoord, int yCoord) {
        addMove(SgfToken.AE, xCoord, yCoord);
    }

    /**
     * Adds the Komi for the game
     *
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
    public String calculateCoordinates(int x, int y) {
        return (char) (x + 97) + String.valueOf((char) (96 + (this.boardSize) - y));
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

    public void bufferStonesBeforeGame(StoneColor color, int xCoord, int yCoord) {
        switch (color) {
            case BLACK -> blackBuffer.add(calculateCoordinates(xCoord, yCoord));
            case WHITE -> whiteBuffer.add(calculateCoordinates(xCoord, yCoord));
        }
    }

    public void insertBufferedStonesBeforeGame() {
        addStonesBeforeGame(whiteBuffer, blackBuffer);
    }

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
                    if (temp.getNext() == null) {
                        sb.append(System.lineSeparator());
                        sb.append(temp);
                        temp = temp.getNext();
                    } else {
                        sb.append(temp);
                        sb.append(System.lineSeparator());
                        count = 0;
                        temp = temp.getNext();
                    }
                } else {
                    sb.append(temp);
                    count++;
                    temp = temp.getNext();
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

    public boolean saveFile(Path path){
        return fileHandler.saveFile(path,toString());
    }

    public int[] getGridCoords(Node node){
        return calculateGridCoordinates(node.getToken());
    }

    public int[] calculateGridCoordinates(String s) {
        char x = s.charAt(0);
        char y = s.charAt(1);
        return new int[]{x - 97, this.boardSize - ((int) s.charAt(1) - 96)};
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
