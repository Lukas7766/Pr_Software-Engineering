package pr_se.gogame.model;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class UserDefinableRuleset implements Ruleset {
    private boolean suicideAllowed = false;
    private boolean hasDefaultHandicap = true;

    int koAmount = 2;

    private BiConsumer<Game, Integer> handicapStoneSetter;
    private Predicate<StoneGroup> suicideCheck;


    /** Depending on the rules, suicide is allowed or forbidden. <br>
     * -> set to true if you want to allow it <br>
     * -> set to false if you want to forbid it <br>
     * @param allow (true or false)
     * @param check predicate to evaluate whether the stonegroup in question is allowed to commit suicide.
     */
    void setSuicide(boolean allow, Predicate<StoneGroup> check) {
        suicideAllowed = allow;
        suicideCheck = check;
    }

    /** Depending on the rules, suicide is allowed or forbidden. This is the default option to forbid suicide. <br>
     * @return false
     */
    void setDefaultSuicide() {
        suicideAllowed = false;
        suicideCheck = null;
    }

    @Override
    public boolean getSuicide(StoneGroup existingGroup, StoneGroup addedStone) {
        return suicideAllowed && suicideCheck.test(existingGroup);
    }

    /** To prevent endless repetitions or make them pointless, positional repetition is restricted. <br>
     * This method sets the amount of restricted repetitions.
     * @param amount (2 ... n)
     */
    void setKoAmount(int amount) {
        koAmount = amount;
    }

    @Override
    public int getKoAmount() {
        return koAmount;
    }

    @Override
    public UndoableCommand updateKoMove(int x, int y) {
        return null;
    }

    @Override
    public UndoableCommand checkKoMove(int x, int y) {
        return null;
    }

    @Override
    public boolean isKoMove(int x, int y) {
        return false;
    }

    @Override
    public UndoableCommand resetKoMove() {
        return null;
    }

    @Override
    public UndoableCommand isKo(Game game) {
        return null;
    }

    /** To prevent endless repetitions or make them pointless, positional repetition is restricted. <br>
     * Two consecutive moves (except passing) may not restore the original position.
     * @return 2
     */
    void setDefaultKoAmount() {
        koAmount = 2;
    }

    @Override
    public GameResult scoreGame(Game game) {
        return new GameResult(1, 1, null,"");
    }

    @Override
    public boolean hasDefaultHandicapPlacement() {
        return hasDefaultHandicap;
    }

    public void setCustomHandicapPlacement(BiConsumer<Game, Integer> stoneSetter) {
        hasDefaultHandicap = false;
        handicapStoneSetter = stoneSetter;
    }

    public void setDefaultHandicapPlacement() {
        hasDefaultHandicap = true;
        handicapStoneSetter = null;
    }

    @Override
    public void setHandicapStones(Game game, StoneColor beginner, int noStones) {
        if(!hasDefaultHandicap) {
            handicapStoneSetter.accept(game, noStones);
        }
    }
}
