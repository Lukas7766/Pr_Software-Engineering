package pr_se.gogame.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.file.FileHandler;
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

    }

    @Test
    void onlyHandicap() {
        handicap = 9;
        setUp();
    }

    @Test
    void onlyHandicapCommentsAndMark() {
        handicap = 2;
        setUp();
        game.mark(0, 0, MarkShape.CIRCLE);
        game.setComment("Good Luck!");
    }

    @Test
    void handicapAndOneStone() {
        handicap = 9;
        setUp();
        game.playMove(0, 0);
    }

    @Test
    void onlySetup() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(1, 0, BLACK);
        game.placeSetupStone(0, 1, WHITE);
        game.placeSetupStone(1, 1, WHITE);
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
    }

    @Test
    void onlyPass() {
        game.pass();
    }

    @Test
    void onlyPassAndOneStone() {
        game.pass();
        game.playMove(0, 0);
    }

    @Test
    void getCurrentFile() {
        assertEquals(file, FileHandler.getCurrentFile());
    }

    @Test
    void clearCurrentFile() {
        getCurrentFile();
        FileHandler.clearCurrentFile();
        assertNull(FileHandler.getCurrentFile());
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
    }

    @AfterEach
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
        assertTrue(oldHistory.hasSameContentAs(game.getHistory()));
    }

}
