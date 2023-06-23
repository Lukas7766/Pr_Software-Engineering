package pr_se.gogame.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import static org.junit.jupiter.api.Assertions.*;

import static pr_se.gogame.model.StoneColor.*;

class BoardTest {
    Game game;
    Board board;

    /*
     * This variable is used for AssertThrows()-calls, as SonarQube (rightly) points out that nested method calls
     * might create ambiguity as to which method has thrown an expected (or unexpected) exception.
     */
    int maxCoord;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK, 19, 0, new JapaneseRuleset());
        board = new Board(game, 19);
        maxCoord = board.getSize() - 1;
    }

    @AfterEach
    void tearDown() {
    }

    // argument checking
    @Test
    void setStoneArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.setStone(-1, 0, BLACK, false));
        assertThrows(IllegalArgumentException.class, () -> board.setStone(maxCoord + 1, 0, BLACK, false));
        assertThrows(IllegalArgumentException.class, () -> board.setStone(0, -1, BLACK, false));
        assertThrows(IllegalArgumentException.class, () -> board.setStone(0, maxCoord + 1, BLACK, false));
        assertThrows(NullPointerException.class, () -> board.setStone(0, 0, null, false));
    }

    @Test
    void removeStoneArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(0, -1));
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(maxCoord + 1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(0, maxCoord + 1));
    }

    @Test
    void getColorAtArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(0, -1));
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(maxCoord + 1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(0, maxCoord + 1));
    }

    @Test
    void printDebugInfoArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(0, -1));
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(maxCoord + 1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(0, maxCoord + 1));
    }

    // other tests
    @Test
    void setStonePrepare() {
        assertNotNull(board.setStone(0, 0, BLACK, true));
        assertNotNull(board.setStone(1, 1, WHITE, true));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));
    }

    @Test
    void setStoneMove() {
        assertNotNull(board.setStone(0, 0, BLACK, false));
        assertNotNull(board.setStone(1, 1, WHITE, false));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));
    }

    @Test
    void noSetStoneOnSameSpace() {
        assertNotNull(board.setStone(0, 0, BLACK, false));
        assertNull(board.setStone(0, 0, BLACK, false));
    }

    @Test
    void groupMergingAndCapture() {
        assertNotNull(board.setStone(10, 9, BLACK, false));
        assertNotNull(board.setStone(10, 11, BLACK, false));
        assertNotNull(board.setStone(10, 10, BLACK, false));

        assertNotNull(board.setStone(9, 9, WHITE, false));
        assertNotNull(board.setStone(9, 10, WHITE, false));
        assertNotNull(board.setStone(9, 11, WHITE, false));
        assertNotNull(board.setStone(11, 9, WHITE, false));
        assertNotNull(board.setStone(11, 10, WHITE, false));
        assertNotNull(board.setStone(11, 11, WHITE, false));
        assertNotNull(board.setStone(10, 8, WHITE, false));

        assertEquals(BLACK, board.getColorAt(10, 11));

        assertNotNull(board.setStone(10, 12, WHITE, false));

        assertNull(board.getColorAt(10, 11));
    }

    // TODO: Move to GameTest
    @Test
    void setStoneKoPrevention() {
        game.newGame(BLACK, 19, 0, new JapaneseRuleset());

        assertNotNull(board.setStone(9, 1, BLACK, false));
        assertNotNull(board.setStone(10, 1, WHITE, false));
        assertNotNull(board.setStone(9, 3, BLACK, false));
        assertNotNull(board.setStone(10, 3, WHITE, false));
        assertNotNull(board.setStone(8, 2, BLACK, false));
        assertNotNull(board.setStone(11, 2, WHITE, false));

        assertNotNull(board.setStone(9, 2, WHITE, false));
        assertNotNull(board.setStone(10, 2, BLACK, false));

        assertNull(board.setStone(9, 2, WHITE, false));
        assertNull(board.setStone(9, 2, WHITE, false)); // for maximum code coverage
        assertNotNull(board.setStone(12, 2, WHITE, false)); // for maximum code coverage
    }

    @Test
    void setStone() {
        assertNotNull(board.setStone(0, 1, BLACK, false)); // save = false for max. code coverage
        assertNotNull(board.setStone(1, 0, WHITE, false));
        assertNotNull(board.setStone(board.getSize() - 1, board.getSize() - 2, BLACK, false));
        assertNotNull(board.setStone(board.getSize() - 2, board.getSize() - 1, WHITE, false));

        assertEquals(BLACK, board.getColorAt(0, 1));
        assertEquals(WHITE, board.getColorAt(1, 0));
        assertEquals(BLACK, board.getColorAt(board.getSize() - 1, board.getSize() - 2));
        assertEquals(WHITE, board.getColorAt(board.getSize() - 2, board.getSize() - 1));
    }

    @Test
    void setStoneSuicidePreventionIfNotAllowed() {
        assertNotNull(board.setStone(9, 1, BLACK, false));
        assertNotNull(board.setStone(9, 3, BLACK, false));
        assertNotNull(board.setStone(8, 2, BLACK, false));
        assertNotNull(board.setStone(10, 2, BLACK, false));

        assertNull(board.setStone(9, 2, WHITE, false));
        assertNotNull(board.setStone(9, 2, BLACK, false));
    }

    /*@Test
    void setStoneSuicideAllowed() {
        assertNotNull(board.setStone(9, 1, BLACK, false));
        assertNotNull(board.setStone(10, 1, BLACK, false));
        assertNotNull(board.setStone(9, 3, BLACK, false));
        assertNotNull(board.setStone(10, 3, BLACK, false));
        assertNotNull(board.setStone(8, 2, BLACK, false));
        assertNotNull(board.setStone(11, 2, BLACK, false));

        assertNotNull(board.setStone(9, 2, WHITE, false));
    }*/

    @Test
    void removeStone() {
        assertNotNull(board.setStone(0, 0, BLACK, false));
        assertNotNull(board.setStone(1, 1, WHITE, false));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));

        board.removeStone(1, 1);
        assertNull(board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(0, 0));

        game.pass(); // for maximum code-coverage
        board.removeStone(0, 0);
        assertNull(board.getColorAt(0, 0));
    }

    @Test
    void getSize() {
        assertEquals(19, board.getSize());
    }

    @Test
    void getColorAt() {
        assertNotNull(board.setStone(0, 0, BLACK, false));
        assertEquals(BLACK, board.getColorAt(0, 0));
    }

    @Test
    void printDebugInfo() {
        assertNotNull(board.setStone(0, 0, BLACK, false));
        assertDoesNotThrow(() -> board.printDebugInfo(0, 0));
    }

    @Test
    void printDebugInfoRepeatedly() { // This method  really only exists for maximising branch coverage.
        assertDoesNotThrow(() -> board.printDebugInfo(0, 0)); // board == null
        assertNotNull(board.setStone(0, 0, BLACK, false));
        assertDoesNotThrow(() -> board.printDebugInfo(0, 0)); // x == lastDebugX && y == lastDebugY
        assertNotNull(board.setStone(0, 1, BLACK, false));
        assertDoesNotThrow(() -> board.printDebugInfo(0, 1)); // x == lastDebugX && y != lastDebugY
        assertDoesNotThrow(() -> board.printDebugInfo(1, 1)); // x != lastDebugX && [y == lastDebugY]
    }

    @Test
    void simpleUndoTest() {
        board.setStone(1, 0, BLACK, false);
        board.setStone(1, 2, BLACK, false);
        board.setStone(0, 1, BLACK, false);
        board.setStone(1, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c = board.setStone(2, 1, BLACK, false);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));

        c.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        c.execute(true);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));
    }

    @Test
    void simpleUndoTest2() {
        board.setStone(1, 0, BLACK, false);
        board.setStone(1, 2, BLACK, false);
        board.setStone(0, 1, BLACK, false);
        board.setStone(1, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c = board.setStone(2, 1, BLACK, false);
        assertNotNull(c);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));

        c.undo();
        assertNotNull(c);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c2 = board.setStone(2, 1, WHITE, false);
        assertNotNull(c2);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));

        c2.undo();
        assertNotNull(c2);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        c.execute(true);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));
    }

    @Test
    void simpleUndoTest3() {
        board.setStone(1, 0, BLACK, false);
        board.setStone(1, 2, BLACK, false);
        board.setStone(0, 1, BLACK, false);
        board.setStone(1, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c2 = board.setStone(2, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));

        c2.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c = board.setStone(2, 1, BLACK, false);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));

        c.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));
    }

    @Test
    void simpleUndoTest4() {
        board.setStone(1, 0, BLACK, false);
        board.setStone(1, 2, BLACK, false);
        board.setStone(0, 1, BLACK, false);
        board.setStone(1, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c2 = board.setStone(2, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));

        UndoableCommand c3 = board.setStone(3, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));

        c3.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));
        assertEquals(null, board.getColorAt(3, 1));

        c2.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));
        assertEquals(null, board.getColorAt(3, 1));

        UndoableCommand c = board.setStone(2, 1, BLACK, false);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));
        assertEquals(null, board.getColorAt(3, 1));

        c.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));
        assertEquals(null, board.getColorAt(3, 1));
    }

    @Test
    void complexUndoTest() {
        board.setStone(1, 0, BLACK, false);
        board.setStone(1, 2, BLACK, false);
        board.setStone(0, 1, BLACK, false);
        board.setStone(1, 1, WHITE, false);
        board.setStone(2, 0, BLACK, false);
        board.setStone(2, 2, BLACK, false);
        board.setStone(3, 0, BLACK, false);
        board.setStone(3, 2, BLACK, false);
        board.setStone(3, 1, WHITE, false);
        UndoableCommand c = board.setStone(4, 1, BLACK, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));

        UndoableCommand c2 = board.setStone(2, 1, BLACK, false);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(3, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));

        c2.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));
        assertEquals(null, board.getColorAt(2, 1));

        board.setStone(2, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1)); // due to suicide detection
        assertEquals(WHITE, board.getColorAt(3, 1));

        c.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));
        assertEquals(null, board.getColorAt(4, 1));

        UndoableCommand c3 = board.setStone(2, 1, WHITE, false);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));

        c3.undo();
        c.execute(true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));
        assertEquals(null, board.getColorAt(2, 1));
        assertEquals(BLACK, board.getColorAt(4, 1));

        c2.execute(true);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(3, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));
        assertEquals(BLACK, board.getColorAt(4, 1));
    }
}