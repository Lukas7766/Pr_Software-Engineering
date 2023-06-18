package pr_se.gogame.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static pr_se.gogame.model.SgfToken.*;
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
                output.write(String.format(START.getValue() + "\n\n(", game.getSize()));
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
                                outputFormatString = SgfToken.LONE_ATTRIBUTE.getValue();
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

    public void loadFile(File file) {
        try (FileReader input = new FileReader(file)) {
            SGFScanner scanner = new SGFScanner(input);

            ScannedToken t;

            do {
                t = scanner.next();

                System.out.println("Reading preparatory Token ...");

                if(t.getToken() != LPAR) {
                    unexpected(LPAR.getValue(), t);
                }

                t = scanner.next();

                if(t.getToken() != SEMICOLON) {
                    unexpected(SEMICOLON.getValue(), t);
                }

                int size = -1;
                int handicap = -1;

                loop:
                for(;;) {
                    t = scanner.next();

                    switch (t.getToken()) {
                        case FF:
                            if (Integer.parseInt(t.getAttributeValue()) != 4) {
                                throw new IOException("Illegal SGF version! Must be 4 but was '" + t.getAttributeValue() + "'");
                            }
                            break;

                        case GM:
                            if (Integer.parseInt(t.getAttributeValue()) != 1) {
                                throw new IOException("SGF file is for wrong game! Must be 1 but is '" + t.getAttributeValue() + "'");
                            }
                            break;

                        case SZ:
                            size = Integer.parseInt(t.getAttributeValue());
                            break;

                        case HA:
                            handicap = Integer.parseInt(t.getAttributeValue());
                            break;

                        case LPAR:
                        case RPAR:
                            break loop;

                        default:
                            unexpected("Game info tokens", t);
                            break loop;
                    }
                }

                if(size < 0) {
                    throw new IOException("Invalid Size in SGF file!");
                }

                if(t.getToken() != LPAR && t.getToken() != RPAR) {
                    unexpected(LPAR.getValue() + " or " + RPAR.getValue(), t);
                }

                game.newGame(BLACK, size, Math.max(handicap, 0), new JapaneseRuleset());

                loop2:
                if(t.getToken() == LPAR) {
                    t = scanner.next();

                    StoneColor handicapMode = null;

                    for(;;) {
                        t = scanner.next();

                        int [] decodedCoords = null;

                        switch (t.getToken()) {
                            case SEMICOLON:
                                break;

                            case AW: // TODO: Maybe add boolean param to Game.newGame() so handicap stones aren't set twice. At the moment, the Ruleset is always asked to set the handicap stones.
                                handicapMode = WHITE;
                                decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                                break;

                            case AB:
                                handicapMode = BLACK;
                                decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                                break;

                            case LONE_ATTRIBUTE:
                                if(handicapMode == null) {
                                    throw new IOException("Stray lone attribute encountered at line " + t.getLine() + ", col " + t.getCol());
                                }
                                break;

                            case B: // TODO: Maybe check this earlier to determine who starts. At the moment, black always starts (which can lead to wrong results if there were handicap stones).
                                if(t.getAttributeValue().equals("")) {
                                    game.pass();
                                } else {
                                    decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                                    game.playMove(decodedCoords[0], decodedCoords[1]);
                                }
                                break;

                            case W:
                                if(t.getAttributeValue().equals("")) {
                                    game.pass();
                                } else {
                                    decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                                    game.playMove(decodedCoords[0], decodedCoords[1]);
                                }
                                break;

                            case RPAR:
                                t = scanner.next();
                                break loop2;

                            case LPAR:
                                throw new IOException("This SGF file has multiple branches, a feature currently unsupported by this program.");
                                // break loop2;

                            default:
                                throw new IOException("Unsupported token \"" + t.getToken() + "\" read at line " + t.getLine() + ", col " + t.getCol());
                                // break loop2;
                        }
                    }
                }

                if(t.getToken() != RPAR) {
                    unexpected(RPAR.getValue(), t);
                }

                t = scanner.next(); // Do this one last time so we can check if we're at EOF.
            } while(t.getToken() != EOF);
        } catch(IOException e) {
            System.out.println("Couldn't properly read SGF file!");
            e.printStackTrace();
        }
    }

    private void unexpected(String expected, ScannedToken actual) throws IOException {
        throw new IOException("Expected " + expected + " but parsed " + actual.getToken().getValue() + " on line " + actual.getLine() + ", col " + actual.getCol());
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
    private String calculateCoordinates(int x, int y) {
        return "" + (char) (x + 97) + (char) (97 + y);
    }

    private int[] calculateGridCoordinates(String s) {
        return new int[]{s.charAt(0) - 97, s.charAt(1) - 97};
    }
}
