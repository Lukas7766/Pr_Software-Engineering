package pr_se.gogame.model.file;

import pr_se.gogame.model.HistoryNode;

import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;

public enum SGFToken {

    /**
     * Move for black
     */
    B("B[%s]", false),

    /**
     * Delete Stone/Set empty Stone
     */
    AE("AE[%s]", true),

    /**
     * Move for white
     */
    W("W[%s]", false),

    /**
     * Add white stone for handicap
     */
    AW("AW[%s]", true),

    /**
     * Add black stone for handicap
     */
    AB("AB[%s]", true),

    /**
     * Name for Player black
     */
    PB("PB[%s]", false),
    /**
     * Name for Player white
     */
    PW("PW[%s]", false),
    /**
     * Komi value like "1", "2"
     */
    KM("KM[%s]", false),

    /**
     * Textual comment
     */
    C("C[%s]", false),

    /**
     * Handicap (number of stones)
     */
    HA("HA[%s]", false),

    /**
     * Markup f.ex. with an x
     */
    MA("MA[%s]", false),

    /**
     * Circle mark
     */
    CR("CR[%s]", false),

    /**
     * Square mark
     */
    SQ("SQ[%s]", false),

    /**
     * Triangle mark
     */
    TR("TR[%s]", false),

    /**
     * The version of the SGF format that is used. We are using version 4.
     */
    FF("FF[%s]", false),

    /**
     * Game Type. 1 is Go.
     */
    GM("GM[%s]", false),

    /**
     * Board size.
     */
    SZ("SZ[%s]", false),

    /**
     * Opening parenthesis indicating beginning of subtree (probably unused by our program).
     */
    LPAR("(", false),

    /**
     * Opening parenthesis indicating end of subtree (probably unused by our program).
     */
    RPAR(")", false),

    /**
     * Prepended to some tokens. I'm unsure what it means, but we'll read it in separately just in case.
     */
    SEMICOLON(";", false),

    /**
     * Sometimes, a token can have multiple attributes. This is what this token signifies.
     */
    LONE_ATTRIBUTE("[%s]", false),

    /**
     * Marks the end of the file.
     */
    EOF("EOF", false);

    /**
     * The value for the enum
     */
    private final String value;

    private final boolean supportsMultiAttribs;

    /**
     * Creates a token
     *
     * @param token                String for the token
     * @param supportsMultiAttribs whether the token may have multiple attributes in the file
     */
    SGFToken(String token, boolean supportsMultiAttribs) {
        this.value = token;
        this.supportsMultiAttribs = supportsMultiAttribs;
    }


    /**
     * Returns the value of the token
     *
     * @return the token value
     */
    public String getValue() {
        return value;
    }

    public boolean hasMultiAttribs() {
        return supportsMultiAttribs;
    }

    public static SGFToken ofHistoryNode(HistoryNode node) {
        switch(node.getSaveToken()) {
            case SETUP, HANDICAP:
                if(node.getColor() == BLACK) {
                    return AB;
                } else if(node.getColor() == WHITE) {
                    return AW;
                } else {
                    return AE;
                }

            case MOVE, PASS:
                if(node.getColor() == BLACK) {
                    return B;
                } else {
                    return W;
                }

            default:
                return null;
        }
    }
}
