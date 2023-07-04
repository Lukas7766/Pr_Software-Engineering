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
import java.util.ListIterator;

import static pr_se.gogame.model.History.HistoryNode.AbstractSaveToken.*;
import static pr_se.gogame.model.helper.StoneColor.getOpposite;

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

    private GameResult gameResult;

    private History history;

    private boolean setupMode;

    private FileHandler fileHandler;

    public Game() {
        this.listeners = new LinkedList<>();
        this.gameState = GameState.NOT_STARTED_YET;
        this.board = new Board(this, 19);
        this.fileHandler = new SGFFileHandler(this);
        this.gameResult = new GameResult();
    }

    @Override
    public void initGame() {
        gameState = GameState.NOT_STARTED_YET;
        this.history = null;
        this.fileHandler = new SGFFileHandler(this);
        this.gameResult = new GameResult();

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

        this.curMoveNumber = 0;
        this.gameResult = new GameResult();
        this.gameResult.addScoreComponent(startingColor, GameResult.PointType.HANDICAP, handicap);
        this.gameResult.addScoreComponent(getOpposite(startingColor), GameResult.PointType.KOMI, ruleset.getKomi());

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
            history.addNode(new History.HistoryNode(c, PASS, StoneColor.getOpposite(curColor), "")); // StoneColor.getOpposite() because we switched colors before
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
                    throw new IllegalStateException("Can't resign before all handicap stones have been set.");
                }
                throw new IllegalStateException("Can't resign when game is in setup mode!");
            }
            throw new IllegalStateException("Can't resign if game isn't running! gameState was " + gameState);
        }

        endGame(RESIGN);
    }

    @Override
    public void scoreGame() throws IllegalStateException {
        if(gameState != GameState.RUNNING) {
            if(gameState == GameState.SETTING_UP) {
                if(handicapStoneCounter >= 1) {
                    throw new IllegalStateException("Can't score game before all handicap stones have been set.");
                }
                throw new IllegalStateException("Can't score game when it is in setup mode!");
            }
            throw new IllegalStateException("Can't score game if it isn't running! gameState was " + gameState);
        }

        endGame(SCORED_GAME);
    }

    private void endGame(History.HistoryNode.AbstractSaveToken saveToken) {
        if(saveToken == null) {
            throw new NullPointerException();
        }

        if(saveToken != SCORED_GAME && saveToken != RESIGN) {
            throw new IllegalArgumentException("AbstractSaveToken " + saveToken + " invalid for endGame() method");
        }

        List<UndoableCommand> subcommands = new LinkedList<>();

        subcommands.add(ruleset.scoreGame(this));
        if(saveToken == RESIGN) {
            subcommands.add(gameResult.setWinner(StoneColor.getOpposite(curColor)));
            subcommands.add(gameResult.setDescription(curColor, "Game was resigned by " + curColor + "!"));
            subcommands.add(gameResult.setDescription(StoneColor.getOpposite(curColor), StoneColor.getOpposite(curColor) + " won!"));
        }

        UndoableCommand c = UndoableCommand.updateValue(gs -> gameState = gs, GameState.RUNNING, GameState.GAME_OVER);
        c.execute(true);

        subcommands.add(c);

        UndoableCommand ret = UndoableCommand.of(subcommands);
        ret.getExecuteEvents().add(new GameEvent(GameCommand.GAME_WON));
        ret.getUndoEvents().add(new GameEvent(GameCommand.UPDATE));
        ret.getExecuteEvents().forEach(this::fireGameEvent);

        history.addNode(new History.HistoryNode(ret, saveToken, curColor, ""));
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

        placeStone(x, y, color, SETUP);
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

        if(placeStone) {
            if (handicapStoneCounter <= 0) {
                throw new IllegalStateException("Can't place any more handicap stones!");
            }

            placeStone(x, y, color, HANDICAP);
        } else {
            fireGameEvent(new GameEvent(GameCommand.HANDICAP_SET, x, y, curMoveNumber));
        }
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

        return placeStone(x, y, color, MOVE);
    }

    private boolean placeStone(final int x, final int y, final StoneColor color, final History.HistoryNode.AbstractSaveToken saveToken) {
        if (color == null || saveToken == null) {
            throw new NullPointerException();
        }

        if (saveToken != MOVE && saveToken != SETUP && saveToken != HANDICAP) {
            throw new IllegalArgumentException("AbstractSaveToken " + saveToken + " is incompatible with placeStone().");
        }

        checkCoords(x, y);

        List<UndoableCommand> subcommands = new LinkedList<>();

        if(saveToken == MOVE) {
            subcommands.add(setCurColor(color));
        }

        final int oldCurMoveNumber = curMoveNumber;
        if (saveToken == SETUP) {
            curMoveNumber = 0;
        }
        final UndoableCommand uc02SetStone = board.setStone(x, y, color, saveToken == SETUP || saveToken == HANDICAP);
        if (saveToken == SETUP) {
            curMoveNumber = oldCurMoveNumber;
        }

        if (uc02SetStone == null) {
            ListIterator<UndoableCommand> reverseIter = subcommands.listIterator(subcommands.size());
            while(reverseIter.hasPrevious()) {
                reverseIter.previous().undo();
            }
            return false;
        }

        uc02SetStone.getExecuteEvents().add(new GameEvent(GameCommand.SETUP_STONE_SET, x, y, null, 0));

        subcommands.add(uc02SetStone);

        if(saveToken == MOVE) {
            final UndoableCommand uc03IsKo = ruleset.isKo(this);
            if(uc03IsKo == null) {
                ListIterator<UndoableCommand> reverseIter = subcommands.listIterator(subcommands.size());
                while(reverseIter.hasPrevious()) {
                    reverseIter.previous().undo();
                }
                return false;
            }
            subcommands.add(uc03IsKo);

            final UndoableCommand uc04UpdateMoveNo = UndoableCommand.updateValue(i -> curMoveNumber = i, oldCurMoveNumber, oldCurMoveNumber + 1);
            uc04UpdateMoveNo.execute(true);
            subcommands.add(uc04UpdateMoveNo);
        }

        final int oldHandicapCtr = handicapStoneCounter;
        final int newHandicapCtr = handicapStoneCounter - 1;

        if(saveToken == HANDICAP) {
            final UndoableCommand uc05UpdateCounter = new UndoableCommand() {
                @Override
                public void execute(final boolean saveEffects) {
                    handicapStoneCounter = newHandicapCtr;

                    if (newHandicapCtr <= 0) {
                        gameState = GameState.RUNNING;
                    }
                }

                @Override
                public void undo() {
                    handicapStoneCounter = oldHandicapCtr;

                    gameState = GameState.SETTING_UP;
                }
            };
            uc05UpdateCounter.execute(true);

            subcommands.add(uc05UpdateCounter);
        }

        if(saveToken == MOVE || (saveToken == HANDICAP && newHandicapCtr <= 0)) {
            subcommands.add(switchColor());
        }

        UndoableCommand ret = UndoableCommand.of(subcommands);
        if(saveToken == HANDICAP) {
            ret.getExecuteEvents().add(new GameEvent(GameCommand.HANDICAP_SET, x, y, curMoveNumber));
            ret.getUndoEvents().add(new GameEvent(GameCommand.HANDICAP_REMOVED, x, y, curMoveNumber));
        }

        history.addNode(new History.HistoryNode(ret, saveToken, color, "", x, y));
        hideAllMarks();
        ret.getExecuteEvents().forEach(this::fireGameEvent);

        return true;
    }

    @Override
    public UndoableCommand addCapturedStones(final StoneColor color, final int amount) {
        if (color == null) {
            throw new NullPointerException();
        }
        if (amount < 0) {
            throw new IllegalArgumentException();
        }

        int oldAmount = gameResult.getScoreComponents(color).getOrDefault(GameResult.PointType.CAPTURED_STONES, 0).intValue();
        return gameResult.addScoreComponent(color, GameResult.PointType.CAPTURED_STONES, oldAmount + amount);
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
        hideAllMarks();
        history.stepBack();
        showAllMarks();
    }

    @Override
    public void redo() {
        hideAllMarks();
        history.stepForward();
        showAllMarks();
    }

    @Override
    public void rewind() {
        hideAllMarks();
        if(!history.isAtBeginning()) {
            if((history.getCurrentNode().getSaveToken() == HANDICAP || history.getCurrentNode().getSaveToken() == SETUP)) {
                history.gotoBeginning();
            } else if(!history.isAtBeginning()) {
                goBeforeFirstMove();
            }
        }
        showAllMarks();
    }

    @Override
    public void fastForward() {
        hideAllMarks();
        History.HistoryNode n = history.getCurrentNode();
        if(n.getSaveToken() == BEGINNING_OF_HISTORY || n.getSaveToken() == HANDICAP || n.getSaveToken() == SETUP) {
            goBeforeFirstMove();
            if(history.getCurrentNode() == n) {
                history.gotoEnd();
            }
        } else {
            history.gotoEnd();
        }
        showAllMarks();
    }

    @Override
    public void goBeforeFirstMove() {
        goToFirstMove();
        hideAllMarks();
        if(history.getCurrentNode().getSaveToken() != SETUP && history.getCurrentNode().getSaveToken() != HANDICAP) {
            history.stepBack();
        }
        showAllMarks();
    }

    @Override
    public void goToFirstMove() {
        hideAllMarks();
        history.gotoBeginning();

        do {
            history.stepForward();
        } while(!history.isAtEnd() && (history.getCurrentNode().getSaveToken() == HANDICAP || history.getCurrentNode().getSaveToken() == SETUP));
        showAllMarks();
    }

    @Override
    public void goToEnd() {
        hideAllMarks();
        history.gotoEnd();
        showAllMarks();
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
        UndoableCommand ret = setCurColor(StoneColor.getOpposite(curColor));
        ret.getExecuteEvents().add(new GameEvent(GameCommand.UPDATE));
        ret.getUndoEvents().add(new GameEvent(GameCommand.UPDATE));

        return ret;

    }

    private UndoableCommand setCurColor(final StoneColor newColor) {
        if (newColor == null) {
            throw new NullPointerException();
        }

        final StoneColor oldColor = this.curColor;

        UndoableCommand ret = UndoableCommand.updateValue(c -> curColor = c, oldColor, newColor);
        ret.execute(true);

        return ret;
    }

    private void hideAllMarks() {
        history.getCurrentNode().getMarks().forEach((key, value) -> fireGameEvent(new GameEvent(GameCommand.UNMARK, key.getX(), key.getY(), curMoveNumber)));
    }

    private void showAllMarks() {
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

