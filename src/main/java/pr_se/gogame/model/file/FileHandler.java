package pr_se.gogame.model.file;

import pr_se.gogame.model.*;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static pr_se.gogame.model.file.SGFToken.*;
import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;

public class FileHandler {
    private static File currentFile;

    public static boolean saveFile(Game game, File file, GeraldsHistory history) {
        if(file == null) {
            throw new NullPointerException();
        }

        currentFile = file;

        history.rewind();

        try (FileWriter output = new FileWriter(file)) {

            GeraldsNode node;
            SGFToken t;

            try {
                output.write(String.format(START.getValue() + "\n\n", game.getSize()));
                output.write(String.format(HA.getValue() + "", game.getHandicap())); // Used to include an opening '(' at the end.

                history.stepForward();
                node = history.getCurrentNode();

                if(game.getHandicap() > 0) {
                    boolean handicapMode = false;

                    while(!history.isAtEnd() && node.getSaveToken() == GeraldsNode.AbstractSaveToken.HANDICAP) {
                        if (node.getColor() == BLACK) {
                            t = SGFToken.AB;
                        } else if (node.getColor() == WHITE) {
                            t = SGFToken.AW;
                        } else {
                            throw new IllegalStateException("AE token not supported!");
                        }

                        String outputFormatString;

                        if(handicapMode) {
                            outputFormatString = SGFToken.LONE_ATTRIBUTE.getValue();
                        } else {
                            handicapMode = true;
                            outputFormatString = t.getValue();
                        }

                        output.write(String.format(outputFormatString, calculateCoordinates(node.getX(), node.getY())));

                        history.stepForward();
                        node = history.getCurrentNode();
                    }
                }
            } catch(IOException e) {
                System.out.println("Couldn't write file header!");
                return false;
            }

            output.write("\n\n");

            try {
                for (;;) {
                    switch (node.getSaveToken()) {
                        case MOVE:
                            if(node.getColor() == BLACK) {
                                t = SGFToken.B;
                            } else {
                                t = SGFToken.W;
                            }

                            output.write(String.format("\n" + t.getValue(), calculateCoordinates(node.getX(), node.getY())));
                            break;

                        case PASS:
                            if(node.getColor() == BLACK) {
                                t = SGFToken.B;
                            } else {
                                t = SGFToken.W;
                            }

                            output.write("\n" + String.format(t.getValue(), "")); // Passing is done by having an empty move.
                            break;

                        case RESIGN:
                            break;

                        default:
                            break;
                    }

                    if(!node.getComment().equals("")) {
                        output.write(String.format(C.getValue(), node.getComment()));
                    }

                    if(history.isAtEnd()) {
                        break;
                    }

                    history.stepForward();
                    node = history.getCurrentNode();
                }

                output.write("\n\n)"); // Used to have a closing ')' at the beginning.
            } catch (IOException e) {
                System.out.println("File write Error");
                return false;
            }
        } catch(IOException e) {
            System.out.println("Couldn't open file output stream!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean loadFile(Game game, File file) {
        if(file == null) {
            throw new NullPointerException();
        }

        currentFile = file;

        try (FileReader input = new FileReader(file)) {
            SGFScanner scanner = new SGFScanner(input);

            ScannedToken t = scanner.next();

            System.out.println("Reading preparatory Token ...");

            if(t.getToken() != LPAR) {
                unexpected(LPAR.getValue(), t);
            }

            t = scanner.next();

            if(t.getToken() != SEMICOLON) {
                unexpected(SEMICOLON.getValue(), t);
            }

            int size = -1;

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
                        if(size < Game.MIN_CUSTOM_BOARD_SIZE || size > Game.MAX_CUSTOM_BOARD_SIZE) {
                            throw new IOException("Invalid size '" + size + "' in SGF file!");
                        }
                        break;

                    case LPAR:
                        throw new IOException("This program does not support multiple GameTrees!");

                    case HA:
                    case SEMICOLON:
                    case RPAR:
                        break loop;

                    default:
                        unexpected("Game info tokens", t);
                        break loop;
                }
            }
            int[] decodedCoords;
            String currentComment = null;

            if(t.getToken() == HA) {
                int handicap = Integer.parseInt(t.getAttributeValue());
                if(handicap < Game.MIN_HANDICAP_AMOUNT || handicap > Game.MAX_HANDICAP_AMOUNT) {
                    throw new IOException("Invalid handicap amount of " + handicap + "!");
                }
                game.newGame(BLACK, size, handicap, new JapaneseRuleset(), false); // This is to ensure that default handicap positions are still displayed, without stones being set yet.
                if(handicap > 0) {
                    game.setHandicapStoneCounter(handicap);

                    StoneColor handicapColor = null;

                    t = scanner.next();
                    /*
                     * TODO: At the moment, black always starts (which could lead to wrong results in the unimplemented
                     *  situation that white starts. Maybe we should remove relics of this idea, as it seems unlikely,
                     *  and even if a player wanted to start and play white, black could just manually pass for the
                     *  first move.
                     */
                    if(t.getToken() == AB) {
                        handicapColor = BLACK;
                        decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                        game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);

                    } else if(t.getToken() == AW) {
                        handicapColor = WHITE;
                        decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                        game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);
                    } else {
                        unexpected(AB.getValue() + " or " + AW.getValue(), t);
                    }

                    t = scanner.next();
                    while(t.getToken() == LONE_ATTRIBUTE) {
                        decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                        if(handicapColor == BLACK) {
                            game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);
                        } else {
                            game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);
                        }
                        t = scanner.next();
                    }
                }
            } else {
                game.newGame(BLACK, size, 0, new JapaneseRuleset(), false);
            }

            boolean moveWasMade = false;

            if(t.getToken() != RPAR) {
                StoneColor addStoneColor = null;

                loop2:
                for (;;) {
                    t = scanner.next();

                    switch (t.getToken()) {
                        case SEMICOLON:
                            if(currentComment != null) {
                                game.commentCurrentMove(currentComment);
                            }
                            currentComment = null;
                            moveWasMade = false;
                            break;

                        case AW:
                            addStoneColor = WHITE;
                            decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                            game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);
                            moveWasMade = true;
                            break;

                        case AB:
                            addStoneColor = BLACK;
                            decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                            game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);
                            moveWasMade = true;
                            break;

                        case LONE_ATTRIBUTE:
                            if (addStoneColor == null) {
                                throw new IOException("Stray lone attribute encountered at line " + t.getLine() + ", col " + t.getCol());
                            }
                            decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                            if(addStoneColor == BLACK) {
                                game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);
                            } else {
                                game.placeHandicapPosition(decodedCoords[0], decodedCoords[1], true);
                            }
                            break;

                        case B:
                            if (t.getAttributeValue().equals("")) {
                                game.pass();
                            } else {
                                decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                                game.playMove(decodedCoords[0], decodedCoords[1], BLACK);
                            }
                            moveWasMade = true;
                            break;

                        case W:
                            if (t.getAttributeValue().equals("")) {
                                game.pass();
                            } else {
                                decodedCoords = calculateGridCoordinates(t.getAttributeValue());
                                game.playMove(decodedCoords[0], decodedCoords[1], WHITE);
                            }
                            moveWasMade = true;
                            break;

                        case C:
                            if(moveWasMade) {
                                game.commentCurrentMove(t.getAttributeValue());
                            } else {
                                currentComment = t.getAttributeValue();
                            }
                            break;

                        case RPAR:
                            // t = scanner.next();
                            break loop2;

                        case LPAR:
                            throw new IOException("This SGF file has multiple branches, a feature currently unsupported by this program.");

                        default:
                            throw new IOException("Unsupported token \"" + t.getToken() + "\" read at line " + t.getLine() + ", col " + t.getCol());
                    }
                }
            }

            if(t.getToken() != RPAR) {
                unexpected(RPAR.getValue(), t);
            }

            t = scanner.next(); // Do this one last time so that we can check if we're at EOF.

            if(t.getToken() != EOF) {
                unexpected(EOF.getValue(), t);
            }

        } catch(IOException e) {
            System.out.println("Couldn't properly read SGF file!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void unexpected(String expected, ScannedToken actual) throws IOException {
        throw new IOException("Expected " + expected + " but parsed " + actual.getToken().getValue() + " on line " + actual.getLine() + ", col " + actual.getCol());
    }

    /**
     * Calculates the coordinates for the sgf File
     *
     * @param x column of Stone
     * @param y row of Stone
     * @return The x and y-axis in letter format
     */
    private static String calculateCoordinates(int x, int y) {
        return "" + (char) (x + 97) + (char) (97 + y);
    }

    private static int[] calculateGridCoordinates(String s) {
        return new int[]{s.charAt(0) - 97, s.charAt(1) - 97};
    }

    // Getters and setters

    public static File getCurrentFile() {
        return currentFile;
    }
}
