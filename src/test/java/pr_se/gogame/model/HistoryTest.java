package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.helper.GameCommand;
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.helper.Position;
import pr_se.gogame.model.helper.UndoableCommand;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.History.HistoryNode.AbstractSaveToken.*;
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
        History.HistoryNode current = history.getCurrentNode();
        assertThrows(IllegalArgumentException.class, () -> history.addNode(current));
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
        assertTrue(history.isAtBeginning());
        assertTrue(history.isAtEnd());
        History.HistoryNode n = new History.HistoryNode(null, MOVE, game.getCurColor(), "");
        history.addNode(n);
        assertFalse(history.isAtBeginning());
        assertTrue(history.isAtEnd());
        assertEquals(n, history.getCurrentNode());
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
        assertTrue(history.isAtBeginning());
        game.playMove(0, 0);
        assertFalse(history.isAtBeginning());
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
        assertEquals(BEGINNING_OF_HISTORY, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(HANDICAP, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(HANDICAP, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(SETUP, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(MOVE, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(PASS, iter.next().getSaveToken());
        assertTrue(iter.hasNext());
        assertEquals(END_OF_HISTORY, iter.next().getSaveToken());

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
            if(n.getSaveToken() != END_OF_HISTORY && n.getSaveToken() != BEGINNING_OF_HISTORY) {
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
        StringBuilder expected = new StringBuilder("History \n");
        history.forEach(hn -> expected.append(hn.toString()).append("\n"));
        assertEquals(expected.toString(), history.toString());

        History.HistoryNode firstNode = new History.HistoryNode(null, PASS, BLACK, "foo");
        history.addNode(firstNode);
        StringBuilder expected2 = new StringBuilder("History \n");
        history.forEach(hn -> expected2.append(hn.toString()).append("\n"));
        assertEquals(expected2.toString(), history.toString());

        History.HistoryNode secondNode = new History.HistoryNode(null, PASS, WHITE, "bar");
        history.addNode(secondNode);
        StringBuilder expected3 = new StringBuilder("History \n");
        history.forEach(hn -> expected3.append(hn.toString()).append("\n"));
        assertEquals(expected3.toString(), history.toString());
    }

    // History.HistoryNode
    @Test
    void HistoryNodeConstructorArgs() {
        assertThrows(NullPointerException.class, () -> new History.HistoryNode(new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {

            }

            @Override
            public void undo() {

            }
        }, null, game.getCurColor(), ""));
    }

    @Test
    void HistoryNodeSetCommentArgs() {
        History.HistoryNode current = history.getCurrentNode();
        assertThrows(NullPointerException.class, () -> current.setComment(null));
    }

    @Test
    void HistoryNodeSetComment() {
        history.getCurrentNode().setComment("foo");
        assertEquals("foo", history.getCurrentNode().getComment());
        history.getCurrentNode().setComment("bar");
        assertEquals("bar", history.getCurrentNode().getComment());
    }

    @Test
    void HistoryNodeGetters() {
        game.playMove(3, 5);
        game.setComment("Test");

        assertEquals(3, history.getCurrentNode().getX());
        assertEquals(5, history.getCurrentNode().getY());
        assertEquals("Test", history.getCurrentNode().getComment());
        assertEquals(MOVE, history.getCurrentNode().getSaveToken());
        assertEquals(BLACK, history.getCurrentNode().getColor());
        assertNotNull(history.getCurrentNode().getCommand());
    }

    @Test
    void HistoryNodeAddMark() {
        History.HistoryNode n = history.getCurrentNode();
        n.addMark(0, 0, MarkShape.CIRCLE);
        assertEquals(1, n.getMarks().size());
        assertEquals(MarkShape.CIRCLE, n.getMarks().get(new Position(0, 0)));
    }

    @Test
    void HistoryNodeRemoveMark() {
        HistoryNodeAddMark();
        History.HistoryNode n = history.getCurrentNode();
        n.removeMark(0, 0);
        assertEquals(0, n.getMarks().size());
        assertNull(n.getMarks().get(new Position(0, 0)));
    }

    @Test
    void HistoryNodeEquals() {
        game.playMove(0, 0);

        assertEquals(history.getCurrentNode(), history.getCurrentNode());
        assertNotEquals(history.getCurrentNode(), null);
        assertNotEquals(history.getCurrentNode(), new Object());

        History.HistoryNode other = new History.HistoryNode(history.getCurrentNode().getCommand(), history.getCurrentNode().getSaveToken(), history.getCurrentNode().getColor(), "", history.getCurrentNode().getX(), history.getCurrentNode().getY());
        assertEquals(history.getCurrentNode(), other);
        other = new History.HistoryNode(null, history.getCurrentNode().getSaveToken(), history.getCurrentNode().getColor(), "", history.getCurrentNode().getX(), history.getCurrentNode().getY());
        assertEquals(history.getCurrentNode(), other); // The command shouldn't actually be compared, as too much could differ in semantically identical commands).

        other = new History.HistoryNode(history.getCurrentNode().getCommand(), PASS, history.getCurrentNode().getColor(), "", history.getCurrentNode().getX(), history.getCurrentNode().getY());
        assertNotEquals(history.getCurrentNode(), other);
        other = new History.HistoryNode(history.getCurrentNode().getCommand(), history.getCurrentNode().getSaveToken(), null, "", history.getCurrentNode().getX(), history.getCurrentNode().getY());
        assertNotEquals(history.getCurrentNode(), other);
        other = new History.HistoryNode(history.getCurrentNode().getCommand(), history.getCurrentNode().getSaveToken(), history.getCurrentNode().getColor(), "foo", history.getCurrentNode().getX(), history.getCurrentNode().getY());
        assertNotEquals(history.getCurrentNode(), other);
        other = new History.HistoryNode(history.getCurrentNode().getCommand(), history.getCurrentNode().getSaveToken(), history.getCurrentNode().getColor(), "", -1, history.getCurrentNode().getY());
        assertNotEquals(history.getCurrentNode(), other);
        other = new History.HistoryNode(history.getCurrentNode().getCommand(), history.getCurrentNode().getSaveToken(), history.getCurrentNode().getColor(), "", history.getCurrentNode().getX(), -1);
        assertNotEquals(history.getCurrentNode(), other);
        other = new History.HistoryNode(history.getCurrentNode().getCommand(), history.getCurrentNode().getSaveToken(), history.getCurrentNode().getColor(), "", history.getCurrentNode().getX(), history.getCurrentNode().getY());
        other.addMark(0, 0, MarkShape.TRIANGLE);
        assertNotEquals(history.getCurrentNode(), other);
    }
}