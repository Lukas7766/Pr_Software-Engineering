package pr_se.gogame.model;

public enum SgfToken {

    /**
     * Move for black
     */
    B("B[%s]"),
    /**
     *Delete Stone/Set empty Stone
     */
    AE("AE[%s]"), //
    /**
     *Move for white
     */
    W("W[%s]"), //
    /**
     *Name for Plyer black
     */
    PB("PB[%s]"), //
    /**
     *Name for Player white
     */
    PW("PW[%s]"), //
    /**
     * Komi value like "1", "2"
     */
    KM("KM[%s]"), //
    /**
     * Date when the game was played use "YYYY-MM-DD" format
     */
    DT("DT[%s]"), //
    /**
     * Time but write in a real number e.g. "4600" or "300"
     */
    TM("TM[%s]"), //
    /**
     * Textual comment
     */
    C("C[%s]"), //

    //RE("RE[%s]"), //for the Result but has mandatory format
    /**
     * Handicap
     */
    HA("HA[%s]"), //
    /**
     * Markup f.ex. with an x
     */
    MA("MA[%s]"), //
    /**
     * label for a coordinate
     */
    LB("LB[%s]"), //
    /**
     * The format for the first line of a File
     */
    START("FF[4]GM[1]SZ[9]PB[Black]PW[White]");//TODO: change so that this has to be formatted correctly (one for every variable)

    /**
     * The value for the enum
     */
    private final String value;

    /**
     * Creates a token
     * @param token String for the token
     */
    SgfToken(String token) {
        this.value = token;
    }


    /**
     * Returns the value of the token
     * @return the token value
     */
    public String getValue() {
        return value;
    }
}
