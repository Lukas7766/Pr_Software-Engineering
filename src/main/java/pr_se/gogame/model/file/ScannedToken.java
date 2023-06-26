package pr_se.gogame.model.file;

import java.io.Serializable;

public class ScannedToken implements Serializable {
    private final SGFToken token;

    private final String attributeValue;

    private final int line;

    private final int col;

    public ScannedToken(SGFToken token, String attributeValue, int line, int col) {
        this.token = token;
        this.attributeValue = attributeValue;
        this.line = line;
        this.col = col;
    }

    public SGFToken getToken() {
        return token;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "token \"" + String.format(token.getValue(), attributeValue) + "\" at line " + line + ", col " + col;
    }
}
