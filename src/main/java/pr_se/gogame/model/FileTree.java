package pr_se.gogame.model;

import java.util.ArrayList;

public class FileTree {
    private final StartNode start;
    private Node current;

    private Node viewing;

    private final int boardSize;

    private int count;

    public FileTree(int boardSize, String namePlayerBlack, String namePlayerWhite) {
        this.start = new StartNode(boardSize);
        this.current = start;
        current.setPrevious(start);
        this.boardSize = boardSize;
        this.viewing = current;
    }


    public void addNode(SgfToken token, String value) {
        TreeNode newNode = new TreeNode(token, value);
        current.setNext(newNode);
        newNode.setPrevious(current);
        current = newNode;
        viewing = current;
    }

    public void addMove(SgfToken moveType, int xCoord, int yCoord) {
        addNode(moveType, calculateCoordinates(xCoord, yCoord));
    }


    public void addComment(String comment) {
        addNode(SgfToken.C, comment);
    }

    public void addLabelForCoordinate(String label) {
        if (label.length() > 2) {
            addNode(SgfToken.LB, label.substring(0, 2));
        } else {
            addNode(SgfToken.LB, label);
        }
    }

    public void markACoordinate(int xCoord, int yCoord) {
        addNode(SgfToken.MA, calculateCoordinates(xCoord, yCoord));
    }


    public void addName(StoneColor playerColor, String name) {
        switch (playerColor) {
            case BLACK -> addNode(SgfToken.PB, name);
            case WHITE -> addNode(SgfToken.PW, name);
            default -> throw new IllegalArgumentException("Didnt use a correct player color");
        }
    }

    public void addKomi(int komi) {
        addNode(SgfToken.KM, String.valueOf(komi));
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
        Node temp = start;
        int count = 0;
        while (temp != null) {
            if (temp.getToken().equals(start.getToken())) {
                sb.append(temp.getToken());
                sb.append("\n");
                temp = temp.getNext();
                continue;
            }//TODO: Format correctly
            if (temp.toString().startsWith(";")) {
                count++;
            }
            if (count == 2) {
                sb.append(temp.getToken());
                sb.append(System.lineSeparator());
                count = 0;
            } else {
                sb.append(temp.getToken());
            }
            temp = temp.getNext();
        }
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
