package pr_se.gogame.model.ruleset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.file.LoadingGameException;
import pr_se.gogame.model.helper.StoneColor;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;

class JapaneseRulesetTest {
    JapaneseRuleset japaneseRuleset;

    static final String TESTFILE_FOLDER = "./testFiles/";

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
    @DisplayName("testing getKomi(), 6.5 komi points are defined in JP ruleset")
    void getKomi() {
        assertEquals(6.5, japaneseRuleset.getKomi());
    }

    @Test
    @DisplayName("testing scoreGame(), wrong input")
    void scoreGameWI() {
        assertThrowsExactly(NullPointerException.class, () -> japaneseRuleset.scoreGame(null));
    }

    @Test
    @DisplayName("testing scoreGame(), black wins with 10 points")
    void scoreGameB() {
        /*
         *      0  1  2  3  4  5  6  7  8
         *   0     B
         *   1  B     B  B  B  B  B  B  B
         *   2  W  B  W  W  W  W  W  W  W
         *   3        W
         *   4
         *   5
         *   6
         *   7
         *   8
         */
        Game game = new Game();
        game.newGame(BLACK, 9, 0, new JapaneseRuleset());
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

        printBoard(game);

        assertDoesNotThrow(game::scoreGame);
        assertEquals(10, game.getScore(BLACK));
        assertEquals(6.5, game.getScore(WHITE));
        assertEquals(10, japaneseRuleset.scoreGame(game).getScore(BLACK));
        assertEquals(6.5, japaneseRuleset.scoreGame(game).getScore(WHITE));
        assertEquals(BLACK, japaneseRuleset.scoreGame(game).getWinner());
        assertEquals(
                """
                        Black won!

                        Handicap: 0.0
                        + Territory points: 9
                        + Captured stones: 1

                        = 10.0 points""", japaneseRuleset.scoreGame(game).getDescription(BLACK));
    }

    @Test
    @DisplayName("testing scoreGame(), white wins with 6.5 points")
    void scoreGameW() {
        /*
         *   Lower case = captured stone
         *      0  1  2  3  4  5  6  7  8
         *   0     B
         *   1  B  w  B
         *   2  W  B  W
         *   3        W
         *   4
         *   5
         *   6
         *   7
         *   8
         */
        Game game = new Game();
        game.newGame(BLACK, 9, 0, new JapaneseRuleset());
        game.playMove(0, 1);
        game.playMove(0, 2);

        game.playMove(1, 0);
        game.playMove(1, 1); // This WHITE stone will get captured by BLACK, adding 1 capture point to Black's score.

        game.playMove(1, 2);
        game.playMove(2, 3);

        game.playMove(2, 1);
        game.playMove(2, 2);

        printBoard(game);

        assertDoesNotThrow(game::scoreGame);
        assertEquals(3, game.getScore(BLACK));
        assertEquals(6.5, game.getScore(WHITE));

        assertEquals(3, japaneseRuleset.scoreGame(game).getScore(BLACK));
        assertEquals(6.5, japaneseRuleset.scoreGame(game).getScore(WHITE));
        assertEquals(WHITE, japaneseRuleset.scoreGame(game).getWinner());
        assertEquals(
                """
                        White won!

                        Komi: 6.5
                        + Territory points: 0
                        + Captured stones: 0

                        = 6.5 points""", japaneseRuleset.scoreGame(game).getDescription(WHITE));

    }

    @Test
    void isKo() {
        Game game = loadFile("KoSituation.sgf");
        game.goToEnd();
        assertTrue(game.playMove(2, 1));
        assertFalse(game.playMove(1, 1));
    }

    @Test
    void reset() {
        Game game = loadFile("KoSituation.sgf");
        game.goToEnd();
        assertTrue(game.playMove(2, 1));
        assertFalse(game.playMove(1, 1));

        game.getRuleset().reset();
        assertTrue(game.playMove(1, 1));
    }

    //helper methods
    Game loadFile(String fileName) {
        Game game = new Game();
        try {
            assertTrue(game.getFileHandler().loadFile(new File(TESTFILE_FOLDER + fileName)));
        } catch (LoadingGameException e) {
            e.printStackTrace();
            fail();
        } catch (java.nio.file.NoSuchFileException e) {
            throw new RuntimeException(e);
        }

        return game;
    }

    void printBoard(Game game) {
        System.out.print("   ");
        for(int i = 0; i < game.getSize(); i++) {
            System.out.print(" " + i + " ");
        }
        System.out.println("   ");

        for(int y = 0; y < game.getSize(); y++) {
            System.out.print(" " + y + " ");
            for(int x = 0; x < game.getSize(); x++) {
                char output = ' ';
                if(game.getColorAt(x, y) == StoneColor.BLACK) {
                    output = 'B';
                } else if(game.getColorAt(x, y) == StoneColor.WHITE) {
                    output = 'W';
                }
                System.out.print(" " + output + " ");
            }
            System.out.println();
        }
    }
}