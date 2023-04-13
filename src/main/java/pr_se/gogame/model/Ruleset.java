package pr_se.gogame.model;

public interface Ruleset {

    /** Depending on the rules, suicide is allowed or forbidden. <br>
     * -> set to true if you want to allow it <br>
     * -> set to false if you want to forbid it <br>
     * @param allow (true or false)
     */
    void setSuicide(boolean allow);

    /** Depending on the rules, suicide is allowed or forbidden. This is the default option to forbid suicide. <br>
     * @return false
     */
    default boolean setDefaultSuicide() {
        return false;
    }

    /** To prevent endless repetitions or make them pointless, positional repetition is restricted. <br>
     * This method sets the amount of restricted repetitions.
     * @param amount (2 ... n)
     */
    void setKoAmount(int amount);

    /** To prevent endless repetitions or make them pointless, positional repetition is restricted. <br>
     * Two consecutive moves (except passing) may not restore the original position.
     * @return 2
     */
    default int setDefaultKoAmount() {
        return 2;
    }

    /** Evaluation is the central feature of a set of rules; It varies depending on the set of rules.
     *
     */
    void scoreGame();

    //Kompensationspunkte
}
