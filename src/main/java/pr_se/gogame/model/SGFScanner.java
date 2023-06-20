package pr_se.gogame.model;

import java.io.FileReader;
import java.io.IOException;

import static pr_se.gogame.model.SgfToken.*;

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

        SgfToken t = null;
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

            case 'C': // TODO: Do something with comments and *maybe* allow comments to be written by the program (to ease implementation of the problems and solutions).
                t = C;
                getNextChar();
                attribute = getAttribute();

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
        throw new IOException("Expected \'" + expected + "\' but scanned \'" + ch + "\' on line " + line + ", col " + col);
    }

    private String getAttribute() throws IOException {
        if(ch != '[') {
            unexpected("[");
        }
        getNextChar();
        StringBuilder attributeSB = new StringBuilder();
        while(ch != ']' && ch != eof) {
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
