package pr_se.gogame.model;

import pr_se.gogame.model.file.FileHandler;
import pr_se.gogame.model.ruleset.GameResult;
import pr_se.gogame.model.ruleset.JapaneseRuleset;
import pr_se.gogame.model.ruleset.Ruleset;
import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static pr_se.gogame.model.StoneColor.BLACK;

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
    private int handicapStoneCounter = 0;   // counter for manually placed handicap stones
    private double playerBlackScore;
    private int blackCapturedStones;

    private double playerWhiteScore;
    private int whiteCapturedStones;
    private GameResult gameResult;

    private History history;

    public Game() {
        this.listeners = new ArrayList<>();
        this.gameState = GameState.NOT_STARTED_YET;
        this.board = new Board(this, 19);
    }

    @Override
    public void initGame() {
        gameState = GameState.NOT_STARTED_YET;

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

        this.gameState = GameState.SETTING_UP;

        this.history = new History(this);

        this.curColor = startingColor;
        this.handicap = handicap;
        this.ruleset = ruleset;

        this.playerBlackScore = handicap;
        this.playerWhiteScore = this.ruleset.getKomi();
        this.blackCapturedStones = 0;
        this.whiteCapturedStones = 0;
        this.curMoveNumber = 0; // Note: Indicates to the BoardPane that handicap stones are being set.
        this.gameResult = null;

        this.board = new Board(this, size);
        this.ruleset.reset();
        fireGameEvent(new GameEvent(GameCommand.NEW_GAME));

        int tempHandicap = 0;
        if(letRulesetPlaceHandicapStones) {
            tempHandicap = this.handicap;
        }
        this.ruleset.setHandicapStones(this, this.curColor, tempHandicap);

        this.curMoveNumber = 1;

        if(handicapStoneCounter <= 0) {
            gameState = GameState.RUNNING;
        } else {
            gameState = GameState.SETTING_UP;
        }
    }

    @Override
    public boolean saveGame(File file) {
        if (file == null) {
            return false;
        }
        return FileHandler.saveFile(this, file, history);
    }

    @Override
    public boolean loadGame(File file) {
        return FileHandler.loadFile(this, file);
    }

    @Override
    public void pass() {
        UndoableCommand c = switchColor();

        history.addNode(new HistoryNode(c, HistoryNode.AbstractSaveToken.PASS, StoneColor.getOpposite(curColor), "pass")); // StoneColor.getOpposite() because we switched colors before

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void resign() {
        if(gameState != GameState.RUNNING) {
            throw new IllegalStateException("Can't score game if it isn't running! gameState was + " + gameState);
        }

        final GameResult OLD_GAME_RESULT = this.gameResult;
        final StoneColor FINAL_CUR_COLOR = this.curColor;

        UndoableCommand c = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                String msg =
                        "Game was resigned by " + FINAL_CUR_COLOR + "!\n" +
                        "\n" +
                        StoneColor.getOpposite(FINAL_CUR_COLOR) + " won!";
                gameResult = new GameResult(playerBlackScore, playerWhiteScore, StoneColor.getOpposite(FINAL_CUR_COLOR), msg);
                if(saveEffects) {
                    getExecuteEvents().add(new GameEvent(GameCommand.GAME_WON));
                    getUndoEvents().add(new GameEvent(GameCommand.UPDATE));
                }
                gameState = GameState.GAME_OVER;
            }

            @Override
            public void undo() {
                gameResult = OLD_GAME_RESULT;
                gameState = GameState.RUNNING;
            }
        };
        c.execute(true);

        history.addNode(new HistoryNode(c, HistoryNode.AbstractSaveToken.RESIGN, FINAL_CUR_COLOR, "resign"));

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void scoreGame() {
        gameResult = ruleset.scoreGame(this);
        playerBlackScore = gameResult.getScoreBlack();
        playerWhiteScore = gameResult.getScoreWhite();
        gameState = GameState.GAME_OVER;

        fireGameEvent(new GameEvent(GameCommand.GAME_WON));
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

    /*
     * Although this method changes the state, it is only called at the beginning of the game and, hence, doesn't
     * appear to need to be undoable.
     */
    @Override
    public void setHandicapStoneCounter(int noStones) {
        if((noStones < 0 && noStones != -1)) {
            throw new IllegalArgumentException();
        }

        handicapStoneCounter = noStones;
        gameState = GameState.SETTING_UP;
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

    public int getHandicapStoneCounter() {
        return handicapStoneCounter;
    }

    private UndoableCommand setCurColor(StoneColor c) {
        if (c == null) {
            throw new NullPointerException();
        }

        final StoneColor OLD_COLOR = this.curColor;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                Game.this.curColor = c;
            }

            @Override
            public void undo() {
                Game.this.curColor = OLD_COLOR;
            }
        };
        ret.execute(true);

        return ret;
    }

    @Override
    public void playMove(int x, int y) {
        playMove(x, y, this.curColor);
    }

    @Override
    public void playMove(int x, int y, StoneColor color) {
        if(gameState != GameState.RUNNING) {
            throw new IllegalStateException("Can't place stone when game isn't running! Game State was " + gameState);
        }

        if(x < 0 || y < 0 || x >= board.getSize() || y >= board.getSize()) {
            throw new IllegalArgumentException();
        }

        final UndoableCommand UC01_SET_COLOR = setCurColor(color);

        final UndoableCommand UC02_SET_STONE = board.setStone(x, y, curColor, false); // UC02_SET_STONE is already executed within board.setStone().

        if(UC02_SET_STONE == null) {
            System.out.println("Move aborted.");
            return;
        }

        // Assertion: UC02_SET_STONE != null and was hence a valid move.

        final UndoableCommand UC03_IS_KO = ruleset.isKo(this);

        if(UC03_IS_KO == null) {
            UC02_SET_STONE.undo();
            System.out.println("Ko detected. Move aborted.");
            return;
        }

        final int OLD_MOVE_NO = curMoveNumber;

        final UndoableCommand UC04_SWITCH_COLOR = new UndoableCommand() {
            UndoableCommand thisCommand = null;

            @Override
            public void execute(boolean saveEffects) {
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
                if(thisCommand != null) {
                    thisCommand.undo();
                }
                curMoveNumber = OLD_MOVE_NO;
            }
        };
        UC04_SWITCH_COLOR.execute(true);

        UndoableCommand c = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                UC01_SET_COLOR.execute(saveEffects);
                UC02_SET_STONE.execute(saveEffects);
                UC03_IS_KO.execute(saveEffects);
                UC04_SWITCH_COLOR.execute(saveEffects);
            }

            @Override
            public void undo() {
                UC04_SWITCH_COLOR.undo();
                UC03_IS_KO.undo();
                UC02_SET_STONE.undo();
                UC01_SET_COLOR.undo();
            }
        };
        // c was already executed piecemeal

        /*
         * UC02 and UC04 fire events, so those have to be added to the command containing them.
         */
        c.getExecuteEvents().addAll(UC02_SET_STONE.getExecuteEvents());
        c.getExecuteEvents().addAll(UC04_SWITCH_COLOR.getExecuteEvents());
        c.getUndoEvents().addAll(UC02_SET_STONE.getUndoEvents());
        c.getUndoEvents().addAll(UC04_SWITCH_COLOR.getUndoEvents());

        /*
         * StoneColor.getOpposite() because we previously switched colors
         */
        removeAllMarks();
        history.addNode(new HistoryNode(c, HistoryNode.AbstractSaveToken.MOVE, StoneColor.getOpposite(curColor), x, y, "playMove(" + x + ", " + y + ")"));

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void placeHandicapPosition(int x, int y, boolean placeStone) {
        if(gameState != GameState.SETTING_UP) {
            throw new IllegalStateException("Can't place handicap stone when game isn't being set up! Game state was " + gameState);
        }

        if(x < 0 || y < 0 || x >= board.getSize() || y >= board.getSize()) {
            throw new IllegalArgumentException();
        }

        final int OLD_HANDICAP_CTR = handicapStoneCounter;
        handicapStoneCounter--;
        final int NEW_HANDICAP_CTR = handicapStoneCounter;

        if(placeStone) {
            if (OLD_HANDICAP_CTR <= 0) {
                throw new IllegalStateException("Can't place any more handicap stones!");
            }

            final UndoableCommand UC01_SET_STONE = board.setStone(x, y, curColor, true); // UC01_SET_STONE is already executed within board.setStone().

            if(UC01_SET_STONE == null) {
                System.out.println("Move aborted.");
                return;
            }

            // Assertion: UC01_SET_STONE != null and was hence a valid move.

            final UndoableCommand UC02_UPDATE_COUNTER = new UndoableCommand() {
                UndoableCommand uC02_switchColor = null;

                @Override
                public void execute(boolean saveEffects) {
                    handicapStoneCounter = NEW_HANDICAP_CTR;

                    if (NEW_HANDICAP_CTR <= 0) {
                        System.out.println("handicapStoneCounter is now less than 0.");
                        uC02_switchColor = switchColor();
                        if(saveEffects) {
                            getExecuteEvents().addAll(uC02_switchColor.getExecuteEvents());
                            getUndoEvents().addAll(uC02_switchColor.getUndoEvents());
                        }

                        gameState = GameState.RUNNING;
                    }
                }

                @Override
                public void undo() {
                    if(uC02_switchColor != null) {
                        uC02_switchColor.undo();
                    }
                    handicapStoneCounter = OLD_HANDICAP_CTR;

                    gameState = GameState.SETTING_UP;
                }
            };
            UC02_UPDATE_COUNTER.execute(true);

            UndoableCommand c = new UndoableCommand() {
                @Override
                public void execute(boolean saveEffects) {
                    UC01_SET_STONE.execute(saveEffects);
                    UC02_UPDATE_COUNTER.execute(saveEffects);
                }

                @Override
                public void undo() {
                    UC02_UPDATE_COUNTER.undo();
                    UC01_SET_STONE.undo();
                }
            };
            // c was already executed piecemeal
            c.getExecuteEvents().addAll(UC01_SET_STONE.getExecuteEvents());
            c.getExecuteEvents().addAll(UC02_UPDATE_COUNTER.getExecuteEvents());
            c.getUndoEvents().addAll(UC01_SET_STONE.getUndoEvents());
            c.getUndoEvents().addAll(UC02_UPDATE_COUNTER.getUndoEvents());

            /*
             * StoneColor.getOpposite() because we previously switched colors
             */
            StoneColor col = curColor;
            if(NEW_HANDICAP_CTR <= 0) {
                col = StoneColor.getOpposite(curColor);
            }
            removeAllMarks();
            history.addNode(new HistoryNode(c, HistoryNode.AbstractSaveToken.HANDICAP, col, x, y, "placeHandicapPosition(" + x + ", " + y + ")"));

            for(GameEvent e : c.getExecuteEvents()) {
                fireGameEvent(e);
            }

            c.getExecuteEvents().add(new GameEvent(GameCommand.HANDICAP_SET, x, y, null, curMoveNumber));
        }

        fireGameEvent(new GameEvent(GameCommand.HANDICAP_SET, x, y, null, curMoveNumber));
        System.out.println();
    }

    @Override
    public void placeSetupStone(int x, int y, StoneColor color) {
        if(x < 0 || y < 0 || x >= board.getSize() || y >= board.getSize()) {
            throw new IllegalArgumentException();
        }

        final int OLD_CUR_MOVE_NUMBER = curMoveNumber;
        curMoveNumber = 0;
        final UndoableCommand UC01_SET_STONE = board.setStone(x, y, color, true); // UC01_SET_STONE is already executed within board.setStone().
        curMoveNumber = OLD_CUR_MOVE_NUMBER;

        if(UC01_SET_STONE == null) {
            System.out.println("Move aborted.");
            return;
        }

        // Assertion: UC01_SET_STONE != null and was hence a valid move.

        /*
         * StoneColor.getOpposite() because we previously switched colors
         */
        removeAllMarks();
        UC01_SET_STONE.getExecuteEvents().add(new GameEvent(GameCommand.SETUP_STONE_SET, x, y, null, 0));
        history.addNode(new HistoryNode(UC01_SET_STONE, HistoryNode.AbstractSaveToken.SETUP, color, x, y, "placeSetupStone(" + x + ", " + y + ")"));

        for(GameEvent e : UC01_SET_STONE.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public UndoableCommand addCapturedStones(StoneColor color, int amount) {
        if (color == null) {
            throw new NullPointerException();
        }
        if (amount < 0) {
            throw new IllegalArgumentException();
        }

        final int OLD_BLACK_CAPTURED_STONES = blackCapturedStones;
        final int OLD_WHITE_CAPTURED_STONES = whiteCapturedStones;
        final double OLD_BLACK_PLAYER_SCORE = playerBlackScore;
        final double OLD_WHITE_PLAYER_SCORE = playerWhiteScore;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                if (color == BLACK) {
                    Game.this.blackCapturedStones += amount; // TODO: If this causes issues, maybe change to "OLD_BLACK_CAPTURED_STONES + amount" and so on?
                    Game.this.playerBlackScore += amount;
                } else {
                    Game.this.whiteCapturedStones += amount;
                    Game.this.playerWhiteScore += amount;
                }
            }

            @Override
            public void undo() {
                Game.this.blackCapturedStones = OLD_BLACK_CAPTURED_STONES;
                Game.this.whiteCapturedStones = OLD_WHITE_CAPTURED_STONES;
                Game.this.playerBlackScore = OLD_BLACK_PLAYER_SCORE;
                Game.this.playerWhiteScore = OLD_WHITE_PLAYER_SCORE;
            }
        };
        ret.execute(true);

        return ret;
    }

    @Override
    public int getStonesCapturedBy(StoneColor color) {
        if (color == null) throw new NullPointerException();

        if (color == BLACK) return this.blackCapturedStones;
        else return this.whiteCapturedStones;
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

    private UndoableCommand switchColor() {
        UndoableCommand ret = new UndoableCommand() {
            UndoableCommand thisCommand;

            @Override
            public void execute(boolean saveEffects) {
                thisCommand = setCurColor(StoneColor.getOpposite(curColor));
                if(saveEffects) {
                    getExecuteEvents().add(new GameEvent(GameCommand.UPDATE));
                    getUndoEvents().add(new GameEvent(GameCommand.UPDATE));
                }
            }

            @Override
            public void undo() {
                if(thisCommand != null) {
                    thisCommand.undo();
                }
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
            if((history.getCurrentNode().getSaveToken() == HistoryNode.AbstractSaveToken.HANDICAP || history.getCurrentNode().getSaveToken() == HistoryNode.AbstractSaveToken.SETUP)) {
                history.rewind();
            } else {
                goToFirstMove();
            }
        }
        reDisplayMarks();
    }

    @Override
    public void fastForward() {
        removeAllMarks();
        if(history.isAtBeginning()) {
            history.stepForward();
        }
        if((history.getCurrentNode().getSaveToken() == HistoryNode.AbstractSaveToken.HANDICAP || history.getCurrentNode().getSaveToken() == HistoryNode.AbstractSaveToken.SETUP)) {
            goToFirstMove();
        } else {
            history.skipToEnd();
        }
        reDisplayMarks();
    }

    @Override
    public void goToFirstMove() {
        removeAllMarks();
        history.rewind();

        do {
            history.stepForward();
        } while(!history.isAtEnd() && (history.getCurrentNode().getSaveToken() == HistoryNode.AbstractSaveToken.HANDICAP || history.getCurrentNode().getSaveToken() == HistoryNode.AbstractSaveToken.SETUP));
        reDisplayMarks();
    }

    public void removeAllMarks() {
        history.getCurrentNode().getMarks().forEach((key, value) -> fireGameEvent(new GameEvent(GameCommand.UNMARK, key.x, key.y, curMoveNumber)));
    }

    public void reDisplayMarks() {
        history.getCurrentNode().getMarks().forEach((key, value) -> {
            switch (value) {
                case CIRCLE:
                    fireGameEvent(new GameEvent(GameCommand.MARK_CIRCLE, key.x, key.y, curMoveNumber));
                    break;

                case SQUARE:
                    fireGameEvent(new GameEvent(GameCommand.MARK_SQUARE, key.x, key.y, curMoveNumber));
                    break;

                case TRIANGLE:
                    fireGameEvent(new GameEvent(GameCommand.MARK_TRIANGLE, key.x, key.y, curMoveNumber));
                    break;

                default:
                    break;
            }
        });
    }

    @Override
    public String getComment() {
        return history.currentComment();
    }

    @Override
    public void commentCurrentMove(String comment) {
        if(comment == null) {
            throw new NullPointerException();
        }
        history.getCurrentNode().setComment(comment);
        fireGameEvent(new GameEvent(GameCommand.UPDATE));
    }

    @Override
    public void mark(int x, int y, MarkShape shape) {
        history.getCurrentNode().addMark(x, y, shape);
        GameCommand gc = null;
        switch(shape) {
            case CIRCLE:
                gc = GameCommand.MARK_CIRCLE;
                break;

            case SQUARE:
                gc = GameCommand.MARK_SQUARE;
                break;

            case TRIANGLE:
                gc = GameCommand.MARK_TRIANGLE;
                break;
        }
        fireGameEvent(new GameEvent(gc, x, y, curMoveNumber));
    }

    @Override
    public void unmark(int x, int y) {
        history.getCurrentNode().removeMark(x, y);
        fireGameEvent(new GameEvent(GameCommand.UNMARK, x, y, curMoveNumber));
    }

}

