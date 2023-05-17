package pr_se.gogame.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 //TODO: Einbinden wenn Game mit x geschlossen wird
public class FileSaver {
    private String namePlayerOne;

    private String namePlayerTwo;

    private String filepath;

    private String buffer;

    private String format;

    private int count;

    private String file;

    private String version = "FF[4]";

    private String size;

    private String filetext = "(;%s%s%s%s%s";

    public static void main(String[] args) {
        //TODO Is it possible to write file while playing (because of the closing brackets)
        //importFile(Path.of("C:\\Users\\lukas\\Downloads\\test\\test.sgf"));
        /*FileSaver test = new FileSaver("Black","White","19");
        test.addStone("B",0,0);
        test.addStone("W",0,1);*/
        StartNode testStart = new StartNode(SgfToken.START,"FF[4]GM[1]SZ[9]PB[Black]PW[White]");
        FileTree test = new FileTree();
        Node test1 = new TreeNode(SgfToken.B,"bh");
        Node test2 = new TreeNode(SgfToken.B,"cg");
        //BranchNode test3 = new BranchNode(SgfToken.B,"cf");
        //BranchNode test4 = new BranchNode(SgfToken.B,"ch");
        Node test5 = new TreeNode(SgfToken.B,"dh");
        Node test6 = new TreeNode(SgfToken.B,"sa");
        testStart.setNext(test1);
        test1.setNext(test2);
        //test2.setBranchNode(test3);
        //test3.setBranchNode(test4);
        test2.setNext(test5);
        test5.setNext(test6);
        test.setStart(testStart);
        FileTree.printGameTree(testStart);
    }

    public FileSaver(String namePlayerOne, String namePlayerTwo, String size) {
        this.namePlayerOne = namePlayerOne;
        this.namePlayerTwo = namePlayerTwo;
        this.size = size;
        this.buffer = "(;FF[4]GM[1]SZ[" + size + "]PB[" + namePlayerOne + "]PW[" + namePlayerTwo + "]\n";
    }

    public void setNamePlayerOne(String namePlayerOne) {
        this.namePlayerOne = namePlayerOne;
    }

    public void setNamePlayerTwo(String getNamePlayerTwo) {
        this.namePlayerTwo = getNamePlayerTwo;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }


    public boolean addStone(String color, int x, int y) {
        //TODO: closing bracket
        //TODO: closing bracket when game gets closed with x
        //TODO: maybe a way of having the closing bracket in the string constantly
        if (this.count == 4) {
            buffer += color + "[" + calculateCoordinates(x, y,Integer.parseInt(size)) + "];\n";
            count = 0;
            System.out.println(buffer);
            return true;
        } else {
            buffer += color + "[" + calculateCoordinates(x, y,Integer.parseInt(size)) + "];";
            count++;
            System.out.println(buffer);
            return true;
        }
    }

    public boolean removeStone(int x, int y) {
        if (this.count == 3) {
            buffer += "AE[" + calculateCoordinates(x, y,Integer.parseInt(size)) + "];\n";
            count = 0;
            System.out.println(buffer);
            return true;
        } else {
            buffer += "AE[" + calculateCoordinates(x, y,Integer.parseInt(size)) + "];";
            count++;
            System.out.println(buffer);
            return true;
        }
    }


    //TODO: File Speichern (static evtl)
    public boolean saveFile(Path filepath) {
        try {
            Files.write(filepath, buffer.getBytes());
            return true;
        } catch (IOException e) {
            System.out.println("File write Error");
            return false;
        }
    }

    //TODO: File LADEN (static evtl)
    public static boolean importFile(Path filepath) {
        Board saveFileBoard;
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(filepath.toFile()));
            String line = reader.readLine();
            Pattern pattern = Pattern.compile("(?<=SZ\\[)[0-9]+");
            Matcher matcher = pattern.matcher(line);
            //saveFileBoard = new Board(Integer.parseInt(matcher.group()), StoneColor.BLACK);
            line = reader.readLine();
            String colC = "";
            int x;
            int y;
            StoneColor col;
            while (line != null) {
                System.out.println(line);
                for (String move : line.split(";")) {
                    x = move.charAt(2) - 97;
                    y = move.charAt(3) - 97;
                    col = String.valueOf(move.charAt(0)).equals("B") ? StoneColor.BLACK : StoneColor.WHITE;

                    //saveFileBoard.setStone(x, y, col,true);
                }

                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

     /**
      * Calculates the coordinates for the sgf File
      * @param x column of Stone
      * @param y row of Stone
      * @param size The board size
      * @return The x and y-axis in letter format
      */
    private static String calculateCoordinates(int x, int y,int size) {
        return (char) (x + 97) + String.valueOf((char) (96+(size) - y));
    }

     public String getNamePlayerOne() {
         return namePlayerOne;
     }

     public String getNamePlayerTwo() {
         return namePlayerTwo;
     }

     public String getFilepath() {
         return filepath;
     }


     //TODO thread mit rausschreiben und buffer
}
