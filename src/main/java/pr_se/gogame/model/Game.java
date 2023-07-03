package pr_se.gogame.model;

import pr_se.gogame.model.file.FileHandler;
import pr_se.gogame.model.file.SGFFileHandler;
import pr_se.gogame.model.helper.GameCommand;
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;
import pr_se.gogame.model.ruleset.GameResult;
import pr_se.gogame.model.ruleset.JapaneseRuleset;
import pr_se.gogame.model.ruleset.Ruleset;
import pr_se.gogame.view_controller.observer.GameEvent;
import pr_se.gogame.view_controller.observer.GameListener;

import java.util.LinkedList;
import java.util.List;

import static pr_se.gogame.model.helper.StoneColor.BLACK;

public class Game implements GameInterface {

    /**
     * max size of custom board
     */
    public static final int MAX_CUSTOM_BOARD_SIZE = 25;

    /**
     * min size of custom board
     */
    public static final int MIN_CUSTOM_BOARD_SIZE = 5;

    /**
     * max handicap amount
     */
    public static final int MAX_HANDICAP_AMOUNT = 9;

    /**
     * min handicap amount
     */
    public static final int MIN_HANDICAP_AMOUNT = 0;

    //Settings
    private Ruleset ruleset = new JapaneseRuleset();
    private int handicap = 0;

    private GameState gameState;
    private final List<GameListener> listeners;
    private Board board;
    private int curMoveNumber;
    private StoneColor curColor;
    private int handicapStoneCounter;
    private double playerBlackScore;
    private int blackCapturedStones;

    private double playerWhiteScore;
    private int whiteCapturedStones;
    private GameResult gameResult;

    private History history;

    private boolean setupMode;

    private FileHandler fileHandler;

    public Game() {
        this.listeners = new LinkedList<>();
        this.gameState = GameState.NOT_STARTED_YET;
        this.board = new Board(this, 19);
        this.fileHandler = new SGFFileHandler(this);
    }

    @Override
    public void initGame() {
        gameState = GameState.NOT_STARTED_YET;
        this.history = null;
        this.fileHandler = new SGFFileHandler(this);

        fireGameEvent(new GameEvent(GameCommand.INIT));
    }

    @Override
    public void newGame(StoneColor startingColor, int size, int handicap, Ruleset ruleset) {
        newGame(startingColor, size, handicap, ruleset, true);
    }

    @Override
    public void newGame(StoneColor startingColor, int size, int handicap, Ruleset ruleset, boolean letRulesetPlaceHandicapStones) {
        if(size < MIN_CUSTOM_BOARD_SIZE || handicap < MIN_HANDICAP_AMOUNT || handicap > MAX_HANDICAP_AMOUNT) {
            throw new IllegalArgumentException();
        }

        if(startingColor == null) {
            throw new NullPointerException();
        }

        this.setupMode = false;
        this.gameState = GameState.SETTING_UP;

        this.history = new History(this);

        this.curColor = startingColor;
        this.handicap = handicap;
        this.ruleset = ruleset;

        this.playerBlackScore = handicap;
        this.playerWhiteScore = this.ruleset.getKomi();
        this.blackCapturedStones = 0;
        this.whiteCapturedStones = 0;
        this.curMoveNumber = 0;
        this.gameResult = null;

        this.board = new Board(this, size);
        this.ruleset.reset();
        fireGameEvent(new GameEvent(GameCommand.NEW_GAME));

        int tempHandicap = 0;
        if(letRulesetPlaceHandicapStones) {
            tempHandicap = this.handicap;
        }

        this.handicapStoneCounter = this.handicap;

        if(!letRulesetPlaceHandicapStones) {
            gameState = GameState.SETTING_UP;
        }

        boolean ruleSetHasAutoPlacement = this.ruleset.setHandicapStones(this, this.curColor, tempHandicap);

        if(handicapStoneCounter == 0 || (handicapStoneCounter == 1 && ruleSetHasAutoPlacement)) {
            if(handicapStoneCounter > 0) {
                handicapStoneCounter = 0;
            }
            gameState = GameState.RUNNING;
        } else if(handicapStoneCounter == 1) {
            gameState = GameState.SETTING_UP;
        }

        this.curMoveNumber = 1;

        fireGameEvent(new GameEvent(GameCommand.UPDATE));
    }

