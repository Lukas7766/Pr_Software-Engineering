package pr_se.gogame.model.file;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SGFToken {

    /**
     * Move for black
     */
    B(";B[%s]"),

    /**
     * Delete Stone/Set empty Stone
     */
    AE(";AE[%s]"),

    /**
     * Move for white
     */
    W(";W[%s]"),

    /**
     * Add white stone for handicap
     */
    AW("AW[%s]"),

    /**
     * Add black stone for handicap
     */
    AB("AB[%s]"),

    /**
     * Name for Player black
     */
    PB("PB[%s]"),
    /**
     * Name for Player white
     */
    PW("PW[%s]"),
    /**
     * Komi value like "1", "2"
     */
    KM("KM[%s]"),

    /**
     * Textual comment
     */
    C("C[%s]"),

    /**
     * Handicap (number of stones)
     */
    HA("HA[%s]"),

    /**
     * Markup f.ex. with an x
     */
    MA("MA[%s]"),

    /**
     * label for a coordinate
     */
    LB("LB[%s:%s]"),

    /**
     * The version of the SGF format that is used. We are using version 4.
     */
    FF("FF[%s]"),

    /**
     * Game Type. 1 is Go.
     */
    GM("GM[%s]"),

    /**
     * Board size.
     */
    SZ("SZ[%s]"),

    /**
     * Opening parenthesis indicating beginning of subtree (probably unused by our program).
     */
    LPAR("("),

    /**
     * Opening parenthesis indicating end of subtree (probably unused by our program).
     */
    RPAR(")"),

    /**
     * Prepended to some tokens. I'm unsure what it means, but we'll read it in separately just in case.
     */
    SEMICOLON(";"),

    /**
     * Sometimes, a token can have multiple attributes. This is what this token signifies.
     */
    LONE_ATTRIBUTE("[%s]"),

    /**
     * Marks the end of the file.
     */
    EOF("EOF"),

    /**
     * The format for the first line of a File
     */
    START("(;FF[4]GM[1]SZ[%s]");

    /**
     * The value for the enum
     */
    private final String value;

    /**
     * Creates a token
     *
     * @param token String for the token
     */
    SGFToken(String token) {
        this.value = token;
    }


    /**
     * Returns the value of the token
     *
     * @return the token value
     */
    public String getValue() {
        return value;
    }
}
