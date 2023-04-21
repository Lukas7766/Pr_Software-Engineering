package pr_se.gogame.model;

public enum SgfToken {
    B("B[%s%s]"),
    W("W[%s%s]"),
    PB("PB[%s%s]"),
    PW("PW[%s%s]"),




   /*
            PW[]
    KM[] //Komi
            DT[]
    TM[]//ka was des is
            C[] //comment
    Re[] //Result
            HA[] //Number of Handicap stones
    TM[] //Timer wird anscheinend in sekunden angegeben
            MA[]
    LB[]*/


    ;

    //TODO: create token List or map ? maybe with enum
    //Maybe Write token in a formattable way to easily insert coords
    //Like this --> "B[%s%s]; --> Because of that string operations should be less and maybe faster

    private String value;

    SgfToken(String token) {
        this.value = token;
    }


    public String getValue() {
        return value;
    }
}
