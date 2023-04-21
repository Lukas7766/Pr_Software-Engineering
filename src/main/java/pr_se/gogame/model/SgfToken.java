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
    LB("LB[%s]");

    //TODO: create token List or map ? maybe with enum
    //Maybe Write token in a formattable way to easily insert coords
    //Like this --> "B[%s%s]; --> Because of that string operations should be less and maybe faster

    private final String value;

    SgfToken(String token) {
        this.value = token;
    }


    public String getValue() {
        return value;
    }
}
