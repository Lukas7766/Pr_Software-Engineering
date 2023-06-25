package pr_se.gogame.model.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import static pr_se.gogame.model.file.SGFToken.*;

public class SGFScanner {
    private static final char EOF = (char)-1;

    private final BufferedReader input;

    private char ch;

    private int col = 0;
    private int line = 1;

    public SGFScanner(Reader input) {
        if(input == null) {
            throw new NullPointerException();
        }

        this.input = new BufferedReader(input);
    }

    public ScannedToken next() throws IOException {
        do {
            getNextChar();
        } while(Character.isWhitespace(ch));

        SGFToken t = null;
        String attribute = "";

        boolean hasAttribute = true;

        switch (ch) {
            case '[':
                t = LONE_ATTRIBUTE;
                attribute = getAttribute();
                hasAttribute = false;
                break;

            case ';':
                t = SEMICOLON;
                hasAttribute = false;
                break;

            case '(':
                t = LPAR;
                hasAttribute = false;
                break;

            case ')':
                t = RPAR;
                hasAttribute = false;
                break;

            case 'A':
                getNextChar();
                if(ch == 'B') {
                    t = AB;
                } else if (ch == 'W') {
                    t = AW;
                } else if(ch == 'E') {
                    t = AE;
                } else {
                    unexpected('B', 'W', 'E');
                }
                break;

            case 'B':
                t = B;
                break;

            case 'W':
                t = W;
                break;

            case 'C':
                t = C;
                getNextChar();
                if(ch == 'R') {
                    t = CR;
                    getNextChar();
                }
                attribute = getAttribute();
                hasAttribute = false;
                break;

            case 'F':
                getNextChar();
                expect('F');
                t = FF;
                break;

            case 'G':
                getNextChar();
                expect('M');
                t = GM;
                break;

            case 'H':
                getNextChar();
                expect('A');
                t = HA;
                break;

            case 'K':
                getNextChar();
                expect('M');
                t = KM;
                break;

            case 'M':
                getNextChar();
                expect('A');
                t = MA;
                break;

            case 'P':
                getNextChar();
                if(ch == 'B') {
                    t = PB;
                } else if(ch == 'W') {
                    t = PW;
                } else {
                    unexpected('B', 'W');
                }
                break;

            case 'S':
                getNextChar();
                if(ch == 'Z') {
                    t = SZ;
                } else if(ch == 'Q') {
                    t = SQ;
                } else {
                    unexpected('Q', 'Z');
                }
                break;

            case 'T':
                getNextChar();
                expect('R');
                t = TR;
                break;

            case (char)-1:
                t = SGFToken.EOF;
                hasAttribute = false;
                break;

            default:
                throw new IOException("Invalid token '" + ch + "'!");
        }

        if(hasAttribute) {
            getNextChar();
            attribute = getAttribute();
        }

        return new ScannedToken(t, attribute, line, col);
    }

    private void expect(char expected) throws IOException {
        if(ch != expected) {
            unexpected(expected);
        }
    }

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
