package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;

class HistoryTest {

    Game game;

    History history;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK, 19, 0, new JapaneseRuleset());
        history = game.getHistory();
    }

    // Argument-Checking
    @Test
    void constructorArgs() {
        assertThrows(NullPointerException.class, () -> new History(null));
    }

    @Test
    void addNodeArgs() {
        assertThrows(NullPointerException.class, () -> history.addNode(null));
        assertThrows(IllegalArgumentException.class, () -> history.addNode(history.getCurrentNode()));
    }

    // Other tests
    @Test
    void rewind() {
        assertTrue(history.isAtBeginning());
        game.playMove(0, 0);
        assertFalse(history.isAtBeginning());
        history.rewind();
        assertTrue(history.isAtBeginning());
        assertNull(game.getColorAt(0, 0));
    }

    @Test
    void skipToEnd() {
        assertTrue(history.isAtEnd());
        rewind();
        assertFalse(history.isAtEnd());
        history.skipToEnd();
        assertTrue(history.isAtEnd());
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void stepBack() {
        assertTrue(history.isAtEnd());
        skipToEnd();
        history.stepBack();
        assertTrue(history.isAtBeginning());
        assertFalse(history.isAtEnd());
        assertNull(game.getColorAt(0 ,0));
    }

    @Test
    void stepForward() {
        assertTrue(history.isAtBeginning());
        stepBack();
        history.stepForward();
        assertTrue(history.isAtEnd());
        assertFalse(history.isAtBeginning());
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void addNode() {
    }

    @Test
    void getCurrentNode() {
        History.HistoryNode first = history.getCurrentNode();
        game.playMove(0, 0);
        assertNotEquals(first, history.getCurrentNode());
        history.stepBack();
        assertEquals(first, history.getCurrentNode());
    }

    @Test
    void isAtEnd() {
        assertTrue(history.isAtEnd());
        assertTrue(history.isAtBeginning());
        history.rewind();
        assertTrue(history.isAtEnd());
        assertTrue(history.isAtBeginning());
        game.playMove(0, 0);
        assertTrue(history.isAtEnd());
        assertFalse(history.isAtBeginning());
        history.stepBack();
        assertTrue(history.isAtBeginning());
        assertFalse(history.isAtEnd());
    }

    @Test
    void isAtBeginning() {
    }

    @Test
    void testEquals() {
        game.playMove(0, 0);
        game.playMove(1, 1);
        game.playMove(2, 2);

        game.newGame(BLACK, 19, 0, new JapaneseRuleset());
        game.playMove(0, 0);
        assertNotEquals(history, game.getHistory());
        game.playMove(1, 1);
        assertNotEquals(history, game.getHistory());
        game.playMove(2, 2);
        assertEquals(history, game.getHistory());
        game.playMove(3, 3);
        assertNotEquals(history, game.getHistory());
        game.playMove(4, 4);
        assertNotEquals(history, game.getHistory());

        game.newGame(WHITE, 19, 0, new JapaneseRuleset());
        game.playMove(0, 0);
        game.playMove(1, 1);
        game.playMove(2, 2);
        assertNotEquals(history, game.getHistory());

        assertEquals(history, history);
        assertNotEquals(history, null);
        assertNotEquals(history, new Object());
    }

    @Test
    void testHashCode() {
        game.playMove(0, 0);
        HashSet<History> testSet = new HashSet<>();
        testSet.add(history);
        History otherHistory = new History(new Game());
        assertFalse(testSet.contains(otherHistory));
        History yetAnotherHistory = new History(game);
        assertFalse(testSet.contains(yetAnotherHistory));
        assertTrue(testSet.contains(history));

        Game otherGame = new Game();
        otherGame.newGame(BLACK, 19, 0, new JapaneseRuleset());
        otherGame.playMove(0, 0);
        assertFalse(testSet.contains(otherGame.getHistory()));
    }

    @Test
    void iterator() {
        game.newGame(BLACK, 19, 2, new JapaneseRuleset());
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.setSetupMode(false);
        game.playMove(1, 1);
        game.pass();

        Iterator<History.HistoryNode> iter = game.getHistory().iterator();
        assertTrue(iter.hasNext());
        assertEquals(History.HistoryNode.AbstractSaveToken.HANDICAP, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(History.HistoryNode.AbstractSaveToken.HANDICAP, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(History.HistoryNode.AbstractSaveToken.SETUP, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(History.HistoryNode.AbstractSaveToken.MOVE, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(History.HistoryNode.AbstractSaveToken.PASS, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(History.HistoryNode.AbstractSaveToken.END_OF_HISTORY, iter.next().getSaveToken());

        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    void forEach() {
        game.playMove(0, 0);
        game.setComment("foo");
        game.playMove(1, 1);
        game.setComment("foo");
        history.forEach(n -> {
            if(n.getSaveToken() != History.HistoryNode.AbstractSaveToken.END_OF_HISTORY) {
                assertEquals("foo", n.getComment());
            } else {
                assertEquals("", n.getComment());
            }
        });
    }

    @Test
    void spliterator() {
        assertNotNull(history.spliterator());
    }

    @Test
    void testToString() {
        History.HistoryNode lastNode = null;
        for(History.HistoryNode h : history) {
            lastNode = h;
        }
        assertEquals("History \n" + lastNode + "\n", history.toString());
        History.HistoryNode firstNode = new History.HistoryNode(null, History.HistoryNode.AbstractSaveToken.PASS, BLACK, "foo");
        history.addNode(firstNode);
        assertEquals("History \n" + firstNode + "\n" + lastNode + "\n", history.toString());
        History.HistoryNode secondNode = new History.HistoryNode(null, History.HistoryNode.AbstractSaveToken.PASS, WHITE, "bar");
        history.addNode(secondNode);
        assertEquals("History \n" + firstNode + "\n" + secondNode + "\n" + lastNode + "\n", history.toString());
    }
}