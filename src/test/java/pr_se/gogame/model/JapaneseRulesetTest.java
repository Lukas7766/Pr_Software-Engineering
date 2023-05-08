package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JapaneseRulesetTest {
    JapaneseRuleset japaneseRuleset;

    @BeforeEach
    void setUp() {
        japaneseRuleset = new JapaneseRuleset();
    }

    @Test
    void getSuicide() {
        assertTrue(japaneseRuleset.getSuicide(null));
    }

    @Test
    @DisplayName("allowed repeatable moves should be 1")
    void getKoAmount() {
        assertEquals(1, japaneseRuleset.getKoAmount());
    }

    @Test
    void hasDefaultHandicapPlacement() {
        assertTrue(japaneseRuleset.hasDefaultHandicapPlacement());
    }

    @Test
    void setHandicapStones() {
    }

    @Test
    void getKomi() {
        assertEquals(6.5, japaneseRuleset.getKomi());
    }

    @Test
    void predicateKoMove() {
    }

    @Test
    void getKoMove() {
    }

    @Test
    void resetKoMove() {
    }

    @Test
    void scoreGame() {
    }

    @Test
    void calculateTerritoryPoints() {
    }

    @Test
    void floodFill() {
    }

}