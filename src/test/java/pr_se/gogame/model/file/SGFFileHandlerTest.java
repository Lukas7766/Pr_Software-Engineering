package pr_se.gogame.model.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.History;
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.helper.UndoableCommand;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;

class SGFFileHandlerTest {
    Game game;

    File file;

    SGFFileHandler sgfFileHandler;

    History oldHistory;

    int size = 19;

    int handicap = 0;

    static final String TEST_FILE_FOLDER = "./testFiles/";

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK, size, handicap, new JapaneseRuleset());
        oldHistory = game.getHistory();
        file = new File(TEST_FILE_FOLDER + "tmp.sgf");
        sgfFileHandler = new SGFFileHandler(game);
    }

    // Argument-Checking
    @Test
    void constructorArgs() {
        assertThrows(NullPointerException.class, () -> new SGFFileHandler(null));
    }

    @Test
    void saveFileArgs() {
        assertThrows(NullPointerException.class, () -> sgfFileHandler.saveFile(null));
    }

    @Test
    void loadFileArgs() {
        assertThrows(NullPointerException.class, () -> sgfFileHandler.loadFile(null));
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
        assertNull(sgfFileHandler.getCurrentFile());
        try {
            sgfFileHandler.saveFile(file);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(file, sgfFileHandler.getCurrentFile());

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

    @Test
    void handicapOneButNoStone() {
        File myFile = new File(TEST_FILE_FOLDER + "okHAWithoutStones.sgf");

        try {
            assertTrue(sgfFileHandler.loadFile(myFile));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(19, game.getSize());
        assertEquals(1, game.getHandicap());
    }

    @Test
    void noHandicapToken() {
        File myFile = new File(TEST_FILE_FOLDER + "noHA.sgf");

        try {
            assertTrue(sgfFileHandler.loadFile(myFile));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(19, game.getSize());
        assertEquals(0, game.getHandicap());
    }

    @Test
    void whiteHandicap() {
        handicap = 2;
        game.newGame(WHITE, size, handicap, new JapaneseRuleset());
        oldHistory = game.getHistory();

        comprehensiveTest();
    }

    @Test
    void whiteHandicapExtended() {
        whiteHandicap();
        game.getHistory().goToBeginning();
        assertEquals(WHITE, game.getCurColor());
        game.redo();
        assertEquals(WHITE, game.getCurColor());
        game.redo();
        assertEquals(BLACK, game.getCurColor());
    }

    @Test
    void resignAfterGame() {
        game.playMove(1, 1);
        game.resign();

        /*
         * SGF doesn't appear to count resignation as a move and there's no time to store the result; this is purely
         * to maximise code coverage.
         */
        assertSavingWorks();
    }

    @Test
    void emptyComment() {
        game.playMove(0, 0);
        game.setComment("");

        comprehensiveTest();
    }

    @Test
    void handicapOf1() {
        handicap = 1;
        setUp();
        game.playMove(0, 0);

        comprehensiveTest();
    }

    @Test
    void twoConsecutiveSameMoves() {
        game.playMove(0, 0, BLACK);
        game.playMove(1, 1, BLACK);

        comprehensiveTest();
    }

    @Test
    void blackSetup() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);

        comprehensiveTest();
    }

    @Test
    void whiteSetup() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, WHITE);

        comprehensiveTest();
    }

    @Test
    void marksCommentsBeforeFirstMove() {
        game.setComment("foo");
        game.mark(0, 0, MarkShape.CIRCLE);
        game.playMove(0, 0);

        comprehensiveTest();
    }

    // Invalid configurations
    @Test
    void nonExistentFile() {
        File f = new File("nonExistentFile");
        if(f.exists()) {
            fail();
        }
        assertThrows(NoSuchFileException.class, () -> sgfFileHandler.loadFile(f));
    }

    @Test
    void handicapAfterGameBegun() {
        game.playMove(0, 0);
        game.getHistory().addNode(new History.HistoryNode(null, History.HistoryNode.AbstractSaveToken.HANDICAP, null, ""));
        assertThrows(IllegalStateException.class, () -> sgfFileHandler.saveFile(file));
    }

    @Test
    void saveHandicapButNoStones() {
        game.newGame(BLACK, 19, 2, new JapaneseRuleset(), false);
        assertThrows(IllegalStateException.class, () -> sgfFileHandler.saveFile(file));
        game.getHistory().addNode(new History.HistoryNode(null, History.HistoryNode.AbstractSaveToken.MOVE, null, ""));
        assertThrows(IllegalStateException.class, () -> sgfFileHandler.saveFile(file));
    }

    @Test
    void onlyGameInfo() {
        file = new File(TEST_FILE_FOLDER + "onlyInfo.sgf");
        assertLoadingWorks();
    }

    @Test
    void noGameInfo() {
        invalidTest("noInfo.sgf");
    }

    @Test
    void noLPAR() {
        invalidTest("noLPAR.sgf");
    }

    @Test
    void noSemic() {
        invalidTest("noSemic.sgf");
    }

    @Test
    void invSGFVersion() {
        invalidTest("wrongSGFVersion.sgf");
    }

    @Test
    void invGM() {
        invalidTest("wrongGM.sgf");
    }

    @Test
    void loadAE() {
        invalidTest("invAE.sgf");
    }

    @Test
    void saveAE() {
        game.getHistory().addNode(new History.HistoryNode(new UndoableCommand() {
            @Override
            public void execute(final boolean saveEffects) {

            }

            @Override
            public void undo() {

            }
        }, History.HistoryNode.AbstractSaveToken.SETUP, null, ""));

        assertThrows(IllegalStateException.class, () -> sgfFileHandler.saveFile(file));
    }

    @Test
    void playAfterResign() {
        game.resign();
        game.getHistory().addNode(new History.HistoryNode(null, History.HistoryNode.AbstractSaveToken.MOVE, BLACK, ""));
        assertThrows(IllegalStateException.class, () -> sgfFileHandler.saveFile(file));
    }

    @Test
    void noEOFAfterEnd() {
        invalidTest("invNotEOF.sgf");
    }

    @Test
    void handicapNoStones() {
        invalidTest("invHAWithoutStones.sgf");
    }

    @Test
    void strayLoneAttribute() {
        invalidTest("invalidStrayLoneAttr.sgf");
    }

    @Test
    void multipleBranchesEarlier() {
        invalidTest("invMultipleBranchesEarlier.sgf");
    }

    @Test
    void multipleBranchesLater() {
        invalidTest("invalidMultipleBranches.sgf");
    }

    @Test
    void sizeTooSmall() {
        invalidTest("invSizeSmall.sgf");
    }

    @Test
    void sizeTooLarge() {
        invalidTest("invSizeLarge.sgf");
    }

    @Test
    void handicapTooSmall() {
        invalidTest("invHandicapSmall.sgf");
    }

    @Test
    void handicapTooLarge() {
        invalidTest("invHandicapLarge.sgf");
    }

    // Helper methods
    void invalidTest(String fileName) {
        try {
            assertFalse(sgfFileHandler.loadFile(new File(TEST_FILE_FOLDER + fileName)));
        } catch(IOException e) {
            e.printStackTrace();
            fail();
        } catch(LoadingGameException le) {
            le.printStackTrace();
        }
    }

    void comprehensiveTest() {
        assertSavingAndLoadingWorks();
        oldHistory = game.getHistory();
        addCommentAndMarks();
    }

    void addCommentAndMarks() {
        game.setComment("Good Luck!");
        game.mark(0, 0, MarkShape.CIRCLE);
        game.mark(1, 1, MarkShape.SQUARE);
        game.mark(2, 2, MarkShape.TRIANGLE);

        assertSavingAndLoadingWorks();
    }

    void assertSavingAndLoadingWorks() {
        assertSavingWorks();
        assertLoadingWorks();
    }

    void assertSavingWorks() {
        try {
            assertTrue(sgfFileHandler.saveFile(file));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    void assertLoadingWorks() {
        try {
            assertTrue(sgfFileHandler.loadFile(file));
        } catch (IOException | LoadingGameException e) {
            fail();
        }

        assertEquals(size, game.getSize());
        assertEquals(handicap, game.getHandicap());
        assertEquals(oldHistory, game.getHistory());
    }

}
