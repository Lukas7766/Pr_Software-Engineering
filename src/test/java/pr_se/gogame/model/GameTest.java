package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static pr_se.gogame.model.GameCommand.*;
import static pr_se.gogame.model.StoneColor.*;

class GameTest {
    Game game;
    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK_STARTS, 19, 0);
    }

    // argument-checking

    @Test
    void newGameArguments() {
        assertThrows(NullPointerException.class, () -> game.newGame(null, 19, 0));
        assertThrows(IllegalArgumentException.class, () -> game.newGame(DEBUG_INFO, 19, 0));
        assertThrows(IllegalArgumentException.class, () -> game.newGame(null, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.newGame(null, 19, -1));
        assertThrows(IllegalArgumentException.class, () -> game.newGame(null, 19, 10));
    }

    @Test
    void playMoveArguments() {
        assertThrows(IllegalArgumentException.class, () -> game.playMove(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(0, -1));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(game.getSize(), 0));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(0, game.getSize()));
    }

    @Test
    void placeHandicapStoneArguments() {
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapStone(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapStone(0, -1));
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapStone(game.getSize(), 0));
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapStone(0, game.getSize()));
    }

    @Test
    void getCapturedStonesArguments() {
        assertThrows(NullPointerException.class, () -> game.getStonesCapturedBy(null));
    }

    @Test
    void addCapturedStonesArguments() {
        assertThrows(NullPointerException.class, () -> game.addCapturedStones(null, 1));
        assertThrows(IllegalArgumentException.class, () -> game.addCapturedStones(BLACK, -1));
    }

    @Test
    void addListenerArguments() {
        assertThrows(NullPointerException.class, () -> game.addListener(null));
    }

    @Test
    void removeListenerArguments() {
        assertThrows(NullPointerException.class, () -> game.removeListener(null));
    }

    @Test
    void printDebugInfoArguments() {
        assertThrows(IllegalArgumentException.class, () -> game.printDebugInfo(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.printDebugInfo(0, -1));
        assertThrows(IllegalArgumentException.class, () -> game.printDebugInfo(game.getSize(), 0));
        assertThrows(IllegalArgumentException.class, () -> game.printDebugInfo(0, game.getSize()));
    }

    @Test
    void getScoreArguments() {
        assertThrows(NullPointerException.class, () -> game.getScore(null));
    }

    // other tests

    @Test
    void initGame() {
        fail();
    }

    @Test
    void newGame() {
        game.newGame(WHITE_STARTS, 13, 1);
        assertEquals(WHITE, game.getCurColor());
        assertEquals(13, game.getSize());
        assertEquals(1, game.getHandicap());
    }

    @Test
    void saveGame() {
        fail();
    }

    @Test
    void importGame() {
        fail();
    }

    @Test
    void exportGame() {
        fail();
    }

    @Test
    void saveFile() {
        fail();
    }

    @Test
    void importFile() {
        fail();
    }

    @Test
    void pass() {
        StoneColor prevColor = game.getCurColor();
        int prevMoveNumber = game.getCurMoveNumber();
        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
    }

    @Test
    void resign() {
        fail();
    }

    @Test
    void scoreGame() {
        fail();
    }

    @Test
    void getSize() {
        assertEquals(19, game.getSize());
    }

    @Test
    void getHandicap() {
        assertEquals(0, game.getHandicap());
    }

    @Test
    void getKomi() {
        assertEquals(game.getRuleset().getKomi(), game.getKomi());
    }

    @Test
    void addListener() {
        fail();
    }

    @Test
    void removeListener() {
        fail();
    }

    @Test
    void getGameState() {
        fail();
    }

    @Test
    void confirmChoice() {
        fail();
    }

    @Test
    void getBoard() {
        Board board = game.getBoard();
        assertNotNull(board);
        assertEquals(game.getSize(), board.getSize());
    }

    @Test
    void getCurMoveNumber() {
        assertEquals(0, game.getCurMoveNumber());
        game.playMove(0, 0);
        assertEquals(1, game.getCurMoveNumber());
    }

    @Test
    void getCurColor() {
        assertEquals(BLACK, game.getCurColor());
        game.playMove(0, 0);
        assertEquals(WHITE, game.getCurColor());
    }

    @Test
    void getRuleset() {
        Ruleset ruleset = game.getRuleset();
        assertNotNull(ruleset);
        assertInstanceOf(JapaneseRuleset.class, ruleset);
    }

    @Test
    void getFileSaver() {
        assertNotNull(game.getFileSaver());
    }

    @Test
    void getColorAt() {
        assertNull(game.getColorAt(0, 0));
        game.playMove(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void getHandicapStoneCounter() {
        fail();
    }

    @Test
    void setCurMoveNumber() {
        game.setCurMoveNumber(100);
        assertEquals(100, game.getCurMoveNumber());
    }

    @Test
    void setCurColor() {
        game.setCurColor(WHITE);
        assertEquals(WHITE, game.getCurColor());
    }

    @Test
    void playMove() {
        assertNull(game.getColorAt(0, 0));
        game.playMove(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void placeHandicapStone() {
        game.newGame(BLACK_STARTS, 19, 1); // The edge case that a handicap of 1 normally just means that Black starts, as usual, comes in really handy here.
        assertNull(game.getColorAt(0, 0));
        game.placeHandicapStone(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void placeHandicapStoneNotAllowed() {
        assertThrows(IllegalStateException.class, () -> game.placeHandicapStone(0, 0));
    }

    @Test
    void isDemoMode() {
        assertFalse(game.isDemoMode());
    }

    @Test
    void setDemoMode() {
        game.setDemoMode(true);
        assertTrue(game.isDemoMode());
    }

    @Test
    void setConfirmationNeeded() {
        game.setConfirmationNeeded(true);
        assertTrue(game.isConfirmationNeeded());
    }

    @Test
    void isConfirmationNeeded() {
        assertFalse(game.isConfirmationNeeded());
    }

    @Test
    void setShowMoveNumbers() {
        game.setShowMoveNumbers(true);
        assertTrue(game.isShowMoveNumbers());
    }

    @Test
    void isShowMoveNumbers() {
        assertFalse(game.isShowMoveNumbers());
    }

    @Test
    void setShowCoordinates() {
        game.setShowCoordinates(false);
        assertFalse(game.isShowCoordinates());
    }

    @Test
    void isShowCoordinates() {
        assertTrue(game.isShowCoordinates());
    }

    @Test
    void addCapturedStones() {
        assertEquals(0, game.getStonesCapturedBy(BLACK));
        assertEquals(0, game.getStonesCapturedBy(WHITE));

        game.addCapturedStones(BLACK, 10);
        assertEquals(10, game.getStonesCapturedBy(BLACK));
        assertEquals(0, game.getStonesCapturedBy(WHITE));

        game.addCapturedStones(WHITE, 20);
        assertEquals(10, game.getStonesCapturedBy(BLACK));
        assertEquals(20, game.getStonesCapturedBy(WHITE));

        game.addCapturedStones(BLACK, 30);
        assertEquals(40, game.getStonesCapturedBy(BLACK));
        assertEquals(20, game.getStonesCapturedBy(WHITE));

        game.addCapturedStones(WHITE, 40);
        assertEquals(40, game.getStonesCapturedBy(BLACK));
        assertEquals(60, game.getStonesCapturedBy(WHITE));
    }

    @Test
    void getStonesCapturedBy() {
        assertEquals(0, game.getStonesCapturedBy(WHITE));
        assertEquals(0, game.getStonesCapturedBy(BLACK));
        game.playMove(10, 1);
        game.playMove(10, 2);
        game.playMove(10, 3);
        game.playMove(18, 18); // superfluous white stone
        game.playMove(9, 2);
        game.playMove(17, 18); // superfluous white stone
        assertEquals(0, game.getStonesCapturedBy(WHITE));
        assertEquals(0, game.getStonesCapturedBy(BLACK));
        game.playMove(11, 2);
        assertEquals(0, game.getStonesCapturedBy(WHITE));
        assertEquals(1, game.getStonesCapturedBy(BLACK));

    }

    @Test
    void getScore() {
        fail();
    }

    @Test
    void getGameResult() {
        fail();
    }

    @Test
    void fireGameEvent() {
        fail();
    }

    @Test
    void printDebugInfo() {
        assertDoesNotThrow(() -> game.printDebugInfo(0, 0));
    }
}