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
        game.newGame(GameCommand.BLACK_STARTS, 19, 0);
        board = game.getBoard();
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
        assertTrue(board.setStone(0, 0, BLACK, true, true));
        assertTrue(board.setStone(1, 1, WHITE, true, true));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));
    }

    @Test
    void setStoneMove() {
        assertTrue(board.setStone(0, 0, BLACK, false, true));
        assertTrue(board.setStone(1, 1, WHITE, false, true));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));
    }

    @Test
    void noSetStoneOnSameSpace() {
        assertTrue(board.setStone(0, 0, BLACK, false, true));
        assertFalse(board.setStone(0, 0, BLACK, false, true));
    }

    @Test
    void groupMergingAndCapture() {
        assertTrue(board.setStone(10, 9, BLACK, false, true));
        assertTrue(board.setStone(10, 11, BLACK, false, true));
        assertTrue(board.setStone(10, 10, BLACK, false, true));

        assertTrue(board.setStone(9, 9, WHITE, false, true));
        assertTrue(board.setStone(9, 10, WHITE, false, true));
        assertTrue(board.setStone(9, 11, WHITE, false, true));
        assertTrue(board.setStone(11, 9, WHITE, false, true));
        assertTrue(board.setStone(11, 10, WHITE, false, true));
        assertTrue(board.setStone(11, 11, WHITE, false, true));
        assertTrue(board.setStone(10, 8, WHITE, false, true));

        assertEquals(BLACK, board.getColorAt(10, 11));

        assertTrue(board.setStone(10, 12, WHITE, false, true));

        assertNull(board.getColorAt(10, 11));
    }

    @Test
    void setStoneKoPrevention() {
        assertTrue(board.setStone(9, 1, BLACK, false, true));
        assertTrue(board.setStone(10, 1, WHITE, false, true));
        assertTrue(board.setStone(9, 3, BLACK, false, true));
        assertTrue(board.setStone(10, 3, WHITE, false, true));
        assertTrue(board.setStone(8, 2, BLACK, false, true));
        assertTrue(board.setStone(11, 2, WHITE, false, true));

        assertTrue(board.setStone(9, 2, WHITE, false, true));
        assertTrue(board.setStone(10, 2, BLACK, false, true));

        assertFalse(board.setStone(9, 2, WHITE, false, true));
        assertFalse(board.setStone(9, 2, WHITE, false, true)); // for maximum code coverage
        assertTrue(board.setStone(12, 2, WHITE, false, true)); // for maximum code coverage
    }

    @Test
    void setStoneSuicidePreventionIfNotAllowed() {
        assertTrue(board.setStone(9, 1, BLACK, false, true));
        assertTrue(board.setStone(9, 3, BLACK, false, true));
        assertTrue(board.setStone(8, 2, BLACK, false, true));
        assertTrue(board.setStone(10, 2, BLACK, false, true));

        assertFalse(board.setStone(9, 2, WHITE, false, true));
        assertTrue(board.setStone(9, 2, BLACK, false, true));
    }

    /*@Test
    void setStoneSuicideAllowed() {
        assertTrue(board.setStone(9, 1, BLACK, false));
        assertTrue(board.setStone(10, 1, BLACK, false));
        assertTrue(board.setStone(9, 3, BLACK, false));
        assertTrue(board.setStone(10, 3, BLACK, false));
        assertTrue(board.setStone(8, 2, BLACK, false));
        assertTrue(board.setStone(11, 2, BLACK, false));

        assertTrue(board.setStone(9, 2, WHITE, false));
    }*/

    @Test
    void removeStone() {
        assertTrue(board.setStone(0, 0, BLACK, false, true));
        assertTrue(board.setStone(1, 1, WHITE, false, true));

        assertEquals(BLACK, board.getColorAt(0, 0));
        assertEquals(WHITE, board.getColorAt(1, 1));

        board.removeStone(1, 1, true);
        assertNull(board.getColorAt(1, 1));
        assertEquals(BLACK, board.getColorAt(0, 0));

        game.switchColor(); // for maximum code-coverage
        board.removeStone(0, 0, true);
        assertNull(board.getColorAt(0, 0));
    }

    @Test
    void getNeighbors() {
        assertTrue(board.setStone(9, 1, BLACK, false, true));
        assertTrue(board.setStone(9, 3, WHITE, false, true));
        assertTrue(board.setStone(8, 2, BLACK, false, true));
        assertTrue(board.setStone(10, 2, WHITE, false, true));

        assertEquals(4, board.getNeighbors(9, 2).size());
    }

    @Test
    void getSize() {
        assertEquals(19, board.getSize());
    }

    @Test
    void getColorAt() {
        assertTrue(board.setStone(0, 0, BLACK, false, true));
        assertEquals(BLACK, board.getColorAt(0, 0));
    }

    @Test
    void getGAME() {
        assertEquals(game, board.getGAME());
    }

    @Test
    void printDebugInfo() {
        assertTrue(board.setStone(0, 0, BLACK, false, true));
        assertDoesNotThrow(() -> board.printDebugInfo(0, 0));
    }
}