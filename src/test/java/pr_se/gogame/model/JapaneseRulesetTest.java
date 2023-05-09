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
    @DisplayName("suicide is not allowed in JP ruleset")
    void getSuicide() {
        assertFalse(japaneseRuleset.getSuicide(null));
    }

    @Test
    @DisplayName("allowed repeatable moves are one in JP ruleset")
    void getKoAmount() {
        assertEquals(1, japaneseRuleset.getKoAmount());
    }

    @Test
    @DisplayName("default handicap placement is active")
    void hasDefaultHandicapPlacement() {
        assertTrue(japaneseRuleset.hasDefaultHandicapPlacement());
    }

    @Test
    @DisplayName("6.5 komi points were added to player white")
    void getKomi() {
        assertEquals(6.5, japaneseRuleset.getKomi());
    }

    @Test
    void setHandicapStones() {
        assertTrue(false);
    }

    @Test
    void predicateKoMove() {
        assertTrue(false);
    }

    @Test
    void getKoMove() {
        assertTrue(false);
    }

    @Test
    void resetKoMove() {
        assertTrue(false);
    }

    @Test
    void scoreGame() {
        assertTrue(false);
    }

    @Test
    void calculateTerritoryPoints() {
        assertTrue(false);
    }

    @Test
    void floodFill() {
        assertTrue(false);
    }

}