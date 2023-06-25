package pr_se.gogame.model.file;

import pr_se.gogame.model.*;
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.helper.Position;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;
import static pr_se.gogame.model.file.SGFToken.*;

public final class FileHandler {
    private static File currentFile;

    private FileHandler() {
        // This private constructor solely exists to prevent instantiation.
    }

    public static boolean saveFile(Game game, File file) {
        if(game == null || file == null) {
            throw new NullPointerException();
        }

        currentFile = file;

        Iterator<HistoryNode> iter = game.getHistory().iterator();

        try (FileWriter output = new FileWriter(file)) {
            // Write file header

            output.write(String.format(START.getValue(), game.getSize()));
            output.write( "\n\n");
            output.write(String.format(HA.getValue(), game.getHandicap()));

            HistoryNode node = null;
            if(iter.hasNext()) {
                node = iter.next();
            }
            SGFToken t;

            if(game.getHandicap() > 0) {
                if(node != null && node.getSaveToken() == HistoryNode.AbstractSaveToken.HANDICAP) {
                    t = SGFToken.ofHistoryNode(node);

                    if(t == null) {
                        throw new IOException("Can't get SGF token for node " + node);
                    }
                    output.write(String.format(t.getValue(), getStringFromCoords(node.getX(), node.getY())));
                    node = iter.next();

                    node = writeAttributeSequence(output, iter, node);
                    /*
                     * Assertion: node is either the first node after the attributeSequence or the last node in the
                     * History.
                     */
                } else if(game.getHandicap() > 1) {
                    throw new IllegalStateException("Handicap move expected but not found!");
                }
            }

            if(!iter.hasNext() && (node == null || node.getSaveToken() == HistoryNode.AbstractSaveToken.HANDICAP)) {
                if(node != null) {
                    writeNodeMetaData(output, node);
                }
                output.write("\n\n)");
                return true;
            }

            output.write("\n");

            String coords;

            // Write game contents
            for (;;) {
                if(node == null) {
                    throw new IllegalStateException("Reached end of history while saving!");
                }

                if(node.getSaveToken() == HistoryNode.AbstractSaveToken.HANDICAP) {
                    throw new IOException("Can't save handicap after game has commenced!");
                } else {
                    t = SGFToken.ofHistoryNode(node);
                    if (t == AE || t == null) {
                        throw new IllegalStateException(node.getSaveToken() + " with color " + node.getColor() + " not supported!");
                    }
                    coords = node.getSaveToken() == HistoryNode.AbstractSaveToken.PASS ? "" : getStringFromCoords(node.getX(), node.getY());
                }

                output.write("\n" + SEMICOLON.getValue());
                output.write(String.format(t.getValue(), coords));

                if(iter.hasNext()) {
                    node = iter.next();
                    if(t == SGFToken.ofHistoryNode(node) && t.hasMultiAttribs()) {
                        node = writeAttributeSequence(output, iter, node);
                    }

                }

                if(iter.hasNext() || t != SGFToken.ofHistoryNode(node)) {
                    writeNodeMetaData(output, node.getPrev());
                } else {
                    writeNodeMetaData(output, node);
                    break;
                }
            }


            output.write("\n\n)");

        } catch(IOException e) {
            return false;
        }

        return true;
    }

    private static void writeNodeMetaData(FileWriter output, HistoryNode node) throws IOException {
        for (Map.Entry<Position, MarkShape> e : node.getMarks().entrySet()) {
            output.write(String.format(e.getValue().getSgfToken().getValue(), getStringFromCoords(e.getKey().getX(), e.getKey().getY())));
        }

        if (!node.getComment().equals("")) {
            String reformattedComment = node.getComment().replace("\\", "\\\\").replace("]", "\\]").replace(":", "\\:");
            output.write(String.format(C.getValue(), reformattedComment));
        }
    }

