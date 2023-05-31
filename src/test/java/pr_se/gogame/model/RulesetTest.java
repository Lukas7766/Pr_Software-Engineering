package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RulesetTest {

    Ruleset ruleset;

    @BeforeEach
    void setup(){
        //create empty ruleset for testing default methods
        ruleset = new Ruleset() {
            @Override
            public boolean predicateKoMove(int x, int y) {
                return false;
            }

            @Override
            public Position getKoMove() {
                return null;
            }

            @Override
            public void resetKoMove() {

            }

            @Override
            public GameResult scoreGame(Game game) {
                return null;
            }
        };
    }

    @Test
    @DisplayName("testing getSuicide(), is false by default")
    void getSuicide() {
        assertFalse(ruleset.getSuicide(null, null));
    }

    @Test
    @DisplayName("testing getKoAmount(), is 2 by default")
    void getKoAmount() {
        assertEquals(2, ruleset.getKoAmount());
    }


    @Test
    @DisplayName("testing hasDefaultHandicapPlacement(), is true by default")
    void hasDefaultHandicapPlacement() {
        assertTrue(ruleset.hasDefaultHandicapPlacement());
    }

    @Test
    @DisplayName("testing setHandicapStones(), applying default mechanism")
    void testSetHandicapStones() {
        Game game;
        for (int i = 0; i < 10; i++) {
            game = new Game();
            game.newGame(GameCommand.BLACK_STARTS, 19,i);

            switch (i){
                case 0,1 -> {
                    for (int x = 0; x < 19; x++) {
                        for (int y = 0; y < 19; y++) {
                            assertNull(game.getColorAt(x, y));
                        }
                    }
                }
                case 2 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                }
                case 3 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 15));
                }
                case 4 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 3));
                }
                case 5 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(9, 9));
                }
                case 6 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 9));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 9));
                }
                case 7 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 9));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 9));
                    assertEquals(StoneColor.BLACK, game.getColorAt(9, 9));
                }
                case 8 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 9));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 9));
                    assertEquals(StoneColor.BLACK, game.getColorAt(9, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(9, 15));
                }
                case 9 -> {
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(3, 9));
                    assertEquals(StoneColor.BLACK, game.getColorAt(15, 9));
                    assertEquals(StoneColor.BLACK, game.getColorAt(9, 3));
                    assertEquals(StoneColor.BLACK, game.getColorAt(9, 15));
                    assertEquals(StoneColor.BLACK, game.getColorAt(9, 9));
                }

            }
        }
    }

    @Test
    @DisplayName("testing setHandicapStones(), wrong input")
    void testSetHandicapStonesWI() {
        assertThrowsExactly(IllegalArgumentException.class, () -> { ruleset.setHandicapStones(null, null, 0); });
        assertThrowsExactly(IllegalArgumentException.class, () -> { ruleset.setHandicapStones(new Game(), null, 0); });
        assertThrowsExactly(IllegalArgumentException.class, () -> { ruleset.setHandicapStones(new Game(), StoneColor.BLACK, -1); });
        assertThrowsExactly(IllegalArgumentException.class, () -> { ruleset.setHandicapStones(new Game(), StoneColor.BLACK, 10); });
    }


    @Test
    @DisplayName("testing getKomi(), in default is 7.5")
    void getKomi() {
        assertEquals(7.5, ruleset.getKomi());
    }
}