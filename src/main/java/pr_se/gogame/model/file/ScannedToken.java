package pr_se.gogame.model.file;

public class ScannedToken {
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
}
