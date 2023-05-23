package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.view_controller.DebugEvent;
import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

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

    @Test
    void setHandicapStoneCounterArguments() {
        assertThrows(IllegalArgumentException.class, () -> game.setHandicapStoneCounter(-1));
        assertThrows(IllegalArgumentException.class, () -> game.setHandicapStoneCounter(game.getHandicap() + 1));
    }

    @Test
    void setCurMoveNumberArguments() {
        assertThrows(IllegalArgumentException.class, () -> game.setCurMoveNumber(0));
    }

    @Test
    void setCurColorArguments() {
        assertThrows(NullPointerException.class, () -> game.setCurColor(null));
    }

    @Test
    void fireGameEventArguments() {
        assertThrows(NullPointerException.class, () -> game.fireGameEvent(null));
    }

    // other tests

    @Test
    void initGame() {
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(INIT, e.getGameCommand());
            }
        });
        game.initGame();
    }

    @Test
    void newGame() {
        game.newGame(WHITE_STARTS, 13, 1);
        assertEquals(WHITE, game.getCurColor());
        assertEquals(13, game.getSize());
        assertEquals(1, game.getHandicap());
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
    void passBlackStarts() {
        StoneColor prevColor = game.getCurColor();
        assertEquals(BLACK, prevColor);
        GameCommand prevState = game.getGameState();
        assertEquals(BLACK_STARTS, prevState);
        int prevMoveNumber = game.getCurMoveNumber();
        assertEquals(0, prevMoveNumber);

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertNotEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertNotEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        assertNotEquals(prevState, game.getGameState());
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
    }

    @Test
    void passWhiteStarts() {
        game.newGame(WHITE_STARTS, 19, 0);

        StoneColor prevColor = game.getCurColor();
        assertEquals(WHITE, prevColor);
        GameCommand prevState = game.getGameState();
        assertEquals(WHITE_STARTS, prevState);
        int prevMoveNumber = game.getCurMoveNumber();
        assertEquals(0, prevMoveNumber);

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertNotEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        prevColor = game.getCurColor();
        assertNotEquals(prevState, game.getGameState());
        prevState = game.getGameState();
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
        prevMoveNumber = game.getCurMoveNumber();

        game.pass();
        assertNotEquals(prevColor, game.getCurColor());
        assertNotEquals(prevState, game.getGameState());
        assertEquals(prevMoveNumber, game.getCurMoveNumber());
    }

    @Test
    void resignAtStart() {
        GameListener l1 = new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(WHITE_WON, e.getGameCommand());
            }
        };
        game.addListener(l1);
        game.resign();

        game.removeListener(l1);
        game.newGame(WHITE_STARTS, 19, 0);
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(BLACK_WON, e.getGameCommand());
            }
        });
        game.resign();

        assertThrows(IllegalStateException.class, () -> game.resign());
    }

    @Test
    void resignAtPlay() {
        GameListener l1 = new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(BLACK_WON, e.getGameCommand());
            }
        };
        game.playMove(0, 0);
        game.addListener(l1);
        game.resign();

        game.removeListener(l1);
        game.newGame(WHITE_STARTS, 19, 0);
        game.playMove(0, 0);
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(WHITE_WON, e.getGameCommand());
            }
        });
        game.resign();

        assertThrows(IllegalStateException.class, () -> game.resign());
    }

    @Test
    void scoreGame() {
        GameListener l1 = new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(WHITE_WON, e.getGameCommand());
            }
        };
        game.addListener(l1);
        game.scoreGame();

        game.removeListener(l1);
        game.newGame(BLACK_STARTS, 19, 9);
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(BLACK_WON, e.getGameCommand());
            }
        });
        game.scoreGame();
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
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(DEBUG_INFO, e.getGameCommand());
            }
        });
        game.printDebugInfo(0, 0);
    }

    @Test
    void removeListener() {
        GameListener l1 = new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(DEBUG_INFO, e.getGameCommand());
            }
        };
        game.addListener(l1);
        game.printDebugInfo(0, 0);

        game.removeListener(l1);
        game.playMove(0, 0);
    }

    @Test
    void getGameState() {
        assertEquals(BLACK_STARTS, game.getGameState());
        game.playMove(0, 0);
        assertEquals(WHITE_PLAYS, game.getGameState());
    }

    @Test
    void confirmChoice() {
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(CONFIRM_CHOICE, e.getGameCommand());
            }
        });
        game.confirmChoice();
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
        assertNotNull(game.getFileTree());
    }

    @Test
    void getColorAt() {
        assertNull(game.getColorAt(0, 0));
        game.playMove(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void setHandicapStoneCounter() {
        game.newGame(BLACK_STARTS, 19, 8);
        game.setHandicapStoneCounter(8);
        assertEquals(8, game.getHandicapStoneCounter());
    }

    @Test
    void getHandicapStoneCounter() {
        assertEquals(0, game.getHandicapStoneCounter());
    }

    @Test
    void setCurMoveNumber() {
        game.setCurMoveNumber(100);
        assertEquals(100, game.getCurMoveNumber());
    }

    @Test
    void setCurColor() {
        assertEquals(BLACK, game.getCurColor());
        assertEquals(BLACK_STARTS, game.getGameState());
        game.setCurColor(WHITE);
        assertEquals(WHITE, game.getCurColor());
        assertEquals(WHITE_PLAYS, game.getGameState());
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
        game.newGame(BLACK_STARTS, 19, 1); // The edge case that a handicap of 1 normally just means that Black starts, as usual, comes in really handy here.
        game.setHandicapStoneCounter(1);
        assertNull(game.getColorAt(0, 0));
        game.placeHandicapStone(0, 0);
        assertEquals(BLACK, game.getColorAt(0, 0));
    }

    @Test
    void newGameWithHandicap() {
        game.newGame(BLACK_STARTS, 19, 3);
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
        GameEvent ge = new DebugEvent(DEBUG_INFO, 0, 0, 0, 0);
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(ge, e);
            }
        });

        game.fireGameEvent(ge);
    }

    @Test
    void printDebugInfo() {
        game.addListener(new GameListener() {
            @Override
            public void gameCommand(GameEvent e) {
                assertEquals(DEBUG_INFO, e.getGameCommand());
            }
        });

        assertDoesNotThrow(() -> game.printDebugInfo(0, 0));
    }

    @Test
    void switchColor() {
        assertEquals(BLACK, game.getCurColor());
        assertEquals(BLACK_STARTS, game.getGameState());
        game.switchColor();
        assertEquals(WHITE, game.getCurColor());
        assertEquals(WHITE_PLAYS, game.getGameState());
    }
}