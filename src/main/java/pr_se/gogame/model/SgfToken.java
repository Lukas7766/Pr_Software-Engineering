package pr_se.gogame.model;

public enum SgfToken {

    /**
     * Move for black
     */
    B(";B[%s]"),
    /**
     * Delete Stone/Set empty Stone
     */
    AE("AE[%s]"),
    /**
     * Move for white
     */
    W(";W[%s]"),

    /**
     * Add white stone First for handicap
     */
    AWF(";AW%s"),

    /**
     * Add black stone First for handicap
     */
    ABF(";AB%s"),

    /**
     * Add white stone for handicap
     */
    AW("AW%s"),

    /**
     * Add black stone for handicap
     */
    AB("AB%s"),

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
     * Handicap
     */
    HA("HA[%s]"),
    /**
     * Markup f.ex. with an x
     */
    MA("MA[%s]"),
    /**
     * label for a coordinate
     */
    LB("LB[%s]"),
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
    SgfToken(String token) {
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