    @Override
    public void pass() {
        if(gameState != GameState.RUNNING && gameState != GameState.SETTING_UP) {
            throw new IllegalStateException("Can't pass on GameState " + gameState);
        }

        UndoableCommand c = switchColor();

        if(gameState != GameState.SETTING_UP) {
            history.addNode(new History.HistoryNode(c, History.HistoryNode.AbstractSaveToken.PASS, StoneColor.getOpposite(curColor), "")); // StoneColor.getOpposite() because we switched colors before
        }

        for (GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void resign() throws IllegalStateException {
        if(gameState != GameState.RUNNING) {
            if(gameState == GameState.SETTING_UP) {
                if(handicapStoneCounter >= 1) {
                    throw new IllegalStateException("Can't score game before all handicap stones have been set.");
                }
                throw new IllegalStateException("Can't score game when it is in setup mode!");
            }
            throw new IllegalStateException("Can't score game if it isn't running! gameState was " + gameState);
        }

        final GameResult oldGameResult = this.gameResult;
        final StoneColor finalCurColor = this.curColor;

        UndoableCommand c = new UndoableCommand() {
            @Override
            public void execute(final boolean saveEffects) {
                String msg =
                        "Game was resigned by " + finalCurColor + "!\n" +
                        "\n" +
                        StoneColor.getOpposite(finalCurColor) + " won!";
                gameResult = new GameResult(playerBlackScore, playerWhiteScore, StoneColor.getOpposite(finalCurColor), msg);
                gameState = GameState.GAME_OVER;
            }

            @Override
            public void undo() {
                gameResult = oldGameResult;
                gameState = GameState.RUNNING;
            }
        };
        c.execute(true);
        c.getExecuteEvents().add(new GameEvent(GameCommand.GAME_WON));
        c.getUndoEvents().add(new GameEvent(GameCommand.UPDATE));

        history.addNode(new History.HistoryNode(c, History.HistoryNode.AbstractSaveToken.RESIGN, finalCurColor, ""));

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void scoreGame() throws IllegalStateException {
        if(gameState != GameState.RUNNING) {
            if(gameState == GameState.SETTING_UP) {
                if(handicapStoneCounter >= 1) {
                    throw new IllegalStateException("Can't resign before all handicap stones have been set.");
                }
                throw new IllegalStateException("Can't resign when game is in setup mode!");
            }
            throw new IllegalStateException("Can't resign if game isn't running! gameState was " + gameState);
        }

        final GameResult oldGameResult = gameResult;
        final GameResult newGameResult = ruleset.scoreGame(this);

        final double oldBlackScore = playerBlackScore;
        final double newBlackScore = newGameResult.getScoreBlack();

        final double oldWhiteScore = playerWhiteScore;
        final double newWhiteScore = newGameResult.getScoreWhite();

        UndoableCommand c = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                gameResult = newGameResult;
                playerBlackScore = newBlackScore;
                playerWhiteScore = newWhiteScore;
                gameState = GameState.GAME_OVER;
            }

            @Override
            public void undo() {
                gameResult = oldGameResult;
                playerBlackScore = oldBlackScore;
                playerWhiteScore = oldWhiteScore;
                gameState = GameState.RUNNING;
            }
        };
        c.execute(true);
        c.getExecuteEvents().add(new GameEvent(GameCommand.GAME_WON));
        c.getUndoEvents().add(new GameEvent(GameCommand.UPDATE));
        c.getExecuteEvents().forEach(this::fireGameEvent);

        history.addNode(new History.HistoryNode(c, History.HistoryNode.AbstractSaveToken.SCORED_GAME, curColor, ""));
    }

    @Override
    public int getSize() {
        return board.getSize();
    }

    @Override
    public int getHandicap() {
        return this.handicap;
    }

    @Override
    public double getKomi() {
        return this.ruleset.getKomi();
    }

    @Override
    public void addListener(GameListener l) {
        if(l == null) {
            throw new NullPointerException();
        }
        listeners.add(l);
    }

    @Override
    public void removeListener(GameListener l) {
        if(l == null) {
            throw new NullPointerException();
        }
        listeners.remove(l);
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public int getCurMoveNumber() {
        return this.curMoveNumber;
    }

    @Override
    public StoneColor getCurColor() {
        return this.curColor;
    }

    @Override
    public Ruleset getRuleset() {
        return this.ruleset;
    }

    @Override
    public StoneColor getColorAt(int x, int y) {
        return board.getColorAt(x, y);
    }

    @Override
    public boolean playMove(int x, int y) {
        return playMove(x, y, this.curColor);
    }

    @Override
    public boolean playMove(int x, int y, StoneColor color) {
        if(gameState != GameState.RUNNING) {
            throw new IllegalStateException("Can't place stone when game isn't running! Game State was " + gameState);
        }

        checkCoords(x, y);

        if(color == null) {
            throw new NullPointerException();
        }

        List<UndoableCommand> subcommands = new LinkedList<>();

        final UndoableCommand uc01SetColor = setCurColor(color);
        subcommands.add(uc01SetColor);

        final UndoableCommand uc02SetStone = board.setStone(x, y, curColor, false); // uc02SetStone is already executed within board.setStone().

        if(uc02SetStone == null) {
            return false;
        }

        // Assertion: uc02SetStone != null and was hence a valid move.
        subcommands.add(uc02SetStone);

        final UndoableCommand uc03IsKo = ruleset.isKo(this);

        if(uc03IsKo == null) {
            uc02SetStone.undo();
            uc01SetColor.undo();
            return false;
        }

        subcommands.add(uc03IsKo);

        final int OLD_MOVE_NO = curMoveNumber;

        final UndoableCommand uc04SwitchColor = new UndoableCommand() {
            UndoableCommand thisCommand = null;

            @Override
            public void execute(final boolean saveEffects) {
                curMoveNumber++;
                // Update current player color
                thisCommand = switchColor();
                if(saveEffects) {
                    getExecuteEvents().addAll(thisCommand.getExecuteEvents());
                    getUndoEvents().addAll(thisCommand.getUndoEvents());
                }
            }

            @Override
            public void undo() {
                thisCommand.undo();
                curMoveNumber = OLD_MOVE_NO;
            }
        };
        uc04SwitchColor.execute(true);

        subcommands.add(uc04SwitchColor);

        UndoableCommand c = UndoableCommand.of(subcommands);
        // c was already executed piecemeal

        /*
         * StoneColor.getOpposite() because we previously switched colors
         */
        removeAllMarks();
        history.addNode(new History.HistoryNode(c, History.HistoryNode.AbstractSaveToken.MOVE, StoneColor.getOpposite(curColor), "", x, y));

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }

        return true;
    }

    @Override
    public void placeHandicapPosition(int x, int y, boolean placeStone) {
        placeHandicapPosition(x, y, placeStone, curColor);
    }

    @Override
    public void placeHandicapPosition(int x, int y, boolean placeStone, final StoneColor color) {
        if(gameState != GameState.SETTING_UP) {
            throw new IllegalStateException("Can't place handicap stone when game isn't being set up! Game state was " + gameState);
        }

        checkCoords(x, y);

        UndoableCommand c = null;

        if(placeStone) {
            if(color == null) {
                throw new NullPointerException();
            }

            if (handicapStoneCounter <= 0) {
                throw new IllegalStateException("Can't place any more handicap stones!");
            }

            List<UndoableCommand> subcommands = new LinkedList<>();

            final UndoableCommand uc01SetStone = board.setStone(x, y, color, true); // uc01SetStone is already executed within board.setStone().

            if(uc01SetStone == null) {
                return;
            }

            // Assertion: uc01SetStone != null and was hence a valid move.

            subcommands.add(uc01SetStone);

            final int oldHandicapCtr = handicapStoneCounter;
            final int newHandicapCtr = handicapStoneCounter - 1;

            final UndoableCommand uc02UpdateCounter = new UndoableCommand() {
                UndoableCommand uC02SwitchColor = null;

                @Override
                public void execute(final boolean saveEffects) {
                    handicapStoneCounter = newHandicapCtr;

                    if (newHandicapCtr <= 0) {
                        uC02SwitchColor = switchColor();
                        if(saveEffects) {
                            getExecuteEvents().addAll(uC02SwitchColor.getExecuteEvents());
                            getUndoEvents().addAll(uC02SwitchColor.getUndoEvents());
                        }

                        gameState = GameState.RUNNING;
                    }
                }

                @Override
                public void undo() {
                    if(uC02SwitchColor != null) {
                        uC02SwitchColor.undo();
                    }
                    handicapStoneCounter = oldHandicapCtr;

                    gameState = GameState.SETTING_UP;
                }
            };
            uc02UpdateCounter.execute(true);

            subcommands.add(uc02UpdateCounter);

            c = UndoableCommand.of(subcommands);
            // c was already executed piecemeal

            /*
             * StoneColor.getOpposite() because we previously switched colors
             */
            removeAllMarks();
            history.addNode(new History.HistoryNode(c, History.HistoryNode.AbstractSaveToken.HANDICAP, color, "", x, y));

            for(GameEvent e : c.getExecuteEvents()) {
                fireGameEvent(e);
            }
        }

        GameEvent handicapEvent = new GameEvent(GameCommand.HANDICAP_SET, x, y, curMoveNumber);

        if(c != null) {
            c.getExecuteEvents().add(handicapEvent);
            c.getUndoEvents().add(new GameEvent(GameCommand.HANDICAP_REMOVED, x, y, curMoveNumber));
        }

        fireGameEvent(handicapEvent);
    }

    @Override
    public void usePosition(int x, int y) {
        checkCoords(x, y);

        if(gameState == GameState.RUNNING) {
            playMove(x, y);
        } else if(gameState == GameState.SETTING_UP) {
            if(handicapStoneCounter > 0) {
                placeHandicapPosition(x, y, true);
            } else {
                placeSetupStone(x, y, curColor);
            }
        } else {
            throw new IllegalStateException("Can't place stone when game is neither running nor setting up!");
        }
    }


    @Override
    public void placeSetupStone(int x, int y, StoneColor color) {
        if(gameState != GameState.SETTING_UP) {
            throw new IllegalStateException("Can't place setup stone when gameState is " + gameState);
        }

        checkCoords(x, y);

        final int OLD_CUR_MOVE_NUMBER = curMoveNumber;
        curMoveNumber = 0;
        final UndoableCommand uc01SetStone = board.setStone(x, y, color, true); // uc01SetStone is already executed within board.setStone().
        curMoveNumber = OLD_CUR_MOVE_NUMBER;

        if(uc01SetStone == null) {
            return;
        }

        removeAllMarks();
        uc01SetStone.getExecuteEvents().add(new GameEvent(GameCommand.SETUP_STONE_SET, x, y, null, 0));
        history.addNode(new History.HistoryNode(uc01SetStone, History.HistoryNode.AbstractSaveToken.SETUP, color, "", x, y));

        for(GameEvent e : uc01SetStone.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public UndoableCommand addCapturedStones(final StoneColor color, final int amount) {
        if (color == null) {
            throw new NullPointerException();
        }
        if (amount < 0) {
            throw new IllegalArgumentException();
        }

        final int oldBlackCapturedStones = blackCapturedStones;
        final int oldWhiteCapturedStones = whiteCapturedStones;
        final double oldBlackPlayerScore = playerBlackScore;
        final double oldWhitePlayerScore = playerWhiteScore;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(final boolean saveEffects) {
                if (color == BLACK) {
                    Game.this.blackCapturedStones += amount;
                    Game.this.playerBlackScore += amount;
                } else {
                    Game.this.whiteCapturedStones += amount;
                    Game.this.playerWhiteScore += amount;
                }
            }

            @Override
            public void undo() {
                Game.this.blackCapturedStones = oldBlackCapturedStones;
                Game.this.whiteCapturedStones = oldWhiteCapturedStones;
                Game.this.playerBlackScore = oldBlackPlayerScore;
                Game.this.playerWhiteScore = oldWhitePlayerScore;
            }
        };
        ret.execute(true);

        return ret;
    }

    void fireGameEvent(GameEvent e) { // package-private by design
        if(e == null) {
            throw new NullPointerException();
        }

        for (GameListener l : listeners) {
            l.gameCommand(e);
        }
    }

    public void printDebugInfo(int x, int y) {
        board.printDebugInfo(x, y);
    }

    // Methods controlling the history
    @Override
    public void undo() {
        removeAllMarks();
        history.stepBack();
        reDisplayMarks();
    }

    @Override
    public void redo() {
        removeAllMarks();
        history.stepForward();
        reDisplayMarks();
    }

    @Override
    public void rewind() {
        removeAllMarks();
        if(!history.isAtBeginning()) {
            history.stepBack();
            if((history.getCurrentNode().getSaveToken() == History.HistoryNode.AbstractSaveToken.HANDICAP || history.getCurrentNode().getSaveToken() == History.HistoryNode.AbstractSaveToken.SETUP)) {
                history.rewind();
            } else if(!history.isAtBeginning()) {
                goBeforeFirstMove();
            }
        }
        reDisplayMarks();
    }

    @Override
    public void fastForward() {
        removeAllMarks();
        History.HistoryNode n = history.getCurrentNode();
        if(n.getSaveToken() == History.HistoryNode.AbstractSaveToken.BEGINNING_OF_HISTORY || n.getSaveToken() == History.HistoryNode.AbstractSaveToken.HANDICAP || n.getSaveToken() == History.HistoryNode.AbstractSaveToken.SETUP) {
            goBeforeFirstMove();
            if(history.getCurrentNode() == n) {
                history.skipToEnd();
            }
        } else {
            history.skipToEnd();
        }
        reDisplayMarks();
    }

    @Override
    public void goBeforeFirstMove() {
        goToFirstMove();
        removeAllMarks();
        if(history.getCurrentNode().getSaveToken() != History.HistoryNode.AbstractSaveToken.SETUP && history.getCurrentNode().getSaveToken() != History.HistoryNode.AbstractSaveToken.HANDICAP) {
            history.stepBack();
        }
        reDisplayMarks();
    }

    @Override
    public void goToFirstMove() {
        removeAllMarks();
        history.rewind();

        do {
            history.stepForward();
        } while(!history.isAtEnd() && (history.getCurrentNode().getSaveToken() == History.HistoryNode.AbstractSaveToken.HANDICAP || history.getCurrentNode().getSaveToken() == History.HistoryNode.AbstractSaveToken.SETUP));
        reDisplayMarks();
    }

    @Override
    public void goToEnd() {
        removeAllMarks();
        history.skipToEnd();
        reDisplayMarks();
    }

    @Override
    public FileHandler getFileHandler() {
        return this.fileHandler;
    }

    @Override
    public void mark(int x, int y, MarkShape shape) {
        history.getCurrentNode().addMark(x, y, shape);
        GameCommand gc = switch (shape) {
            case CIRCLE -> GameCommand.MARK_CIRCLE;
            case SQUARE -> GameCommand.MARK_SQUARE;
            case TRIANGLE -> GameCommand.MARK_TRIANGLE;
        };
        fireGameEvent(new GameEvent(gc, x, y, curMoveNumber));
    }

    @Override
    public void unmark(int x, int y) {
        checkCoords(x, y);

        history.getCurrentNode().removeMark(x, y);
        fireGameEvent(new GameEvent(GameCommand.UNMARK, x, y, curMoveNumber));
    }

    @Override
    public String getComment() {
        return history.getCurrentNode().getComment();
    }

    @Override
    public void setComment(String comment) {
        if(comment == null) {
            throw new NullPointerException();
        }
        history.getCurrentNode().setComment(comment);
        fireGameEvent(new GameEvent(GameCommand.UPDATE));
    }

    @Override
    public int getStonesCapturedBy(StoneColor color) {
        if (color == null) throw new NullPointerException();

        if (color == BLACK) {
            return this.blackCapturedStones;
        } else {
            return this.whiteCapturedStones;
        }
    }

    @Override
    public double getScore(StoneColor color) {
        if(color == null) {
            throw new NullPointerException();
        }

        return color == BLACK ? this.playerBlackScore : this.playerWhiteScore;
    }

    @Override
    public GameResult getGameResult() {
        return gameResult;
    }

    @Override
    public void setSetupMode(boolean setupMode) {
        if(gameState != GameState.RUNNING && gameState != GameState.SETTING_UP) {
            return;
        }

        if(handicapStoneCounter > 0) {
            return;
        }

        this.setupMode = setupMode;

        this.gameState = this.setupMode ? GameState.SETTING_UP : GameState.RUNNING;
    }

    @Override
    public boolean isSetupMode() {
        return setupMode;
    }

    public History getHistory() {
        return history;
    }


    // private methods

    private UndoableCommand switchColor() {
        UndoableCommand ret = new UndoableCommand() {
            UndoableCommand thisCommand;

            @Override
            public void execute(final boolean saveEffects) {
                thisCommand = setCurColor(StoneColor.getOpposite(curColor));
                if(saveEffects) {
                    getExecuteEvents().add(new GameEvent(GameCommand.UPDATE));
                    getUndoEvents().add(new GameEvent(GameCommand.UPDATE));
                }
            }

            @Override
            public void undo() {
                thisCommand.undo();
            }
        };
        ret.execute(true);

        return ret;

    }

    private UndoableCommand setCurColor(final StoneColor c) {
        if (c == null) {
            throw new NullPointerException();
        }

        final StoneColor oldColor = this.curColor;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(final boolean saveEffects) {
                Game.this.curColor = c;
            }

            @Override
            public void undo() {
                Game.this.curColor = oldColor;
            }
        };
        ret.execute(true);

        return ret;
    }

    private void removeAllMarks() {
        history.getCurrentNode().getMarks().forEach((key, value) -> fireGameEvent(new GameEvent(GameCommand.UNMARK, key.getX(), key.getY(), curMoveNumber)));
    }

    private void reDisplayMarks() {
        history.getCurrentNode().getMarks().forEach((key, value) -> mark(key.getX(), key.getY(), value));
    }

    private void checkCoords(int x, int y) {
        if(x < 0 || y < 0 || x >= board.getSize() || y >= board.getSize()) {
            throw new IllegalArgumentException("Invalid coordinates x = " + x + ", y = " + y);
        }
    }

    public enum GameState {
        NOT_STARTED_YET,
        SETTING_UP,
        RUNNING,
        GAME_OVER
    }
}

