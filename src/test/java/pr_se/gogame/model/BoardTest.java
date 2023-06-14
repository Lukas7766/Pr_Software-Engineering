package pr_se.gogame.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static pr_se.gogame.model.StoneColor.*;

class BoardTest {
    Game game;
    Board board;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK, 19, 0, new JapaneseRuleset());
        board = new Board(game);
    }

    @AfterEach
    void tearDown() {
    }

    // argument checking
    @Test
    void setStoneArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.setStone(-1, 0, BLACK, false, true));
        assertThrows(IllegalArgumentException.class, () -> board.setStone(board.getSize(), 0, BLACK, false, true));
        assertThrows(IllegalArgumentException.class, () -> board.setStone(0, -1, BLACK, false, true));
        assertThrows(IllegalArgumentException.class, () -> board.setStone(0, board.getSize(), BLACK, false, true));
        assertThrows(NullPointerException.class, () -> board.setStone(0, 0, null, false, true));
    }

    @Test
    void removeStoneArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(-1, 0, true));
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(0, -1, true));
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(board.getSize(), 0, true));
        assertThrows(IllegalArgumentException.class, () -> board.removeStone(0, board.getSize(), true));
    }

    @Test
    void getColorAtArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(0, -1));
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(board.getSize(), 0));
        assertThrows(IllegalArgumentException.class, () -> board.getColorAt(0, board.getSize()));
    }

    @Test
    void printDebugInfoArguments() {
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(0, -1));
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(board.getSize(), 0));
        assertThrows(IllegalArgumentException.class, () -> board.printDebugInfo(0, board.getSize()));
    }

    // other tests
    @Test
    void setStonePrepare() {
        assertNotNull(board.setStone(0, 0, BLACK, true, true));
        assertNotNull(board.setStone(1, 1, WHITE, true, true));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));
    }

    @Test
    void setStoneMove() {
        assertNotNull(board.setStone(0, 0, BLACK, false, true));
        assertNotNull(board.setStone(1, 1, WHITE, false, true));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));
    }

    @Test
    void noSetStoneOnSameSpace() {
        assertNotNull(board.setStone(0, 0, BLACK, false, true));
        assertNull(board.setStone(0, 0, BLACK, false, true));
    }

    @Test
    void groupMergingAndCapture() {
        assertNotNull(board.setStone(10, 9, BLACK, false, true));
        assertNotNull(board.setStone(10, 11, BLACK, false, true));
        assertNotNull(board.setStone(10, 10, BLACK, false, true));

        assertNotNull(board.setStone(9, 9, WHITE, false, true));
        assertNotNull(board.setStone(9, 10, WHITE, false, true));
        assertNotNull(board.setStone(9, 11, WHITE, false, true));
        assertNotNull(board.setStone(11, 9, WHITE, false, true));
        assertNotNull(board.setStone(11, 10, WHITE, false, true));
        assertNotNull(board.setStone(11, 11, WHITE, false, true));
        assertNotNull(board.setStone(10, 8, WHITE, false, true));

        assertEquals(BLACK, board.getColorAt(10, 11));

        assertNotNull(board.setStone(10, 12, WHITE, false, true));

        assertNull(board.getColorAt(10, 11));
    }

    // TODO: Move to GameTest
    @Test
    void setStoneKoPrevention() {
        game.newGame(BLACK, 19, 0, new JapaneseRuleset());

        assertNotNull(board.setStone(9, 1, BLACK, false, true));
        assertNotNull(board.setStone(10, 1, WHITE, false, true));
        assertNotNull(board.setStone(9, 3, BLACK, false, true));
        assertNotNull(board.setStone(10, 3, WHITE, false, true));
        assertNotNull(board.setStone(8, 2, BLACK, false, true));
        assertNotNull(board.setStone(11, 2, WHITE, false, true));

        assertNotNull(board.setStone(9, 2, WHITE, false, true));
        assertNotNull(board.setStone(10, 2, BLACK, false, true));

        assertNull(board.setStone(9, 2, WHITE, false, true));
        assertNull(board.setStone(9, 2, WHITE, false, true)); // for maximum code coverage
        assertNotNull(board.setStone(12, 2, WHITE, false, true)); // for maximum code coverage
    }

    @Test
    void setStone() {
        assertNotNull(board.setStone(0, 1, BLACK, false, false)); // save = false for max. code coverage
        assertNotNull(board.setStone(1, 0, WHITE, false, false));
        assertNotNull(board.setStone(board.getSize() - 1, board.getSize() - 2, BLACK, false, false));
        assertNotNull(board.setStone(board.getSize() - 2, board.getSize() - 1, WHITE, false, false));

        assertEquals(BLACK, board.getColorAt(0, 1));
        assertEquals(WHITE, board.getColorAt(1, 0));
        assertEquals(BLACK, board.getColorAt(board.getSize() - 1, board.getSize() - 2));
        assertEquals(WHITE, board.getColorAt(board.getSize() - 2, board.getSize() - 1));
    }

    @Test
    void setStoneSuicidePreventionIfNotAllowed() {
        assertNotNull(board.setStone(9, 1, BLACK, false, true));
        assertNotNull(board.setStone(9, 3, BLACK, false, true));
        assertNotNull(board.setStone(8, 2, BLACK, false, true));
        assertNotNull(board.setStone(10, 2, BLACK, false, true));

        assertNull(board.setStone(9, 2, WHITE, false, true));
        assertNotNull(board.setStone(9, 2, BLACK, false, true));
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
        assertNotNull(board.setStone(0, 0, BLACK, false, true));
        assertNotNull(board.setStone(1, 1, WHITE, false, true));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));

        board.removeStone(1, 1, true);
        assertNull(board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(0, 0));

        game.pass(); // for maximum code-coverage
        board.removeStone(0, 0, true);
        assertNull(board.getColorAt(0, 0));
    }

    @Test
    void getSize() {
        assertEquals(19, board.getSize());
    }

    @Test
    void getColorAt() {
        assertNotNull(board.setStone(0, 0, BLACK, false, true));
        assertEquals(BLACK, board.getColorAt(0, 0));
    }

    @Test
    void printDebugInfo() {
        assertNotNull(board.setStone(0, 0, BLACK, false, true));
        assertDoesNotThrow(() -> board.printDebugInfo(0, 0));
    }

    @Test
    void printDebugInfoRepeatedly() { // This method  really only exists for maximising branch coverage.
        assertDoesNotThrow(() -> board.printDebugInfo(0, 0)); // board == null
        assertNotNull(board.setStone(0, 0, BLACK, false, true));
        assertDoesNotThrow(() -> board.printDebugInfo(0, 0)); // x == lastDebugX && y == lastDebugY
        assertNotNull(board.setStone(0, 1, BLACK, false, true));
        assertDoesNotThrow(() -> board.printDebugInfo(0, 1)); // x == lastDebugX && y != lastDebugY
        assertDoesNotThrow(() -> board.printDebugInfo(1, 1)); // x != lastDebugX && [y == lastDebugY]
    }

    @Test
    void simpleUndoTest() {
        board.setStone(1, 0, BLACK, false, true);
        board.setStone(1, 2, BLACK, false, true);
        board.setStone(0, 1, BLACK, false, true);
        board.setStone(1, 1, WHITE, false, true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c = board.setStone(2, 1, BLACK, false, true);
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
        board.setStone(1, 0, BLACK, false, true);
        board.setStone(1, 2, BLACK, false, true);
        board.setStone(0, 1, BLACK, false, true);
        board.setStone(1, 1, WHITE, false, true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c = board.setStone(2, 1, BLACK, false, true);
        assertNotNull(c);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));

        c.undo();
        assertNotNull(c);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c2 = board.setStone(2, 1, WHITE, false, true);
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
        board.setStone(1, 0, BLACK, false, true);
        board.setStone(1, 2, BLACK, false, true);
        board.setStone(0, 1, BLACK, false, true);
        board.setStone(1, 1, WHITE, false, true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c2 = board.setStone(2, 1, WHITE, false, true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));

        c2.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c = board.setStone(2, 1, BLACK, false, true);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));

        c.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));
    }

    @Test
    void simpleUndoTest4() {
        board.setStone(1, 0, BLACK, false, true);
        board.setStone(1, 2, BLACK, false, true);
        board.setStone(0, 1, BLACK, false, true);
        board.setStone(1, 1, WHITE, false, true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1));

        UndoableCommand c2 = board.setStone(2, 1, WHITE, false, true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(2, 1));

        UndoableCommand c3 = board.setStone(3, 1, WHITE, false, true);
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

        UndoableCommand c = board.setStone(2, 1, BLACK, false, true);
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
        board.setStone(1, 0, BLACK, false, true);
        board.setStone(1, 2, BLACK, false, true);
        board.setStone(0, 1, BLACK, false, true);
        board.setStone(1, 1, WHITE, false, true);
        board.setStone(2, 0, BLACK, false,true);
        board.setStone(2, 2, BLACK, false,true);
        board.setStone(3, 0, BLACK, false,true);
        board.setStone(3, 2, BLACK, false,true);
        board.setStone(3, 1, WHITE, false, true);
        UndoableCommand c = board.setStone(4, 1, BLACK, false,true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));

        UndoableCommand c2 = board.setStone(2, 1, BLACK, false, true);
        assertEquals(null, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(3, 1));
        assertEquals(BLACK, board.getColorAt(2, 1));

        c2.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));
        assertEquals(null, board.getColorAt(2, 1));

        board.setStone(2, 1, WHITE, false, true);
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(null, board.getColorAt(2, 1)); // due to suicide detection
        assertEquals(WHITE, board.getColorAt(3, 1));

        c.undo();
        assertEquals(WHITE, board.getColorAt(1, 1));
        assertEquals(WHITE, board.getColorAt(3, 1));
        assertEquals(null, board.getColorAt(4, 1));

        UndoableCommand c3 = board.setStone(2, 1, WHITE, false, true);
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