package pr_se.gogame.model.file;

import pr_se.gogame.model.*;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;
import static pr_se.gogame.model.file.SGFToken.*;

public final class FileHandler {
    private static File currentFile;

    private FileHandler() {
        // This private constructor solely exists to prevent instantiation.
    }

    public static boolean saveFile(Game game, File file, History history) {
        if(file == null) {
            throw new NullPointerException();
        }

        currentFile = file;

        history.rewind();

        try (FileWriter output = new FileWriter(file)) {

            HistoryNode node;
            SGFToken t;

            // Write file header

            output.write(String.format(START.getValue(), game.getSize()));
            output.write( "\n\n");
            output.write(String.format(HA.getValue(), game.getHandicap()));

            history.stepForward();
            node = history.getCurrentNode();

            if(game.getHandicap() > 0) {
                if(node.getSaveToken() == HistoryNode.AbstractSaveToken.HANDICAP) {
                    if (node.getColor() == BLACK) {
                        t = SGFToken.AB;
                    } else if (node.getColor() == WHITE) {
                        t = SGFToken.AW;
                    } else {
                        throw new IllegalStateException("AE token not supported!");
                    }

                    output.write(String.format(t.getValue(), formStringFromCoords(node.getX(), node.getY())));

                    writeAttributeSequence(output, history, node);

                    history.stepForward();
                    node = history.getCurrentNode();
                } else {
                    if(game.getHandicap() > 1) {
                        throw new IllegalStateException("Handicap move expected but not found!");
                    }
                }
            }

            output.write("\n\n");

            // Write game contents

            for (;;) {
                output.write("\n");

                switch (node.getSaveToken()) {
                    case SETUP:
                        if(node.getColor() == BLACK) {
                            t = AB;
                        } else {
                            t = AW;
                        }

                        output.write(";");
                        output.write(String.format(t.getValue(), formStringFromCoords(node.getX(), node.getY())));

                        writeAttributeSequence(output, history, node);

                        break;

                    case MOVE, PASS:
                        if(node.getColor() == BLACK) {
                            t = B;
                        } else {
                            t = W;
                        }

                        String coords = node.getSaveToken() == HistoryNode.AbstractSaveToken.MOVE ? formStringFromCoords(node.getX(), node.getY()) : "";
                        output.write(String.format(t.getValue(), coords));

                        break;

                    case HANDICAP:
                        if(!history.isAtEnd()) {
                            throw new IOException("Can't save handicap after game has commenced!");
                        }
                        break;

                    default:
                        break;
                }

                for(Map.Entry<Position, MarkShape> e : node.getMarks().entrySet()) {
                    output.write(String.format(e.getValue().getSgfToken().getValue(), formStringFromCoords(e.getKey().x, e.getKey().y)));
                }

                if(!node.getComment().equals("")) {
                    String reformattedComment = node.getComment().replace("\\", "\\\\").replace("]", "\\]").replace(":", "\\:");
                    output.write(String.format(C.getValue(), reformattedComment));
                }

                if(history.isAtEnd()) {
                    break;
                }

                history.stepForward();
                node = history.getCurrentNode();
            }

            output.write("\n\n)");

        } catch(IOException e) {
            return false;
        }

        return true;
    }

    private static void writeAttributeSequence(FileWriter output, History history, HistoryNode parentNode) throws IOException {
        history.stepForward();
        while(history.getCurrentNode().getSaveToken() == parentNode.getSaveToken() && history.getCurrentNode().getColor() == parentNode.getColor()) {
            output.write(String.format(LONE_ATTRIBUTE.getValue(), formStringFromCoords(history.getCurrentNode().getX(), history.getCurrentNode().getY())));
            if(history.isAtEnd()) {
                break;
            }
            history.stepForward();
        }
        if(!history.isAtEnd()) {
            history.stepBack();
        }
    }

    public static boolean loadFile(Game game, File file) {
        if(file == null) {
            throw new NullPointerException();
        }

        currentFile = file;

        try (FileReader input = new FileReader(file)) {
            SGFScanner scanner = new SGFScanner(input);

            ScannedToken t = scanner.next();

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

                    case HA, SEMICOLON, RPAR:
                        break loop;

                    default:
                        unexpected("Game info tokens", t);
                        break loop;
                }
            }
            Position decodedCoords;
            String currentComment = null;
            int handicap = 0;

            if(t.getToken() == HA) {
                handicap = Integer.parseInt(t.getAttributeValue());
                if (handicap < Game.MIN_HANDICAP_AMOUNT || handicap > Game.MAX_HANDICAP_AMOUNT) {
                    throw new IOException("Invalid handicap amount of " + handicap + "!");
                }
            }

