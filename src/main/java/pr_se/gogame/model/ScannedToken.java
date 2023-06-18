package pr_se.gogame.model;

public class ScannedToken {
    private final SgfToken token;

    private final String attributeValue;

    private final int line;

    private final int col;

    public ScannedToken(SgfToken token, String attributeValue, int line, int col) {
        this.token = token;
        this.attributeValue = attributeValue;
        this.line = line;
        this.col = col;
    }

    public SgfToken getToken() {
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
