package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.helper.Position;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.ruleset.JapaneseRuleset;
import pr_se.gogame.model.ruleset.NewZealandRuleset;
import pr_se.gogame.model.ruleset.Ruleset;
import pr_se.gogame.view_controller.observer.DebugEvent;
import pr_se.gogame.view_controller.observer.GameEvent;
import pr_se.gogame.view_controller.observer.GameListener;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static pr_se.gogame.model.Game.GameState.RUNNING;
import static pr_se.gogame.model.Game.GameState.SETTING_UP;
import static pr_se.gogame.model.HistoryNode.AbstractSaveToken.*;
import static pr_se.gogame.model.helper.GameCommand.*;
import static pr_se.gogame.model.helper.StoneColor.*;

class GameTest {
    Game game;

    /*
     * These variables are mostly meant for AssertThrows()-calls, as SonarQube (rightly) points out that nested method
     * calls might create ambiguity as to which method has thrown an expected (or unexpected) exception.
     */
    Ruleset ruleset;

    int maxCoord;

    static final String TESTFILE_FOLDER = "./testFiles/";

    @BeforeEach
    void setUp() {
        game = new Game();
        ruleset = new JapaneseRuleset();
        game.newGame(BLACK, 19, 0, ruleset);
        maxCoord = game.getSize() - 1;
    }

    // argument-checking

    @Test
    void newGameArguments() {
        assertThrows(NullPointerException.class, () -> game.newGame(null, 19, 0, ruleset));
        assertThrows(IllegalArgumentException.class, () -> game.newGame(null, -1, 0, ruleset));
        assertThrows(IllegalArgumentException.class, () -> game.newGame(null, 19, -1, ruleset));
        assertThrows(IllegalArgumentException.class, () -> game.newGame(null, 19, 10, ruleset));
    }

