package pr_se.gogame.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;

public class FileHandler {

    String namePlayerBlack;

    String getNamePlayerWhite;

    // private final Map<Pattern, BiConsumer<Pattern, String>> patternToMethod = createPatternMap();

    private final Game game;

    public FileHandler(Game game) {
        this.game = game;
    }

    public boolean saveFile(File file, GeraldsHistory history) {
        history.rewind();

        try (FileWriter output = new FileWriter(file)) {

            try {
                output.write(String.format(SgfToken.START.getValue() + "\n\n(", game.getSize()));
            } catch(IOException e) {
                System.out.println("Couldn't write file header!");
            }

            try {
                boolean handicapMode = false;

                while (!history.isAtEnd()) {
                    history.stepForward();
                    GeraldsNode n = history.getCurrentNode();
                    SgfToken t;

                    switch (n.getSaveToken()) {
                        case HANDICAP:
                            if (n.getColor() == BLACK) {
                                t = SgfToken.AB;
                            } else if (n.getColor() == WHITE) {
                                t = SgfToken.AW;
                            } else {
                                throw new IllegalStateException("AE token not supported!");
                            }

                            String outputFormatString;

                            if(handicapMode) {
                                outputFormatString = "[%s]";
                            } else {
                                handicapMode = true;
                                outputFormatString = t.getValue();
                            }

                            output.write(String.format(outputFormatString, calculateCoordinates(n.getX(), n.getY())));
                            break;

                        case MOVE:
                            if(n.getColor() == BLACK) {
                                t = SgfToken.B;
                            } else {
                                t = SgfToken.W;
                            }

                            output.write(String.format("\n" + t.getValue(), calculateCoordinates(n.getX(), n.getY())));
                            break;

                        case PASS:
                            if(n.getColor() == BLACK) {
                                t = SgfToken.B;
                            } else {
                                t = SgfToken.W;
                            }

                            output.write("\n" + String.format(t.getValue(), "")); // Passing is done by having an empty move.
                            break;

                        case RESIGN:
                            break;

                        default:
                            break;
                    }
                }

                output.write(")\n\n)");
                return true;
            } catch (IOException e) {
                System.out.println("File write Error");
                return false;
            }
        } catch(IOException e) {
            System.out.println("Couldn't open file output stream!");
            e.printStackTrace();
        }

        return true;
    }

    /*public void loadFile(Path filepath) {
        String content;
        try {
            content = Files.readString(filepath);
            content = content.replaceAll("\\R", " ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String[] test = content.split(";");
        for (String s : test) {
            processString(s);
            if (this.boardSize != 0){
                this.tree = new FileTree(this.boardSize);
            }
        }
    }


    public Map<Pattern, BiConsumer<Pattern, String>> createPatternMap() {
        Map<Pattern, BiConsumer<Pattern, String>> map = new HashMap<>();
        map.put(Pattern.compile("B\\[\\w+\\]"), this::addStoneBlack);
        map.put(Pattern.compile("FF\\[4\\]GM\\[1\\]SZ\\[(.+?)\\].*PB\\[(.+?)\\].*PW\\[(.+?)\\]"), this::processStartOfFile);
        map.put(Pattern.compile("W\\[(\\w+)\\]"), this::addNameWhite);
        map.put(Pattern.compile("AE\\[\\w+\\]"), this::addEmpty);
        //map.put(Pattern.compile("AW(\\w+)"), FileHandler::method1);
        //map.put(Pattern.compile("AB(\\w+)"), FileHandler::method1);
        map.put(Pattern.compile("PB\\[(\\w+)\\]"), this::addNameBlack);
        map.put(Pattern.compile("PW\\[(\\w+)\\]"), this::addNameWhite);
        //map.put(Pattern.compile("KM\\[(\\w+)\\]"), FileHandler::method1);
        //map.put(Pattern.compile("C\\[(\\w+)\\]"), FileHandler::method1);
        map.put(Pattern.compile("HA\\[(\\w+)\\]"), this::addHandicap);
        //map.put(Pattern.compile("MA\\[(\\w+)\\]"), FileHandler::method1);
        //map.put(Pattern.compile("LB\\[(\\w+):(\\w+)\\]"), FileHandler::method1);

        return Collections.unmodifiableMap(map);
    }

    public void processString(String s) {
        for (Map.Entry<Pattern, BiConsumer<Pattern, String>> entry : patternToMethod.entrySet()) {
            Matcher m = entry.getKey().matcher(s);
            if (m.matches()) {
                entry.getValue().accept(entry.getKey(), s);//TODO doesnt detect start file ? why
                return;
            }
        }
    }

    private void processStartOfFile(Pattern p, String s) {
        Matcher m = p.matcher(s);
        if (m.matches()) {
            this.tree = new FileTree(Integer.parseInt(m.group(0)));
            tree.addName(BLACK, m.group(1));
            tree.addName(WHITE, m.group(2));
        }
    }


    public int[] calculateGridCoordinates(String s) {
        return new int[]{s.charAt(0) - 97, this.boardSize - ((int) s.charAt(1) - 96)};
    }

    public void setMoves(Game game){
        Node start = tree.getStart();
        while (start != null){
            if (start.getToken().startsWith(";W")){
                int[] coords = tree.getGridCoords(start);
                game.playMove(coords[0],coords[1]);
            }
            if (start.getToken().startsWith(";B")){
                int[] coords = tree.getGridCoords(start);
                game.playMove(coords[0],coords[1]);
            }
            start = start.getNext();
        }
    }*/

    /**
     * Calculates the coordinates for the sgf File
     *
     * @param x column of Stone
     * @param y row of Stone
     * @return The x and y-axis in letter format
     */
    public String calculateCoordinates(int x, int y) {
        return "" + (char) (x + 97) + (char) (97 + y);
    }

}
