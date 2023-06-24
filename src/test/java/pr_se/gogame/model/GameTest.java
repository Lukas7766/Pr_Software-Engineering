package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.ruleset.JapaneseRuleset;
import pr_se.gogame.model.ruleset.Ruleset;
import pr_se.gogame.view_controller.DebugEvent;
import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import static org.junit.jupiter.api.Assertions.*;

import static pr_se.gogame.model.GameCommand.*;
import static pr_se.gogame.model.StoneColor.*;

class GameTest {
    Game game;

    /*
     * These variables are mostly meant for AssertThrows()-calls, as SonarQube (rightly) points out that nested method
     * calls might create ambiguity as to which method has thrown an expected (or unexpected) exception.
     */
    Ruleset ruleset;

    int maxCoord;

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
    }

    @Test
    void placeHandicapStoneArguments() {
        game.newGame(BLACK, 19, 1, ruleset);
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(-1, 0, true));
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(0, -1, true));
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(maxCoord + 1, 0, true));
        game.setHandicapStoneCounter(1);
        assertThrows(IllegalArgumentException.class, () -> game.placeHandicapPosition(0, maxCoord + 1, true));
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
        fail();
    }

    @Test
    void saveGame() {
        fail();
    }

    @Test
    void passBlackStarts() {
        StoneColor prevColor = game.getCurColor();
        assertEquals(BLACK, prevColor);
        GameState prevState = game.getGameState();
        assertEquals(GameState.RUNNING, prevState);
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
        GameState prevState = game.getGameState();
        assertEquals(GameState.RUNNING, prevState);
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
        assertEquals(GameState.RUNNING, game.getGameState());
        game.playMove(0, 0);
        assertEquals(GameState.RUNNING, game.getGameState());
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
    void dontPlaceHandicapStonesAfterStart() {
        game.playMove(0, 0);
        assertThrows(IllegalStateException.class, () -> game.placeHandicapPosition(1, 1, true));
    }

    @Test
    void dontPlayAfterGameIsOver() {
        game.resign();
        assertThrows(IllegalStateException.class, () -> game.playMove(0, 0));
    }
}