    @Test
    void playMoveArguments() {
        assertThrows(IllegalArgumentException.class, () -> game.playMove(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(0, -1));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(maxCoord + 1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(0, maxCoord + 1));
        assertThrows(NullPointerException.class, () -> game.playMove(0, 0, null));
    }

    @Test
    void placeHandicapPositionArguments() {
        game.newGame(BLACK, 19, 1, ruleset);
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(-1, 0, true));
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(0, -1, true));
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(maxCoord + 1, 0, true));
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(0, maxCoord + 1, true));
        game.setHandicapStoneCounter(1);
        assertThrows(NullPointerException.class, () -> game.placeHandicapPosition(0, 0, true, null));
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
        assertThrows(IllegalArgumentException.class, () -> game.printDebugInfo(maxCoord + 1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.printDebugInfo(0, maxCoord + 1));
    }

    @Test
    void getScoreArguments() {
        assertThrows(NullPointerException.class, () -> game.getScore(null));
    }

    @Test
    void setHandicapStoneCounterArguments() {
        assertThrows(IllegalArgumentException.class, () -> game.setHandicapStoneCounter(Game.MAX_HANDICAP_AMOUNT + 1));
        assertThrows(IllegalArgumentException.class, () -> game.setHandicapStoneCounter(Game.MIN_HANDICAP_AMOUNT - 1));
    }

    @Test
    void fireGameEventArguments() {
        assertThrows(NullPointerException.class, () -> game.fireGameEvent(null));
    }

    @Test
    void saveGameArgs() {
        assertThrows(NullPointerException.class, () -> game.saveGame(null));
    }

    @Test
    void loadGameArgs() {
        assertThrows(NullPointerException.class, () -> game.loadGame(null));
    }

    @Test
    void usePositionArgs() {
        assertThrows(IllegalArgumentException.class, () -> game.usePosition(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.usePosition(0, -1));
        assertThrows(IllegalArgumentException.class, () -> game.usePosition(maxCoord + 1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.usePosition(0, maxCoord + 1));
    }

    @Test
    void placeSetupStoneArgs() {
        game.setSetupMode(true);
        assertThrows(IllegalArgumentException.class, () -> game.placeSetupStone(-1, 0, BLACK));
        assertThrows(IllegalArgumentException.class, () -> game.placeSetupStone(0, -1, BLACK));
        assertThrows(IllegalArgumentException.class, () -> game.placeSetupStone(maxCoord + 1, 0, BLACK));
        assertThrows(IllegalArgumentException.class, () -> game.placeSetupStone(0, maxCoord + 1, BLACK));
        assertThrows(NullPointerException.class, () -> game.placeSetupStone(0, 0, null));
    }

    @Test
    void setCommentArgs() {
        assertThrows(NullPointerException.class, () -> game.setComment(null));
    }

    // State Pattern
    @Test
    void dontPlaceHandicapStonesAfterStart() {
        game.playMove(0, 0);
        assertThrows(IllegalStateException.class, () -> game.placeHandicapPosition(1, 1, true));
    }

    @Test
    void dontUsePositionWhenGameOver() {
        assertNotPossibleWhenGameOver(() -> game.usePosition(0, 0));
    }

    @Test
    void dontPlayAfterGameIsOver() {
        assertNotPossibleWhenGameOver(() -> game.playMove(0, 0));
    }

    @Test
    void dontPlaceHandicapAfterGameOver() {
        assertNotPossibleWhenGameOver(() -> game.placeHandicapPosition(0, 0, true));
    }

    @Test
    void dontPlaceSetupAfterGameOver() {
        assertNotPossibleWhenGameOver(() -> game.placeSetupStone(0, 0, BLACK));
    }

    @Test
    void dontPassWhenGameIsOver() {
        assertNotPossibleWhenGameOver(() -> game.pass());
    }



    void assertNotPossibleWhenGameOver(Executable ex) {
        game.resign();
        assertThrows(IllegalStateException.class, ex);
    }

    // other tests

    @Test
    void initGame() {
        game.addListener(e -> assertEquals(INIT, e.getGameCommand()));
        game.initGame();
    }

    @Test
    void newGame() {
        game.newGame(WHITE, 13, 1, ruleset);
        assertEquals(WHITE, game.getCurColor());
        assertEquals(13, game.getSize());
        assertEquals(1, game.getHandicap());
    }

    @Test
    void loadGame() {
        saveGame();
        assertTrue(game.loadGame(new File("tmp.sgf")));
        game.goToEnd();
        assertEquals(BLACK, game.getColorAt(0, 0));
        assertEquals(WHITE, game.getColorAt(1, 0));
    }

    @Test
    void saveGame() {
        game.playMove(0, 0);
        game.playMove(1, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
        assertEquals(WHITE, game.getColorAt(1, 0));
        assertTrue(game.saveGame(new File("tmp.sgf")));
    }

    @Test
    void passBlackStarts() {
        StoneColor prevColor = game.getCurColor();
        assertEquals(BLACK, prevColor);
        Game.GameState prevState = game.getGameState();
        assertEquals(RUNNING, prevState);
        int prevMoveNumber = game.getCurMoveNumber();
        assertEquals(1, prevMoveNumber);

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        assertEquals(prevState, game.getGameState());
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
    }

    @Test
    void passWhiteStarts() {
        game.newGame(WHITE, 19, 0, ruleset);

        StoneColor prevColor = game.getCurColor();
        assertEquals(WHITE, prevColor);
        Game.GameState prevState = game.getGameState();
        assertEquals(RUNNING, prevState);
        int prevMoveNumber = game.getCurMoveNumber();
        assertEquals(1, prevMoveNumber);

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        assertEquals(prevState, game.getGameState());
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
    }

    @Test
    void passDuringSetup() {
        game.setSetupMode(true);
        assertEquals(1, game.getCurMoveNumber());
        assertDoesNotThrow(() -> game.pass());
        assertEquals(1, game.getCurMoveNumber());
    }

    @Test
    void resignAtStart() {
        GameListener l1 = e -> assertEquals(GAME_WON, e.getGameCommand());
        game.addListener(l1);
        game.resign();
        assertEquals(WHITE, game.getGameResult().getWinner());

        game.removeListener(l1);
        game.newGame(WHITE, 19, 0, ruleset);
        game.addListener(e -> assertEquals(GAME_WON, e.getGameCommand()));
        game.resign();
        assertEquals(BLACK, game.getGameResult().getWinner());

        assertThrows(IllegalStateException.class, () -> game.resign());
    }

    @Test
    void resignAtPlay() {
        GameListener l1 = e -> assertEquals(GAME_WON, e.getGameCommand());
        game.playMove(0, 0);
        game.addListener(l1);
        game.resign();
        assertEquals(BLACK, game.getGameResult().getWinner());

        game.removeListener(l1);
        game.newGame(WHITE, 19, 0, ruleset);
        game.playMove(0, 0);
        game.addListener(e -> assertEquals(GAME_WON, e.getGameCommand()));
        game.resign();
        assertEquals(WHITE, game.getGameResult().getWinner());

        assertThrows(IllegalStateException.class, () -> game.resign());
    }

    @Test
    void scoreGame() {
        GameListener l1 = e -> assertEquals(GAME_WON, e.getGameCommand());
        game.addListener(l1);
        game.scoreGame();
        assertEquals(WHITE, game.getGameResult().getWinner());

        game.removeListener(l1);
        game.newGame(BLACK, 19, 9, ruleset);
        game.addListener(e -> assertEquals(GAME_WON, e.getGameCommand()));
        game.scoreGame();
        assertEquals(BLACK, game.getGameResult().getWinner());
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
        game.addListener(e -> assertEquals(DEBUG_INFO, e.getGameCommand()));
        game.printDebugInfo(0, 0);
    }

    @Test
    void removeListener() {
        GameListener l1 = e -> assertEquals(DEBUG_INFO, e.getGameCommand());
        game.addListener(l1);
        game.printDebugInfo(0, 0);

        game.removeListener(l1);
        game.playMove(0, 0);
    }

    @Test
    void getGameState() {
        assertEquals(RUNNING, game.getGameState());
        game.playMove(0, 0);
        assertEquals(RUNNING, game.getGameState());
    }

    @Test
    void getCurMoveNumber() {
        assertEquals(1, game.getCurMoveNumber());
        game.playMove(0, 0);
        assertEquals(2, game.getCurMoveNumber());
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
    void getColorAt() {
        assertNull(game.getColorAt(0, 0));
        game.playMove(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void setHandicapStoneCounter() {
        game.setHandicapStoneCounter(1);
        assertDoesNotThrow(() -> game.placeHandicapPosition(0, 0, false));
        assertThrows(IllegalStateException.class, () -> game.placeHandicapPosition(0, 0, false));
    }

    @Test
    void playMove() {
        assertNull(game.getColorAt(0, 0));
        game.playMove(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void abortedMove() {
        assertNull(game.getColorAt(0, 0));
        game.playMove(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
        game.playMove(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void placeHandicapStone() {
        game.newGame(BLACK, 19, 1, ruleset); // The edge case that a handicap of 1 normally just means that Black starts, as usual, comes in really handy here.
        game.setHandicapStoneCounter(1);
        assertNull(game.getColorAt(0, 0));
        game.placeHandicapPosition(0, 0, true);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void newGameWithHandicap() {
        game.newGame(BLACK, 19, 3, ruleset);
        assertEquals(WHITE, game.getCurColor());
    }

    @Test
    void placeHandicapStoneNotAllowed() {
        assertThrows(IllegalStateException.class, () -> game.placeHandicapPosition(0, 0, true));
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
        assertEquals(0, game.getScore(BLACK));
        assertEquals(game.getRuleset().getKomi(), game.getScore(WHITE));
    }

    @Test
    void getGameResult() {
        game.resign();
        assertNotNull(game.getGameResult());
    }

    @Test
    void fireGameEvent() {
        GameEvent ge = new DebugEvent(0, 0, 0, 0);
        game.addListener(e -> assertEquals(ge, e));

        game.fireGameEvent(ge);
    }

    @Test
    void printDebugInfo() {
        game.addListener(e -> assertEquals(DEBUG_INFO, e.getGameCommand()));

        assertDoesNotThrow(() -> game.printDebugInfo(0, 0));
    }

    @Test
    void playMoveKoPrevention() {
        game.newGame(BLACK, 19, 0, new JapaneseRuleset());

        game.loadGame(new File(TESTFILE_FOLDER + "KoSituation.sgf"));
        game.fastForward();

        assertTrue(game.playMove(2, 1));
        assertFalse(game.playMove(1, 1));
    }

    @Test
    void handicapCtrButSetManually() {
        // Used by the FileHandler
        game.newGame(BLACK, 19, 0, new JapaneseRuleset(), false);

        assertEquals(RUNNING, game.getGameState());
    }

    @Test
    void newZealandRuleSet() {
        game.newGame(BLACK, 19, 9, new NewZealandRuleset());

        assertEquals(SETTING_UP, game.getGameState());
    }

    @Test
    void isSetupMode() {
        assertFalse(game.isSetupMode());
        game.setSetupMode(true);
        assertTrue(game.isSetupMode());
    }

    @Test
    void getComment() {
        game.setComment("foo");
        assertEquals("foo", game.getComment());
        game.setComment("bar");
        assertEquals("bar", game.getComment());
    }

    @Test
    void usePosition() {
        game.setHandicapStoneCounter(1);
        game.usePosition(1, 1);
        game.setSetupMode(true);
        game.usePosition(2, 2);
        game.setSetupMode(false);
        game.usePosition(3, 3);

        Iterator<HistoryNode> i = game.getHistory().iterator();
        assertEquals(HANDICAP, i.next().getSaveToken());
        assertEquals(SETUP, i.next().getSaveToken());
        assertEquals(MOVE, i.next().getSaveToken());
    }

    @Test
    void mark() {
        game.playMove(1, 1);
        Map<Position, MarkShape> mirrorImage = Collections.unmodifiableMap(game.getHistory().getCurrentNode().getMarks());
        assertEquals(0, mirrorImage.size());

        game.mark(1, 1, MarkShape.CIRCLE);
        assertEquals(1, mirrorImage.size());
        assertEquals(MarkShape.CIRCLE, mirrorImage.get(new Position(1, 1)));

        game.mark(2, 2, MarkShape.SQUARE);
        assertEquals(2, mirrorImage.size());
        assertEquals(MarkShape.SQUARE, mirrorImage.get(new Position(2, 2)));

        game.mark(3, 3, MarkShape.TRIANGLE);
        assertEquals(3, mirrorImage.size());
        assertEquals(MarkShape.TRIANGLE, mirrorImage.get(new Position(3, 3)));
    }

    @Test
    void unmark() {
        mark();
        Map<Position, MarkShape> mirrorImage = Collections.unmodifiableMap(game.getHistory().getCurrentNode().getMarks());

        game.unmark(1, 1);
        assertEquals(2, mirrorImage.size());
        assertNull(mirrorImage.get(new Position(1, 1)));

        game.unmark(3, 3);
        assertEquals(1, mirrorImage.size());
        assertNull(mirrorImage.get(new Position(3, 3)));

        game.unmark(2, 2);
        assertEquals(0, mirrorImage.size());
        assertNull(mirrorImage.get(new Position(2, 2)));
    }

    @Test
    void playMoveSuicide() {
        game.setSetupMode(true);
        game.placeSetupStone(1, 0, BLACK);
        game.placeSetupStone(0, 1, BLACK);
        game.placeSetupStone(2, 1, BLACK);
        game.placeSetupStone(1, 2, BLACK);
        game.setSetupMode(false);
        game.pass();
        assertFalse(game.playMove(1, 1));
    }

    @Test
    void tryOverridingHandicapPosition() {
        game.setHandicapStoneCounter(2);
        game.placeHandicapPosition(0, 0, true);
        assertEquals(SETTING_UP, game.getGameState());
        game.placeHandicapPosition(0, 0, true);
        assertEquals(SETTING_UP, game.getGameState());
        assertDoesNotThrow(() -> game.placeHandicapPosition(1, 1, true));
        assertEquals(RUNNING, game.getGameState());
    }

    @Test
    void tryOverridingSetupStone() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(0, 0, WHITE);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void rewind() {
        game.playMove(0, 0);
        assertFalse(game.getHistory().isAtBeginning());
        game.rewind();
        assertTrue(game.getHistory().isAtBeginning());
    }

    @Test
    void rewindWithSetup() {
        game.setSetupMode(true);
        game.placeSetupStone(0, 0, BLACK);
        game.placeSetupStone(4, 4, BLACK);
        game.setSetupMode(false);
        game.playMove(1, 1);
        game.playMove(2, 2);
        game.rewind();
        assertNull(game.getColorAt(2, 2));
        assertNull(game.getColorAt(1, 1));
        assertEquals(BLACK, game.getColorAt(0, 0));
        assertFalse(game.getHistory().isAtBeginning());
        game.rewind();
        assertNull(game.getColorAt(0, 0));
        assertNull(game.getColorAt(4, 4));
        assertTrue(game.getHistory().isAtBeginning());
    }

    @Test
    void rewindWithHandicap() {
        game.newGame(BLACK, 19, 9, new JapaneseRuleset(), true);
        assertFalse(game.getHistory().isAtBeginning());
        assertEquals(BLACK, game.getColorAt(3, 3));
        game.rewind();
        assertTrue(game.getHistory().isAtBeginning());
        assertNull(game.getColorAt(3, 3));
    }

    @Test
    void rewindAtBeginning() {
        assertTrue(game.getHistory().isAtBeginning());
        game.rewind();
        assertTrue(game.getHistory().isAtBeginning());
    }

    @Test
    void fastForward() {
        rewind();
        game.fastForward();
        assertTrue(game.getHistory().isAtEnd());
    }

    @Test
    void fastForwardWithSetup() {
        rewindWithSetup();
        game.fastForward();
        assertEquals(BLACK, game.getColorAt(4, 4));
        assertNull(game.getColorAt(1, 1));
        game.fastForward();
        assertEquals(BLACK, game.getColorAt(1, 1));
        assertEquals(WHITE, game.getColorAt(2, 2));
    }

    @Test
    void fastForwardWithHandicap() {
        rewindWithHandicap();
        game.fastForward();
        assertEquals(BLACK, game.getColorAt(3, 3));
    }

    @Test
    void fastForwardAtEnd() {
        assertTrue(game.getHistory().isAtEnd());
        game.fastForward();
        assertTrue(game.getHistory().isAtEnd());
    }

    // Command pattern
    @Test
    void undoPlayMove() {
        game.playMove(1, 1);
        assertEquals(BLACK, game.getColorAt(1, 1));
        game.undo();
        assertNull(game.getColorAt(1, 1));
    }

    @Test
    void redoPlayMove() {
        undoPlayMove();
        game.redo();
        assertEquals(BLACK, game.getColorAt(1, 1));
    }

    @Test
    void undoPlaceHandicapPosition() {
        game.setHandicapStoneCounter(1);
        assertEquals(SETTING_UP, game.getGameState());
        game.placeHandicapPosition(1, 1, true);
        assertEquals(BLACK, game.getColorAt(1, 1));
        assertEquals(RUNNING, game.getGameState());
        game.undo();
        assertNull(game.getColorAt(1, 1));
        assertEquals(SETTING_UP, game.getGameState());
    }

    @Test
    void redoPlaceHandicapPosition() {
        undoPlaceHandicapPosition();
        game.redo();
        assertEquals(BLACK, game.getColorAt(1, 1));
    }

    @Test
    void undoPlaceSetupStone() {
        game.setSetupMode(true);
        game.placeSetupStone(1, 1, BLACK);
        assertEquals(BLACK, game.getColorAt(1, 1));
        game.undo();
        assertNull(game.getColorAt(1, 1));
    }

    @Test
    void redoPlaceSetupStone() {
        undoPlaceSetupStone();
        game.redo();
        assertEquals(BLACK, game.getColorAt(1, 1));
    }

    @Test
    void undoResign() {
        game.resign();
        assertThrows(IllegalStateException.class, () -> game.resign());
        game.undo();
        assertDoesNotThrow(() -> game.resign());
    }

    @Test
    void redoResign() {
        undoResign();
        game.undo();
        game.redo();
        assertThrows(IllegalStateException.class, () -> game.resign());
    }
}