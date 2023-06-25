package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.file.FileHandler;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;

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
    void emptySave() {
        assertSavingWorks();
    }

    @Test
    void emptySaveAndLoad() {
        emptySave();

        assertLoadingWorks();
    }

    @Test
    void onlyHandicapSave() {
        handicap = 9;
        setUp();
        assertSavingWorks();
    }

    @Test
    void onlyHandicapSaveAndLoad() {
        onlyHandicapSave();

        assertLoadingWorks();
    }

    @Test
    void handicapAndOneStoneSave() {
        handicap = 9;
        setUp();
        game.playMove(0, 0);
        assertSavingWorks();
    }

    @Test
    void handicapAndOneStoneSaveAndLoad() {
        handicapAndOneStoneSave();

        assertLoadingWorks();
    }

    @Test
    void onlySetupSave() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(1, 0, BLACK);
        game.placeSetupStone(0, 1, WHITE);
        game.placeSetupStone(1, 1, WHITE);

        assertSavingWorks();
    }

    @Test
    void onlySetupSaveAndLoad() {
        onlySetupSave();

        assertLoadingWorks();
    }

    @Test
    void onlySetupAndOneStoneSave() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(1, 0, BLACK);
        game.placeSetupStone(0, 1, WHITE);
        game.placeSetupStone(1, 1, WHITE);

        game.setSetupMode(false);
        game.playMove(0, 2);

        assertSavingWorks();
    }

    @Test
    void onlySetupAndOneStoneSaveAndLoad() {
        onlySetupAndOneStoneSave();

        assertLoadingWorks();
    }

    @Test
    void onlyPassSave() {
        game.pass();

        assertSavingWorks();
    }

    @Test
    void onlyPassSaveAndLoad() {
        onlyPassSave();

        assertLoadingWorks();
    }

    @Test
    void onlyPassAndOneStoneSave() {
        game.pass();
        game.playMove(0, 0);

        assertSavingWorks();
    }

    @Test
    void onlyPassAndOneStoneSaveAndLoad() {
        onlyPassAndOneStoneSave();

        assertLoadingWorks();
    }

    void assertSavingWorks() {
        assertTrue(FileHandler.saveFile(game, file));
    }

    void assertLoadingWorks() {
        FileHandler.loadFile(game, file);

        assertEquals(size, game.getSize());
        assertEquals(handicap, game.getHandicap());
        assertTrue(oldHistory.hasSameContentAs(game.getHistory()));
    }

}
