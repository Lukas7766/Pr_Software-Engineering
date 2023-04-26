package pr_se.gogame.model;

public enum SgfToken {
    B("B[%s]"),
    W("W[%s]"),
    PB("PB[%s]"),
    PW("PW[%s]"),
    KM("KM[%s]"),
    DT("DT[%s]"),
    TM("TM[%s]"),
    C("C[%s]"),
    RE("RE[%s]"),
    HA("HA[%s]"),
    MA("MA[%s]"),
    LB("LB[%s]"),
    START("FF[4]GM[1]SZ[9]PB[Black]PW[White]");//TODO: change so that this has to be formatted correctly (one for every variable)

    //TODO: create token List or map ? maybe with enum
    //TODO: Test
    //Maybe Write token in a formattable way to easily insert coords
    //Like this --> "B[%s]; --> Because of that string operations should be less and maybe faster (its not faster)

    private final String value;


    SgfToken(String token) {
        this.value = token;
    }


    public String getValue() {
        return value;
    }
}
