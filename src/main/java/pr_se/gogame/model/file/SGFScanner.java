package pr_se.gogame.model.file;

import java.io.FileReader;
import java.io.IOException;

import static pr_se.gogame.model.file.SGFToken.*;

public class SGFScanner {
    private static final char eof = (char)-1;

    private final FileReader input;

    private char ch;

    private int col = 0, line = 1;

    public SGFScanner(FileReader input) {
        this.input = input;
    }

    public ScannedToken next() throws IOException {
        do {
            getNextChar();
        } while(Character.isWhitespace(ch));

        SGFToken t = null;
        String attribute = "";

        switch (ch) {
            case '[':
                t = LONE_ATTRIBUTE;
                attribute = getAttribute();
                break;

            case ';':
                t = SEMICOLON;
                break;

            case '(':
                t = LPAR;
                break;

            case ')':
                t = RPAR;
                break;

            case 'A':
                getNextChar();
                if(ch == 'B') {
                    t = AB;
                } else if (ch == 'W') {
                    t = AW;
                } else {
                    unexpected("B or W");
                }
                getNextChar();
                attribute = getAttribute();
                break;

            case 'B':
                t = B;
                getNextChar();
                attribute = getAttribute();
                break;

            case 'W':
                t = W;
                getNextChar();
                attribute = getAttribute();
                break;

            case 'C':
                t = C;
                getNextChar();
                if(ch == 'R') {
                    t = CR;
                    getNextChar();
                }
                attribute = getAttribute();
                break;

            case 'F':
                getNextChar();
                if(ch != 'F') {
                    unexpected("F");
                }
                t = FF;
                getNextChar();
                attribute = getAttribute();
                break;

            case 'G':
                getNextChar();
                if(ch != 'M') {
                    unexpected("M");
                }
                t = GM;
                getNextChar();
                attribute = getAttribute();
                break;

            case 'H':
                getNextChar();
                if(ch != 'A') {
                    unexpected("A");
                }
                t = HA;
                getNextChar();
                attribute = getAttribute();
                break;

            case 'S':
                getNextChar();
                if(ch != 'Z') {
                    unexpected("Z");
                }
                t = SZ;
                getNextChar();
                attribute = getAttribute();
                break;

            case (char)-1:
                t = EOF;
                break;

            default:
                throw new IOException("Invalid token '" + ch + "'!");
        }

        return new ScannedToken(t, attribute, line, col);
    }

    private void unexpected(String expected) throws IOException {
        throw new IOException("Expected '" + expected + "' but scanned '" + ch + "' on line " + line + ", col " + col);
    }

    /**
     * Scans the attribute string while doing some pre-conversions as per the most permissive value type "Text".
     * See https://www.red-bean.com/sgf/sgf4.html#text for more information.
     * @return An pre-formatted attribute string
     * @throws IOException
     */
    private String getAttribute() throws IOException {
        if(ch != '[') {
            unexpected("[");
        }
        getNextChar();
        StringBuilder attributeSB = new StringBuilder();
        while(ch != ']' && ch != eof) {
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
        if(ch != ']') {
            unexpected("]");
        }
        return attributeSB.toString();
    }

    private void getNextChar() {
        try {
            col++;
            ch = (char)input.read();
            while(ch == '\r') {
                ch = (char)input.read();
            }
            if(ch == '\n') {
                col = 0;
                line++;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