    /**
     * @param output     the FileWriter that is to be written to
     * @param iter       the Iterator over the History
     * @param parentNode the parent Node of this attribute sequence
     * @return the first node of a different AbstractSaveToken or color, or the last node in the History.
     * @throws IOException if the FileWriter cannot be written to
     */
    private static HistoryNode writeAttributeSequence(FileWriter output, Iterator<HistoryNode> iter, HistoryNode parentNode) throws IOException {
        if(output == null || parentNode == null || iter == null) {
            throw new NullPointerException();
        }

        HistoryNode n = parentNode;

        do {
            output.write(String.format(LONE_ATTRIBUTE.getValue(), getStringFromCoords(n.getX(), n.getY())));
            if(iter.hasNext()) {
                n = iter.next();
            } else {
                break;
            }
        } while(n.getSaveToken() == parentNode.getSaveToken() && n.getColor() == parentNode.getColor());

        return n;
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

            final Map<SGFToken, StoneColor> correspondingColors = new EnumMap<>(SGFToken.class);
            correspondingColors.put(AW, WHITE);
            correspondingColors.put(W, WHITE);
            correspondingColors.put(AB, BLACK);
            correspondingColors.put(B, BLACK);

            Position decodedCoords;
            String currentComment = null;
            int handicap = 0;

            if(t.getToken() == HA) {
                handicap = Integer.parseInt(t.getAttributeValue());
                if (handicap < Game.MIN_HANDICAP_AMOUNT || handicap > Game.MAX_HANDICAP_AMOUNT) {
                    throw new IOException("Invalid handicap amount of " + handicap + "!");
                }
                t = scanner.next();
            }

            game.newGame(BLACK, size, handicap, new JapaneseRuleset(), false); // This is to ensure that default handicap positions are still displayed, without stones being set yet.

            if(handicap > 0) {
                StoneColor handicapColor = null;

                if(t.getToken() == AB || t.getToken() == AW) {
                    handicapColor = correspondingColors.get(t.getToken());
                } else if(handicap > 1) {
                    unexpected(AB.getValue() + " or " + AW.getValue(), t);
                }

                game.setHandicapStoneCounter(handicap);
                do {
                    decodedCoords = getCoordsFromString(t.getAttributeValue());
                    game.placeHandicapPosition(decodedCoords.getX(), decodedCoords.getY(), true, handicapColor);

                    t = scanner.next();
                } while (t.getToken() == LONE_ATTRIBUTE);

            }

            Map<Position, MarkShape> marks = new LinkedHashMap<>();

            if(t.getToken() != RPAR) {
                StoneColor addStoneColor = null;

                loop2:
                for (;;) {
                    switch (t.getToken()) {
                        case SEMICOLON, RPAR:
                            if(currentComment != null) {
                                game.setComment(currentComment);
                            }
                            currentComment = null;
                            marks.forEach((key, value) -> game.mark(key.getX(), key.getY(), value));
                            marks = new LinkedHashMap<>();
                            if(t.getToken() == RPAR) {
                                break loop2;
                            }
                            break;

                        case AB, AW:
                            addStoneColor = correspondingColors.get(t.getToken());
                            decodedCoords = getCoordsFromString(t.getAttributeValue());
                            game.setSetupMode(true);
                            game.placeSetupStone(decodedCoords.getX(), decodedCoords.getY(), addStoneColor);
                            break;

                        case LONE_ATTRIBUTE:
                            if (addStoneColor == null) {
                                throw new IOException("Stray lone attribute encountered at line " + t.getLine() + ", col " + t.getCol());
                            }
                            decodedCoords = getCoordsFromString(t.getAttributeValue());
                            game.placeSetupStone(decodedCoords.getX(), decodedCoords.getY(), addStoneColor);
                            break;

                        case B, W:
                            StoneColor c = correspondingColors.get(t.getToken());
                            game.setSetupMode(false);
                            if (t.getAttributeValue().equals("")) {
                                game.pass();
                            } else {
                                decodedCoords = getCoordsFromString(t.getAttributeValue());
                                game.playMove(decodedCoords.getX(), decodedCoords.getY(), c);
                            }
                            break;

                        case C:
                            currentComment = t.getAttributeValue();
                            break;

                        case CR:
                            decodedCoords = getCoordsFromString(t.getAttributeValue());
                            marks.put(new Position(decodedCoords.getX(), decodedCoords.getY()), MarkShape.CIRCLE);
                            break;

                        case LPAR:
                            throw new IOException("Line " + t.getLine() + ", col " + t.getCol() + ": This SGF file has multiple branches, a feature currently unsupported by this program.");

                        default:
                            throw new IOException("Unsupported " + t);
                    }

                    t = scanner.next();
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
    private static String getStringFromCoords(int x, int y) {
        return "" + (char) (x + 97) + (char) (97 + y);
    }

    private static Position getCoordsFromString(String s) {
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
