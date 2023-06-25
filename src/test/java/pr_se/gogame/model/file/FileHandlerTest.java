package pr_se.gogame.model.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.History;
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;

class FileHandlerTest {
    Game game;

    File file;

    History oldHistory;

    int size = 19;

    int handicap = 0;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK, size, handicap, new JapaneseRuleset());
        oldHistory = game.getHistory();
        file = new File("tmp.sgf");
    }

    // Argument-Checking
    @Test
    void saveFileArgs() {
        assertThrows(NullPointerException.class, () -> FileHandler.saveFile(null, file));
        assertThrows(NullPointerException.class, () -> FileHandler.saveFile(game, null));
    }

    @Test
    void loadFileArgs() {
        assertThrows(NullPointerException.class, () -> FileHandler.loadFile(null, file));
        assertThrows(NullPointerException.class, () -> FileHandler.loadFile(game, null));
    }

    // Edge cases

    @Test
    void empty() {
        comprehensiveTest();
    }

    @Test
    void onlyHandicap() {
        handicap = 9;
        setUp();

        comprehensiveTest();
    }

    @Test
    void onlyHandicapCommentsAndMark() {
        handicap = 2;
        setUp();
        game.mark(0, 0, MarkShape.CIRCLE);
        game.setComment("Good Luck!");

        comprehensiveTest();
    }

    @Test
    void handicapAndOneStone() {
        handicap = 9;
        setUp();
        game.playMove(0, 0);

        comprehensiveTest();
    }

    @Test
    void onlySetup() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(1, 0, BLACK);
        game.placeSetupStone(0, 1, WHITE);
        game.placeSetupStone(1, 1, WHITE);

        comprehensiveTest();
    }

    @Test
    void onlySetupAndOneStone() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(1, 0, BLACK);
        game.placeSetupStone(0, 1, WHITE);
        game.placeSetupStone(1, 1, WHITE);

        game.setSetupMode(false);
        game.playMove(0, 2);

        comprehensiveTest();
    }

    @Test
    void onlyPass() {
        game.pass();

        comprehensiveTest();
    }

    @Test
    void onlyPassAndOneStone() {
        game.pass();
        game.playMove(0, 0);

        comprehensiveTest();
    }

    @Test
    void getCurrentFile() {
        assertEquals(file, FileHandler.getCurrentFile());

        comprehensiveTest();
    }

    @Test
    void clearCurrentFile() {
        getCurrentFile();
        FileHandler.clearCurrentFile();
        assertNull(FileHandler.getCurrentFile());

        comprehensiveTest();
    }

    @Test
    void sampleGame() {
        size = 13;
        handicap = 5;
        setUp();

        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(0, 1, BLACK);
        game.placeSetupStone(1, 0, WHITE);
        game.placeSetupStone(1, 1, WHITE);

        game.setSetupMode(false);
        game.playMove(2, 2);
        game.playMove(3, 3);

        comprehensiveTest();
    }

    // Invalid configurations
    @Test
    void handicapAfterGameBegun() {
        game.playMove(0, 0);
        game.setHandicapStoneCounter(1);
        game.placeHandicapPosition(1, 0, true);
        assertFalse(FileHandler.saveFile(game, file));
    }

    void comprehensiveTest() {
        assertSavingAndLoadingWorks();
        oldHistory = game.getHistory();
        addCommentAndMarks();
    }

    void addCommentAndMarks() {
        game.setComment("Good Luck!");
        game.mark(0, 0, MarkShape.CIRCLE);

        assertSavingAndLoadingWorks();
    }

    void assertSavingAndLoadingWorks() {
        assertSavingWorks();
        assertLoadingWorks();
    }

    void assertSavingWorks() {
        assertTrue(FileHandler.saveFile(game, file));
    }

    void assertLoadingWorks() {
        assertTrue(FileHandler.loadFile(game, file));

        assertEquals(size, game.getSize());
        assertEquals(handicap, game.getHandicap());
        assertEquals(oldHistory, game.getHistory());
    }

}
