package pr_se.gogame.model.ruleset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.helper.UndoableCommand;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;

class GameResultTest {

    GameResult gameResult;

    @BeforeEach
    void setUp() {
        gameResult = new GameResult();
    }

    // Argument-checking
    @Test
    void addScoreComponentArgs() {
        assertThrows(NullPointerException.class, () -> gameResult.addScoreComponent(null, GameResult.PointType.CAPTURED_STONES, 0));
        assertThrows(NullPointerException.class, () -> gameResult.addScoreComponent(BLACK, null, 0));
        assertThrows(NullPointerException.class, () -> gameResult.addScoreComponent(BLACK, GameResult.PointType.CAPTURED_STONES, null));
    }

    @Test
    void getScoreArgs() {
        assertThrows(NullPointerException.class, () -> gameResult.getScore(null));
    }

    @Test
    void getDescriptionArgs() {
        assertThrows(NullPointerException.class, () -> gameResult.getDescription(null));
    }

    // Other tests
    @Test
    void addScoreComponent() {
        assertFalse(gameResult.getScoreComponents(BLACK).containsKey(GameResult.PointType.CAPTURED_STONES));
        assertFalse(gameResult.getScoreComponents(BLACK).containsValue(1));
        UndoableCommand c1 = gameResult.addScoreComponent(BLACK, GameResult.PointType.CAPTURED_STONES, 1);
        Map<GameResult.PointType, Number> scoreComponents = gameResult.getScoreComponents(BLACK);
        assertTrue(scoreComponents.containsKey(GameResult.PointType.CAPTURED_STONES));
        assertTrue(scoreComponents.containsValue(1));

        UndoableCommand c2 = gameResult.addScoreComponent(BLACK, GameResult.PointType.CAPTURED_STONES, 2);
        scoreComponents = gameResult.getScoreComponents(BLACK);
        assertTrue(scoreComponents.containsKey(GameResult.PointType.CAPTURED_STONES));
        assertFalse(scoreComponents.containsValue(1));
        assertTrue(scoreComponents.containsValue(2));

        c2.undo();
        scoreComponents = gameResult.getScoreComponents(BLACK);
        assertTrue(scoreComponents.containsKey(GameResult.PointType.CAPTURED_STONES));
        assertFalse(scoreComponents.containsValue(2));
        assertTrue(scoreComponents.containsValue(1));

        c1.undo();
        assertFalse(gameResult.getScoreComponents(BLACK).containsKey(GameResult.PointType.CAPTURED_STONES));
        assertFalse(gameResult.getScoreComponents(BLACK).containsValue(1));
    }

    @Test
    void getScore() {
        assertEquals(0, gameResult.getScore(BLACK));
        assertEquals(0, gameResult.getScore(WHITE));
    }

    @Test
    void getDescription() {
        String emptyDesc = "\n\n= 0.0 points";

        assertEquals(emptyDesc, gameResult.getDescription(BLACK));
        assertEquals(emptyDesc, gameResult.getDescription(WHITE));
    }

    @Test
    void setDescription() {
        UndoableCommand c1 = gameResult.setDescription(BLACK, "BlackDesc");
        UndoableCommand c2 = gameResult.setDescription(WHITE, "WhiteDesc");

        String emptyDesc = "\n\n= 0.0 points";

        assertEquals("BlackDesc" + emptyDesc, gameResult.getDescription(BLACK));
        assertEquals("WhiteDesc" + emptyDesc, gameResult.getDescription(WHITE));

        c1.undo();

        assertEquals(emptyDesc, gameResult.getDescription(BLACK));
        assertEquals("WhiteDesc" + emptyDesc, gameResult.getDescription(WHITE));

        c2.undo();
        assertEquals(emptyDesc, gameResult.getDescription(BLACK));
        assertEquals(emptyDesc, gameResult.getDescription(WHITE));
    }

    @Test
    void getWinner() {
        assertNull(gameResult.getWinner());
    }

    @Test
    void setWinner() {
        getWinner();
        UndoableCommand c = gameResult.setWinner(BLACK);
        assertEquals(BLACK, gameResult.getWinner());

        c.undo();
        assertNull(gameResult.getWinner());
    }

    @Test
    void getScoreComponents() {
        assertNotNull(gameResult.getScoreComponents(BLACK));
        assertNotNull(gameResult.getScoreComponents(WHITE));
    }
}