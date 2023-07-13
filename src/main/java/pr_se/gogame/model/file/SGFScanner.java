package pr_se.gogame.model.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import static pr_se.gogame.model.file.SGFToken.*;

/**
 * Scanner for generating ScannedTokens from SGF files
 */
public class SGFScanner {
    /**
     * Convenience-variable for detecting the end of input
     */
    private static final char EOF = (char)-1;

    /**
     * The input (could be from a file, a String, etc.)
     */
    private final BufferedReader input;

    /**
     * The currently read character in the scanned file
     */
    private char ch;

    /**
     * The current column in the scanned file
     */
    private int col = 0;

    /**
     * The current line in the scanned file
     */
    private int line = 1;

    /**
     * Instantiates a new SGFScanner
     * @param input any form of textual input. Mostly files, but could also be a String (e.g., for testing purposes).
     */
    public SGFScanner(Reader input) {
        if(input == null) {
            throw new NullPointerException();
        }

        this.input = new BufferedReader(input);
    }

    /**
     * Scans the next ScannedToken from the input
     * @return the next ScannedToken in the input
     * @throws IOException if an IO error occurs with the input or an unexpected character is read.
     */
    public ScannedToken next() throws IOException {
        do {
            getNextChar();
        } while(Character.isWhitespace(ch));

        SGFToken t = null;
        String attribute = "";

        boolean hasAttribute = true;

        switch (ch) {
            case '[' -> {
                t = LONE_ATTRIBUTE;
                attribute = getAttribute();
                hasAttribute = false;
            }
            case ';' -> {
                t = SEMICOLON;
                hasAttribute = false;
            }
            case '(' -> {
                t = LPAR;
                hasAttribute = false;
            }
            case ')' -> {
                t = RPAR;
                hasAttribute = false;
            }
            case 'A' -> {
                getNextChar();
                if (ch == 'B') {
                    t = AB;
                } else if (ch == 'W') {
                    t = AW;
                } else if (ch == 'E') {
                    t = AE;
                } else {
                    unexpected('B', 'W', 'E');
                }
            }
            case 'B' -> t = B;
            case 'W' -> t = W;
            case 'C' -> {
                t = C;
                getNextChar();
                if (ch == 'R') {
                    t = CR;
                    getNextChar();
                }
                attribute = getAttribute();
                hasAttribute = false;
            }
            case 'F' -> {
                getNextChar();
                expect('F');
                t = FF;
            }
            case 'G' -> {
                getNextChar();
                expect('M');
                t = GM;
            }
            case 'H' -> {
                getNextChar();
                expect('A');
                t = HA;
            }
            case 'K' -> {
                getNextChar();
                expect('M');
                t = KM;
            }
            case 'M' -> {
                getNextChar();
                expect('A');
                t = MA;
            }
            case 'P' -> {
                getNextChar();
                if (ch == 'B') {
                    t = PB;
                } else if (ch == 'W') {
                    t = PW;
                } else {
                    unexpected('B', 'W');
                }
            }
            case 'S' -> {
                getNextChar();
                if (ch == 'Z') {
                    t = SZ;
                } else if (ch == 'Q') {
                    t = SQ;
                } else {
                    unexpected('Q', 'Z');
                }
            }
            case 'T' -> {
                getNextChar();
                expect('R');
                t = TR;
            }
            case (char) -1 -> {
                t = SGFToken.EOF;
                hasAttribute = false;
            }
            default -> throw new IOException("Invalid token '" + ch + "'!");
        }

        if(hasAttribute) {
            getNextChar();
            attribute = getAttribute();
        }

        return new ScannedToken(t, attribute, line, col);
    }

    /**
     * Checks if the current character is the expected one.
     * @param expected The expected character at this position in the SGF file
     * @throws IOException If the current character differs from the expected one
     */
    private void expect(char expected) throws IOException {
        if(ch != expected) {
            unexpected(expected);
        }
    }

    /**
     * Method for throwing an Exception if an unexpected character was scanned. Reduces code duplication
     * @param expected The character(s) that would have been expected here
     * @throws IOException always
     */
    private void unexpected(char ... expected) throws IOException {
        StringBuilder sb = new StringBuilder("'" + expected[0] + "'");
        for(int i = 1; i < expected.length; i++) {
            sb.append("or '").append(expected[i]).append("'");
        }
        throw new IOException("Expected " + sb + " but scanned '" + ch + "' on line " + line + ", col " + col);
    }

    /**
     * Scans the attribute string while doing some pre-conversions as per the most permissive value type "Text".
     * See <a href="https://www.red-bean.com/sgf/sgf4.html#text">...</a> for more information.
     * @return A pre-formatted attribute string
     * @throws IOException if reading from the file fails or an unexpected character is read
     */
    private String getAttribute() throws IOException {
        expect('[');
        getNextChar();
        StringBuilder attributeSB = new StringBuilder();
        while(ch != ']' && ch != EOF) {
            if(ch == '\\') {
                getNextChar();
                if(ch == '\n') {
                    getNextChar();
                }
            }
            if(Character.isWhitespace(ch) && ch != '\n') {
                ch = ' ';
            }
            attributeSB.append(ch);
            getNextChar();
        }
        expect(']');
        return attributeSB.toString();
    }

    /**
     * Fetches the next character from the input and updates the member variables of this Scanner accordingly.
     * @throws IOException if there is an IO error with the input
     */
    private void getNextChar() throws IOException {
        col++;
        ch = (char)input.read();
        while(ch == '\r') {
            ch = (char)input.read();
        }
        if(ch == '\n') {
            col = 0;
            line++;
        }
    }
}
