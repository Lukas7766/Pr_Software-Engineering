package pr_se.gogame.model.file;

import java.io.Serializable;

/**
 * Data structure for storing information about SGF Tokens scanned by the SGFScanner
 */
public class ScannedToken implements Serializable {
    /**
     * The scanned SGFToken
     */
    private final SGFToken token;

    /**
     * The SGF attribute (inside the [] brackets)
     */
    private final String attributeValue;

    /**
     * The line that the token was on
     */
    private final int line;

    /**
     * The column that the token was on
     */
    private final int col;

    /**
     * Instantiates a new ScannedToken
     * @param token The scanned SGFToken
     * @param attributeValue The SGF attribute (inside the [] brackets)
     * @param line The line that the token was on
     * @param col The column that the token was on
     */
    public ScannedToken(SGFToken token, String attributeValue, int line, int col) {
        this.token = token;
        this.attributeValue = attributeValue;
        this.line = line;
        this.col = col;
    }


    /**
     * Returns the scanned SGF token
     * @return the scanned SGF token
     */
    public SGFToken getToken() {
        return token;
    }

    /**
     * Returns the SGF attribute
     * @return the SGF attribute of this ScannedToken
     */
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     * Returns the line that the token was on
     * @return the line that the token was on
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column that the token was on
     * @return the column that the token was on
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns a String representation of this ScannedToken's contents
     * @return a String representation of this ScannedToken's contents
     */
    @Override
    public String toString() {
        return "token \"" + String.format(token.getValue(), attributeValue) + "\" at line " + line + ", col " + col;
    }
}