            game.newGame(BLACK, size, handicap, new JapaneseRuleset(), false); // This is to ensure that default handicap positions are still displayed, without stones being set yet.

            if(handicap > 0) {
                StoneColor handicapColor = null;

                t = scanner.next();

                if(t.getToken() == AB) {
                    handicapColor = BLACK;
                } else if(t.getToken() == AW) {
                    handicapColor = WHITE;
                } else if(handicap > 1){
                    unexpected(AB.getValue() + " or " + AW.getValue(), t);
                }

                if(handicapColor != null) {
                    game.setHandicapStoneCounter(handicap);
                    do {
                        decodedCoords = calculateCoordsFromString(t.getAttributeValue());
                        game.placeHandicapPosition(decodedCoords.x, decodedCoords.y, handicapColor, true);

                        t = scanner.next();
                    } while (t.getToken() == LONE_ATTRIBUTE);
                }
            }

            Map<Position, MarkShape> marks = new LinkedHashMap<>();

            if(t.getToken() != RPAR) {
                StoneColor addStoneColor = null;

                loop2:
                for (;;) {
                    t = scanner.next();

                    switch (t.getToken()) {
                        case SEMICOLON, RPAR:
                            if(currentComment != null) {
                                game.commentCurrentMove(currentComment);
                            }
                            currentComment = null;
                            marks.entrySet().forEach(e -> game.mark(e.getKey().x, e.getKey().y, e.getValue()));
                            marks = new LinkedHashMap<>();
                            if(t.getToken() == RPAR) {
                                break loop2;
                            }
                            break;

                        case AW:
                            addStoneColor = WHITE;
                            decodedCoords = calculateCoordsFromString(t.getAttributeValue());
                            game.placeSetupStone(decodedCoords.x, decodedCoords.y, addStoneColor);
                            break;

                        case AB:
                            addStoneColor = BLACK;
                            decodedCoords = calculateCoordsFromString(t.getAttributeValue());
                            game.placeSetupStone(decodedCoords.x, decodedCoords.y, addStoneColor);
                            break;

                        case LONE_ATTRIBUTE:
                            if (addStoneColor == null) {
                                throw new IOException("Stray lone attribute encountered at line " + t.getLine() + ", col " + t.getCol());
                            }
                            decodedCoords = calculateCoordsFromString(t.getAttributeValue());
                            game.placeSetupStone(decodedCoords.x, decodedCoords.y, addStoneColor);
                            break;

                        case B:
                            if (t.getAttributeValue().equals("")) {
                                game.pass();
                            } else {
                                decodedCoords = calculateCoordsFromString(t.getAttributeValue());
                                game.playMove(decodedCoords.x, decodedCoords.y, BLACK);
                            }
                            break;

                        case W:
                            if (t.getAttributeValue().equals("")) {
                                game.pass();
                            } else {
                                decodedCoords = calculateCoordsFromString(t.getAttributeValue());
                                game.playMove(decodedCoords.x, decodedCoords.y, WHITE);
                            }
                            break;

                        case C:
                            currentComment = t.getAttributeValue();
                            break;

                        case CR:
                            decodedCoords = calculateCoordsFromString(t.getAttributeValue());
                            marks.put(new Position(decodedCoords.x, decodedCoords.y), MarkShape.CIRCLE);
                            break;

                        case LPAR:
                            throw new IOException("Line " + t.getLine() + ", col " + t.getCol() + ": This SGF file has multiple branches, a feature currently unsupported by this program.");

                        default:
                            throw new IOException("Unsupported " + t);
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
            return false;
        }

        game.goToFirstMove();

        return true;
    }

    private static void unexpected(String expectedToken, ScannedToken actualToken) throws IOException {
        throw new IOException("Expected " + expectedToken + " but parsed " + actualToken);
    }

    /**
     * Calculates the coordinates for the sgf File
     *
     * @param x column of Stone
     * @param y row of Stone
     * @return The x and y-axis in letter format
     */
    private static String formStringFromCoords(int x, int y) {
        return "" + (char) (x + 97) + (char) (97 + y);
    }

    private static Position calculateCoordsFromString(String s) {
        return new Position(s.charAt(0) - 97, s.charAt(1) - 97);
    }

    // Getters and setters

    public static File getCurrentFile() {
        return currentFile;
    }

    public static void clearCurrentFile() {
        currentFile = null;
    }
}
