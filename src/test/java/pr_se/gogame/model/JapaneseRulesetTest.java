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
    @DisplayName("testing getSuicide(), suicide is not allowed in JP ruleset")
    void getSuicide() {
        assertFalse(japaneseRuleset.getSuicide(null, null));
    }

    @Test
    @DisplayName("testing getKoAmount(), allowed repeatable moves are two in JP ruleset")
    void getKoAmount() {
        assertEquals(2, japaneseRuleset.getKoAmount());
    }

    @Test
    @DisplayName("testing hasDefaultHandicapPlacement(), default handicap placement is active")
    void hasDefaultHandicapPlacement() {
        assertTrue(japaneseRuleset.hasDefaultHandicapPlacement());
    }

    @Test
    @DisplayName("testing getKomi(), 6.5 komi points are defined in JP ruleset")
    void getKomi() {
        assertEquals(6.5, japaneseRuleset.getKomi());
    }

    @Test
    @DisplayName("testing scoreGame(), wrong input")
    void scoreGameWI() {
        assertThrowsExactly(IllegalArgumentException.class, () -> japaneseRuleset.scoreGame(null));
    }

    @Test
    @DisplayName("testing scoreGame(), black wins with 10 points")
    void scoreGameB() {
        /*
        null BLACK WHITE null null null null null null
        BLACK null BLACK null null null null null null
        null BLACK WHITE WHITE null null null null null
        null BLACK WHITE null null null null null null
        null BLACK WHITE null null null null null null
        null BLACK WHITE null null null null null null
        null BLACK WHITE null null null null null null
        null BLACK WHITE null null null null null null
        null BLACK WHITE null null null null null null
         */
        Game game = new Game();
        game.newGame(GameCommand.BLACK_STARTS, 9, 0);
        game.playMove(0, 1);
        game.playMove(0, 2);

        game.playMove(1, 0);
        game.playMove(1, 1);

        game.playMove(1, 2);
        game.playMove(2, 3);

        game.playMove(2, 1);
        game.playMove(2, 2);

        game.playMove(3, 1);
        game.playMove(3, 2);

        game.playMove(4, 1);
        game.playMove(4, 2);

        game.playMove(5, 1);
        game.playMove(5, 2);

        game.playMove(6, 1);
        game.playMove(6, 2);

        game.playMove(7, 1);
        game.playMove(7, 2);

        game.playMove(8, 1);
        game.playMove(8, 2);

        game.scoreGame();
        assertEquals(10, game.getScore(StoneColor.BLACK));
        assertEquals(6.5, game.getScore(StoneColor.WHITE));
        assertEquals(10, japaneseRuleset.scoreGame(game).getScoreBlack());
        assertEquals(6.5, japaneseRuleset.scoreGame(game).getScoreWhite());
        assertEquals(StoneColor.BLACK, japaneseRuleset.scoreGame(game).getWinner());
        assertEquals(
                "Black won!\n" +
                        "\n" +
                        "Handicap: 0\n" +
                        "+ Territory points: 9\n" +
                        "+ Captured stones: 1\n" +
                        "\n" +
                        "= 10.0 points", japaneseRuleset.scoreGame(game).getGameResult());
    }

    @Test
    @DisplayName("testing scoreGame(), white wins with 6.5 points")
    void scoreGameW() {
        /*
        null BLACK WHITE null null null null null null
        BLACK null BLACK null null null null null null
        null BLACK WHITE WHITE null null null null null
        null null null null null null null null null
        null null null null null null null null null
        null null null null null null null null null
        null null null null null null null null null
        null null null null null null null null null
        null null null null null null null null null
         */
        Game game = new Game();
        game.newGame(GameCommand.BLACK_STARTS, 9, 0);
        game.playMove(0, 1);
        game.playMove(0, 2);

        game.playMove(1, 0);
        game.playMove(1, 1);

        game.playMove(1, 2);
        game.playMove(2, 3);

        game.playMove(2, 1);
        game.playMove(2, 2);

        game.scoreGame();
        assertEquals(3, game.getScore(StoneColor.BLACK));
        assertEquals(6.5, game.getScore(StoneColor.WHITE));

        assertEquals(3, japaneseRuleset.scoreGame(game).getScoreBlack());
        assertEquals(6.5, japaneseRuleset.scoreGame(game).getScoreWhite());
        assertEquals(StoneColor.WHITE, japaneseRuleset.scoreGame(game).getWinner());
        assertEquals(
                "White won!\n" +
                        "\n" +
                        "Komi: 6.5\n" +
                        "+ Territory points: 0\n" +
                        "+ Captured stones: 0\n" +
                        "\n" +
                        "= 6.5 points", japaneseRuleset.scoreGame(game).getGameResult());

    }

    private void printBoard(Game game) {
        for (int i = 0; i < game.getBoard().getSize(); i++) {
            for (int j = 0; j < game.getBoard().getSize(); j++) {
                System.out.print(game.getBoard().getColorAt(i, j) + " ");
            }
            System.out.println();
        }
    }

}